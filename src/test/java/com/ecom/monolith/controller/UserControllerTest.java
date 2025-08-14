package com.ecom.monolith.controller;

import com.ecom.monolith.Dto.AddressDto;
import com.ecom.monolith.Dto.UsersDto;
import com.ecom.monolith.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for the UserController class.
 * This class verifies the behavior of the user-related endpoints.
 */
@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    UserService userService;

    @Test
    @DisplayName("Should return HTTP 200 and a list of all users")
    void getUsers_ok() throws Exception {
        UsersDto userDto1 = createUserDto(
                1L, "Jane", "Smith", "john.doe@example.com", "1234567890",
                createAddressDto("221B Baker St", "London", "Greater London", "UK", "NW1")
        );
        UsersDto userDto2 = createUserDto(
                2L, "John", "Doe", "jane.smith@example.com", "0987654321",
                createAddressDto("742 Evergreen Terrace", "Springfield", "AnyState", "USA", "49007")
        );
        when(userService.getUsers()).thenReturn(List.of(userDto1, userDto2));

        mockMvc.perform(get("/api/users").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].email").value("john.doe@example.com"));

        verify(userService).getUsers();
        verifyNoMoreInteractions(userService);
    }

    @Test
    @DisplayName("Should return HTTP 200 when a user is successfully added")
    void addUser_ok() throws Exception {
        UsersDto userDto1 = createUserDto(
                1L, "Jane", "Smith", "john.doe@example.com", "1234567890",
                createAddressDto("221B Baker St", "London", "Greater London", "UK", "NW1")
        );
        when(userService.addUser(any(UsersDto.class))).thenReturn(userDto1);

        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto1)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        verify(userService).addUser(userDto1);
        verifyNoMoreInteractions(userService);
    }

    @Test
    @DisplayName("Should return HTTP 200 and the user details for a valid ID")
    void findById_ok() throws Exception {
        UsersDto userDto1 = createUserDto(
                1L, "Jane", "Smith", "john.doe@example.com", "1234567890",
                createAddressDto("221B Baker St", "London", "Greater London", "UK", "NW1")
        );
        when(userService.findById(1L)).thenReturn(userDto1);

        mockMvc.perform(get("/api/users/{id}", 1L).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        verify(userService).findById(1L);
        verifyNoMoreInteractions(userService);
    }

    @Test
    @DisplayName("Should return HTTP 200 when a user is successfully updated")
    void updateUser_ok() throws Exception {
        UsersDto userDto1 = createUserDto(
                1L, "Jane", "Smith", "john.doe@example.com", "1234567890",
                createAddressDto("221B Baker St", "London", "Greater London", "UK", "NW1")
        );
        UsersDto userDto2 = createUserDto(
                1L, "UpdatedJane", "Smith", "Updatedjohn.doe@example.com", "1234567890",
                createAddressDto("221B Baker St", "London", "Greater London", "UK", "NW1")
        );
        when(userService.updateUser(1L, userDto1)).thenReturn(userDto2);

        mockMvc.perform(put("/api/users/{id}", 1L).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto1)))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("UpdatedJane"))
                .andExpect(jsonPath("$.email").value("Updatedjohn.doe@example.com"));

        verify(userService).updateUser(1L, userDto1);
        verifyNoMoreInteractions(userService);
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