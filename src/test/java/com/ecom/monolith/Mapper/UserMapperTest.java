package com.ecom.monolith.Mapper;

import com.ecom.monolith.Dto.UsersDto;
import com.ecom.monolith.Dto.AddressDto;
import com.ecom.monolith.model.Users;
import com.ecom.monolith.model.Address;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.*;

public class UserMapperTest {

    private final UserMapper mapper = Mappers.getMapper(UserMapper.class);

    @Test
    void toDto_mapsAllBasicFields_andAddressFields() {
        Users user = user(
                1L, "John", "Doe", "john.doe@example.com", "1234567890",
                address("221B Baker St", "London", "Greater London", "UK", "NW1")
        );

        UsersDto dto = mapper.toDto(user);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getFirstName()).isEqualTo("John");
        assertThat(dto.getLastName()).isEqualTo("Doe");
        assertThat(dto.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(dto.getPhone()).isEqualTo("1234567890");

        assertThat(dto.getAddress()).isNotNull();
        assertThat(dto.getAddress().getStreet()).isEqualTo("221B Baker St");
        assertThat(dto.getAddress().getCity()).isEqualTo("London");
        assertThat(dto.getAddress().getState()).isEqualTo("Greater London");
        assertThat(dto.getAddress().getCountry()).isEqualTo("UK");
        assertThat(dto.getAddress().getZipcode()).isEqualTo("NW1");
    }

    @Test
    void toEntity_mapsAllBasicFields_andAddressFields() {
        UsersDto dto = userDto(
                2L, "Jane", "Smith", "jane.smith@example.com", "0987654321",
                addressDto("742 Evergreen Terrace", "Springfield", "AnyState", "USA", "49007")
        );

        Users user = mapper.toEntity(dto);

        assertThat(user.getId()).isEqualTo(2L);
        assertThat(user.getFirstName()).isEqualTo("Jane");
        assertThat(user.getLastName()).isEqualTo("Smith");
        assertThat(user.getEmail()).isEqualTo("jane.smith@example.com");
        assertThat(user.getPhone()).isEqualTo("0987654321");

        assertThat(user.getAddress()).isNotNull();
        assertThat(user.getAddress().getStreet()).isEqualTo("742 Evergreen Terrace");
        assertThat(user.getAddress().getCity()).isEqualTo("Springfield");
        assertThat(user.getAddress().getState()).isEqualTo("AnyState");
        assertThat(user.getAddress().getCountry()).isEqualTo("USA");
        assertThat(user.getAddress().getZipcode()).isEqualTo("49007");
    }

    @Test
    void nullInputs_returnNull() {
        assertThat(mapper.toDto(null)).isNull();
        assertThat(mapper.toEntity(null)).isNull();
    }


    private Users user(Long id, String first, String last, String email, String phone, Address addr) {
        Users u = new Users();
        u.setId(id);
        u.setFirstName(first);
        u.setLastName(last);
        u.setEmail(email);
        u.setPhone(phone);
        u.setAddress(addr);
        return u;
    }

    private UsersDto userDto(Long id, String first, String last, String email, String phone, AddressDto addr) {
        UsersDto u = new UsersDto();
        u.setId(id);
        u.setFirstName(first);
        u.setLastName(last);
        u.setEmail(email);
        u.setPhone(phone);
        u.setAddress(addr);
        return u;
    }

    private Address address(String street, String city, String state, String country, String zipcode) {
        Address a = new Address();
        a.setStreet(street);
        a.setCity(city);
        a.setState(state);
        a.setCountry(country);
        a.setZipcode(zipcode);
        return a;
    }

    private AddressDto addressDto(String street, String city, String state, String country, String zipcode) {
        AddressDto a = new AddressDto();
        a.setStreet(street);
        a.setCity(city);
        a.setState(state);
        a.setCountry(country);
        a.setZipcode(zipcode);
        return a;
    }
}
