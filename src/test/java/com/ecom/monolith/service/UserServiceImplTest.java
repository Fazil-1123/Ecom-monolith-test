package com.ecom.monolith.service;

import com.ecom.monolith.Dto.AddressDto;
import com.ecom.monolith.Dto.UsersDto;
import com.ecom.monolith.Mapper.UserMapper;
import com.ecom.monolith.exception.ResourceNotFound;
import com.ecom.monolith.model.Address;
import com.ecom.monolith.model.UserRole;
import com.ecom.monolith.model.Users;
import com.ecom.monolith.repositories.UsersRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the UserServiceImpl class.
 * This class verifies the service methods for managing users.
 */
@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    UsersRepository usersRepository;

    @Mock
    UserMapper userMapper;

    @InjectMocks
    UserServiceImpl userService;

    @Test
    @DisplayName("Verify getUsers returns mapped list of users")
    void getUsers_returnsMappedList() {
        UsersDto userDto1 = createUserDto(
                1L, "Jane", "Smith", "john.doe@example.com", "1234567890",
                createAddressDto("221B Baker St", "London", "Greater London", "UK", "NW1")
        );
        UsersDto userDto2 = createUserDto(
                2L, "John", "Doe", "jane.smith@example.com", "0987654321",
                createAddressDto("742 Evergreen Terrace", "Springfield", "AnyState", "USA", "49007")
        );
        Users user1 = createUser(
                1L, "Jane", "Smith", "john.doe@example.com", "1234567890", UserRole.CUSTOMER,
                createAddress(1L, "221B Baker St", "London", "Greater London", "UK", "NW1")
        );
        Users user2 = createUser(
                2L, "John", "Doe", "jane.smith@example.com", "0987654321", UserRole.CUSTOMER,
                createAddress(2L, "742 Evergreen Terrace", "Springfield", "AnyState", "USA", "49007")
        );
        when(usersRepository.findAll()).thenReturn(List.of(user1, user2));
        when(userMapper.toDto(user1)).thenReturn(userDto1);
        when(userMapper.toDto(user2)).thenReturn(userDto2);

        List<UsersDto> result = userService.getUsers();

        assertThat(result).extracting(UsersDto::getId).containsExactly(1L, 2L);
        assertThat(result.get(0).getEmail()).isEqualTo("john.doe@example.com");
        assertThat(result.get(0).getAddress().getCity()).isEqualTo("London");

        verify(usersRepository).findAll();
        verify(userMapper).toDto(user1);
        verify(userMapper).toDto(user2);
        verifyNoMoreInteractions(usersRepository, userMapper);
    }

    @Test
    @DisplayName("Verify getUsers returns empty list when no users are found")
    void getUsers_empty_returnsEmptyList() {
        when(usersRepository.findAll()).thenReturn(List.of());

        List<UsersDto> result = userService.getUsers();

        assertThat(result).isEmpty();
        verify(usersRepository).findAll();
        verifyNoMoreInteractions(usersRepository);
        verifyNoInteractions(userMapper);
    }

    @Test
    @DisplayName("Verify addUser saves and returns the saved user")
    void addUser_returnedSavedUser() {
        Users user1 = createUser(
                1L, "Jane", "Smith", "john.doe@example.com", "1234567890", UserRole.CUSTOMER,
                createAddress(1L, "221B Baker St", "London", "Greater London", "UK", "NW1")
        );
        UsersDto userDto1 = createUserDto(
                1L, "Jane", "Smith", "john.doe@example.com", "1234567890",
                createAddressDto("221B Baker St", "London", "Greater London", "UK", "NW1")
        );
        when(usersRepository.save(any(Users.class))).thenAnswer(
                invocation -> invocation.getArgument(0, Users.class)
        );
        when(userMapper.toDto(user1)).thenReturn(userDto1);
        when(userMapper.toEntity(userDto1)).thenReturn(user1);

        UsersDto savedUserDto = userService.addUser(userDto1);
        assertThat(savedUserDto.getId()).isEqualTo(1L);
        assertThat(savedUserDto.getFirstName()).isEqualTo("Jane");

        ArgumentCaptor<Users> captor = ArgumentCaptor.forClass(Users.class);
        verify(usersRepository).save(captor.capture());
        Users savedUser = captor.getValue();
        assertThat(savedUser.getId()).isEqualTo(1L);
        assertThat(savedUser.getFirstName()).isEqualTo("Jane");

        verify(userMapper).toDto(user1);
        verify(userMapper).toEntity(userDto1);
        verifyNoMoreInteractions(usersRepository, userMapper);
    }

    @Test
    @DisplayName("Verify findById returns mapped user DTO")
    void findById_returnsMappedDto() {
        Long id = 1L;
        Users user1 = createUser(
                id, "Jane", "Smith", "jane@example.com", "1234567890", UserRole.CUSTOMER,
                createAddress(10L, "221B Baker St", "London", "Greater London", "UK", "NW1")
        );
        UsersDto userDto1 = createUserDto(
                id, "Jane", "Smith", "jane@example.com", "1234567890",
                createAddressDto("221B Baker St", "London", "Greater London", "UK", "NW1")
        );

        when(usersRepository.findById(id)).thenReturn(Optional.of(user1));
        when(userMapper.toDto(user1)).thenReturn(userDto1);

        UsersDto result = userService.findById(id);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getFirstName()).isEqualTo("Jane");

        verify(usersRepository).findById(id);
        verify(userMapper).toDto(user1);
        verifyNoMoreInteractions(usersRepository, userMapper);
    }

    @Test
    @DisplayName("Verify findById throws exception when user is not found")
    void findById_notFound_throws() {
        Long id = 99L;
        when(usersRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(id))
                .isInstanceOf(ResourceNotFound.class)
                .hasMessageContaining("User not found with id " + id);

        verify(usersRepository).findById(id);
        verifyNoMoreInteractions(usersRepository);
        verifyNoInteractions(userMapper);
    }

    @Test
    @DisplayName("Verify updateUser merges fields and saves updated user")
    void updateUser_mergesNames_andSaves() {
        Long id = 1L;

        UsersDto patch = createUserDto(
                id, "NewFirst", "NewLast",
                "ignored@example.com", "000",
                createAddressDto("ignored", "ignored", "ignored", "ignored", "ignored")
        );

        Users userUpdate = createUser(null, "NewFirst", "NewLast", null, null, null, null);
        when(userMapper.toEntity(patch)).thenReturn(userUpdate);

        Users existing = createUser(
                id, "OldFirst", "OldLast", "jane@example.com", "1234567890",
                UserRole.CUSTOMER,
                createAddress(10L, "221B Baker St", "London", "Greater London", "UK", "NW1")
        );
        when(usersRepository.findById(id)).thenReturn(Optional.of(existing));
        when(usersRepository.save(any(Users.class))).thenAnswer(inv -> inv.getArgument(0, Users.class));
        when(userMapper.toDto(any(Users.class))).thenAnswer(inv -> {
            Users u = inv.getArgument(0, Users.class);
            UsersDto d = new UsersDto();
            d.setId(u.getId());
            d.setFirstName(u.getFirstName());
            d.setLastName(u.getLastName());
            d.setEmail(u.getEmail());
            d.setPhone(u.getPhone());
            var addr = u.getAddress();
            if (addr != null) {
                d.setAddress(createAddressDto(
                        addr.getStreet(), addr.getCity(), addr.getState(), addr.getCountry(), addr.getZipcode()
                ));
            }
            return d;
        });

        UsersDto result = userService.updateUser(id, patch);

        assertThat(result.getFirstName()).isEqualTo("NewFirst");
        assertThat(result.getLastName()).isEqualTo("NewLast");
        assertThat(result.getEmail()).isEqualTo("jane@example.com");

        ArgumentCaptor<Users> captor = ArgumentCaptor.forClass(Users.class);
        verify(usersRepository).save(captor.capture());
        Users persisted = captor.getValue();
        assertThat(persisted.getId()).isEqualTo(id);
        assertThat(persisted.getFirstName()).isEqualTo("NewFirst");
        assertThat(persisted.getLastName()).isEqualTo("NewLast");
        assertThat(persisted.getEmail()).isEqualTo("jane@example.com");

        verify(userMapper).toEntity(patch);
        verify(usersRepository).findById(id);
        verify(userMapper).toDto(persisted);
        verifyNoMoreInteractions(usersRepository, userMapper);
    }

    @Test
    @DisplayName("Verify updateUser throws exception when user is not found")
    void updateUser_notFound_throws_andMapperStillCalledForPatch() {
        Long id = 42L;

        UsersDto patch = createUserDto(
                id, "NewFirst", "NewLast",
                "ignored@example.com", "000",
                createAddressDto("x", "x", "x", "x", "x")
        );
        when(userMapper.toEntity(patch)).thenReturn(createUser(null, "NewFirst", "NewLast", null, null, null, null));
        when(usersRepository.findById(id)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(id, patch))
                .isInstanceOf(ResourceNotFound.class)
                .hasMessageContaining("User not found with id " + id);

        verify(userMapper).toEntity(patch);
        verify(usersRepository).findById(id);
        verifyNoMoreInteractions(usersRepository, userMapper);
    }

    private Users createUser(Long id, String firstName, String lastName, String email,
                             String phone, UserRole role, Address address) {
        Users user = new Users();
        user.setId(id);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setRole(role);
        user.setAddress(address);
        return user;
    }

    private Address createAddress(Long id, String street, String city, String state, String country, String zipcode) {
        Address address = new Address();
        address.setId(id);
        address.setStreet(street);
        address.setCity(city);
        address.setState(state);
        address.setCountry(country);
        address.setZipcode(zipcode);
        return address;
    }

    private UsersDto createUserDto(Long id, String firstName, String lastName, String email,
                                   String phone, AddressDto address) {
        UsersDto usersDto = new UsersDto();
        usersDto.setId(id);
        usersDto.setFirstName(firstName);
        usersDto.setLastName(lastName);
        usersDto.setEmail(email);
        usersDto.setPhone(phone);
        usersDto.setAddress(address);
        return usersDto;
    }

    private AddressDto createAddressDto(String street, String city, String state, String country, String zipcode) {
        AddressDto addressDto = new AddressDto();
        addressDto.setStreet(street);
        addressDto.setCity(city);
        addressDto.setState(state);
        addressDto.setCountry(country);
        addressDto.setZipcode(zipcode);
        return addressDto;
    }
}