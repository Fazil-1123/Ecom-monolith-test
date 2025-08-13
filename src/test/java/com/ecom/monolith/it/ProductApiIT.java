package com.ecom.monolith.it;

import com.ecom.monolith.BaseIntegrationTest;
import com.ecom.monolith.Dto.ProductDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
public class ProductApiIT extends BaseIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void create_list_search_fetch_product_happy_path() throws Exception {
        ProductDto payload = new ProductDto();
        payload.setName("iphone 15");
        payload.setDescription("iphone 15");
        payload.setPrice(BigDecimal.valueOf(1500));
        payload.setStockQuantity(50);
        payload.setCategory("Electronic");
        payload.setImageUrl("img.png");

        String json = objectMapper.writeValueAsString(payload);

        String createResponse = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("iphone 15")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        ProductDto created = objectMapper.readValue(createResponse, ProductDto.class);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[*].name", hasItem("iphone 15")));

        mockMvc.perform(get("/api/products/search").param("keyword", "iphon"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", hasItem("iphone 15")));

        mockMvc.perform(get("/api/products/{id}", created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(created.getId().intValue())))
                .andExpect(jsonPath("$.name", is("iphone 15")));
    }

    @Test
    void validation_errors_return_400() throws Exception {
        ProductDto productDto = new ProductDto();
        productDto.setDescription("invalid");

        String json = objectMapper.writeValueAsString(productDto);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }
}
