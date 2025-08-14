package com.ecom.monolith.it;

import com.ecom.monolith.BaseIntegrationTest;
import com.ecom.monolith.Dto.CartRequest;
import com.ecom.monolith.Dto.ProductDto;
import com.ecom.monolith.Dto.UsersDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the Order API.
 * Scenarios covered:
 * - Place an order from the cart and clear the cart.
 * - Missing user header returns 400.
 * - Placing an order with an empty cart returns 404.
 */
@AutoConfigureMockMvc
public class OrderApiIT extends BaseIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("Place order from cart and clear cart successfully")
    void place_order_from_cart_returns_201_and_clears_cart() throws Exception {
        UsersDto user = createUser("Jane", "Smith", "john.doe@example.com", "1234567890");
        ProductDto product1 = createProduct("iphone 15", BigDecimal.valueOf(1500));
        ProductDto product2 = createProduct("iphone 16", BigDecimal.valueOf(1700));

        addToCart(user.getId(), product1.getId(), 2);
        addToCart(user.getId(), product2.getId(), 1);

        String orderJson = mockMvc.perform(post("/api/orders")
                        .header("X-User-ID", String.valueOf(user.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.items", not(empty())))
                .andExpect(jsonPath("$.items[*].productId", hasItems(product1.getId().intValue(), product2.getId().intValue())))
                .andExpect(jsonPath("$.items[*].quantity", hasItems(2, 1)))
                .andExpect(jsonPath("$.totalAmount", notNullValue()))
                .andReturn().getResponse().getContentAsString();

        mockMvc.perform(get("/api/cart")
                        .header("X-User-ID", String.valueOf(user.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Return 400 when user header is missing")
    void missing_user_header_returns_400() throws Exception {
        mockMvc.perform(post("/api/orders"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Return 404 when placing order with an empty cart")
    void placing_order_with_empty_cart_returns_404() throws Exception {
        UsersDto user = createUser("Jane", "Smith", "john.doe@example.com", "1234567890");

        mockMvc.perform(post("/api/orders")
                        .header("X-User-ID", String.valueOf(user.getId())))
                .andExpect(status().isNotFound());
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

    private void addToCart(long userId, long productId, int quantity) throws Exception {
        CartRequest cartRequest = new CartRequest();
        cartRequest.setProductId(String.valueOf(productId));
        cartRequest.setQuantity(quantity);

        mockMvc.perform(post("/api/cart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-ID", String.valueOf(userId))
                        .content(objectMapper.writeValueAsString(cartRequest)))
                .andExpect(status().isOk());
    }
}
