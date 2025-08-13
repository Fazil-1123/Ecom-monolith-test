package com.ecom.monolith.it;

import com.ecom.monolith.BaseIntegrationTest;
import com.ecom.monolith.Dto.CartRequest;
import com.ecom.monolith.Dto.ProductDto;
import com.ecom.monolith.Dto.UsersDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class CartApiIT extends BaseIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

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

    private ProductDto createProduct(String name, BigDecimal price) throws Exception {
        ProductDto productDto = new ProductDto();
        productDto.setName(name);
        productDto.setDescription(name + " desc");
        productDto.setPrice(price);
        productDto.setStockQuantity(100);
        productDto.setCategory("Electronic");
        productDto.setImageUrl("img.png");

        String body = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readValue(body, ProductDto.class);
    }

    @Test
    void add_item_list_items_then_remove_item_happy_path() throws Exception {

        UsersDto user = createUser("Jane", "Smith", "john.doe@example.com", "1234567890");
        ProductDto product = createProduct("iphone 15", BigDecimal.valueOf(1500));

        CartRequest cartRequest = new CartRequest();
        cartRequest.setProductId(String.valueOf(product.getId()));
        cartRequest.setQuantity(2);

        mockMvc.perform(post("/api/cart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-ID", String.valueOf(user.getId()))
                        .content(objectMapper.writeValueAsString(cartRequest)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/cart")
                        .header("X-User-ID", String.valueOf(user.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[*].productId", hasItem(String.valueOf(product.getId()))))
                .andExpect(jsonPath("$[*].quantity", hasItem(2)));

        mockMvc.perform(delete("/api/cart/items/{productId}", product.getId())
                        .header("X-User-ID", String.valueOf(user.getId())))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/cart")
                        .header("X-User-ID", String.valueOf(user.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }


    @Test
    void missing_user_header_returns_400() throws Exception {
        CartRequest req = new CartRequest();
        req.setProductId("123");
        req.setQuantity(1);

        mockMvc.perform(post("/api/cart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invalid_quantity_returns_400() throws Exception {
        UsersDto user = createUser("Jane", "Smith", "john.doe@example.com", "1234567890");
        ProductDto prod = createProduct("Phone Case", new BigDecimal("9.99"));

        CartRequest req = new CartRequest();
        req.setProductId(String.valueOf(prod.getId()));
        req.setQuantity(0);

        mockMvc.perform(post("/api/cart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-ID", String.valueOf(user.getId()))
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}
