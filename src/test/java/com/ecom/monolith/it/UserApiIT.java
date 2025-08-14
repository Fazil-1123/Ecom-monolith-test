package com.ecom.monolith.it;

import com.ecom.monolith.BaseIntegrationTest;
import com.ecom.monolith.Dto.UsersDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the User API.
 * Scenarios covered:
 * - Create a user, list all users, fetch by ID, and update a user.
 * - Validation errors return HTTP 400.
 */
@AutoConfigureMockMvc
public class UserApiIT extends BaseIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("Create, list, fetch, and update user successfully")
    void create_list_fetch_update_user_happy_path() throws Exception {
        UsersDto created = createUser("Jane", "Smith", "john.doe@example.com", "1234567890");

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[*].firstName", hasItem("Jane")));

        mockMvc.perform(get("/api/users/{id}", created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(created.getId().intValue())))
                .andExpect(jsonPath("$.firstName", is("Jane")))
                .andExpect(jsonPath("$.lastName", is("Smith")));

        created.setLastName("UpdatedSmith");
        String updatedJson = objectMapper.writeValueAsString(created);

        mockMvc.perform(put("/api/users/{id}", created.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName", is("UpdatedSmith")));
    }

    @Test
    @DisplayName("Return 400 for validation errors on user creation")
    void validation_errors_return_400_on_create() throws Exception {
        UsersDto usersDto = new UsersDto();
        String badJson = objectMapper.writeValueAsString(usersDto);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badJson))
                .andExpect(status().isBadRequest());
    }

    private UsersDto createUser(String first, String last, String email, String phone) throws Exception {
        UsersDto usersDto = new UsersDto();
        usersDto.setFirstName(first);
        usersDto.setLastName(last);
        usersDto.setEmail(email);
        usersDto.setPhone(phone);

        String json = objectMapper.writeValueAsString(usersDto);

        String body = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.firstName", is(first)))
                .andExpect(jsonPath("$.lastName", is(last)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(body, UsersDto.class);
    }
}