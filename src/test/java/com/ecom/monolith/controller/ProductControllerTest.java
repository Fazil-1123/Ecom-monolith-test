package com.ecom.monolith.controller;

import com.ecom.monolith.Dto.ProductDto;
import com.ecom.monolith.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for the ProductController class.
 * This class verifies the behavior of the product-related endpoints.
 */
@WebMvcTest(ProductController.class)
public class ProductControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    ProductService productService;

    @Test
    @DisplayName("Should return HTTP 200 and a list of all products")
    void getAllProducts_ok() throws Exception {
        ProductDto product1 = productDto(1L, "iphone 15", "iphone 15", BigDecimal.valueOf(1300), 10);
        ProductDto product2 = productDto(2L, "iphone 16", "iphone 16", BigDecimal.valueOf(1500), 10);

        when(productService.getAllProducts()).thenReturn(List.of(product1, product2));

        mockMvc.perform(get("/api/products").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("iphone 15"))
                .andExpect(jsonPath("$[1].name").value("iphone 16"));

        verify(productService).getAllProducts();
        verifyNoMoreInteractions(productService);
    }

    @Test
    @DisplayName("Should return HTTP 200 and the product details for a valid ID")
    void findById_ok() throws Exception {
        ProductDto product1 = productDto(1L, "iphone 15", "iphone 15", BigDecimal.valueOf(1300), 10);

        when(productService.findById(1L)).thenReturn(product1);

        mockMvc.perform(get("/api/products/{id}", 1L).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("iphone 15"));

        verify(productService).findById(1L);
        verifyNoMoreInteractions(productService);
    }

    @Test
    @DisplayName("Should return HTTP 200 and a list of products matching the keyword")
    void findByKeyword_ok() throws Exception {
        String keyword = "iphone";
        ProductDto product1 = productDto(1L, "iphone 15", "iphone 15", BigDecimal.valueOf(1300), 10);
        ProductDto product2 = productDto(2L, "iphone 16", "iphone 16", BigDecimal.valueOf(1500), 10);

        when(productService.findByKeyword(keyword)).thenReturn(List.of(product1, product2));

        mockMvc.perform(get("/api/products/search").param("keyword", keyword).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("iphone 15"))
                .andExpect(jsonPath("$[1].name").value("iphone 16"));

        verify(productService).findByKeyword(keyword);
        verifyNoMoreInteractions(productService);
    }

    @Test
    @DisplayName("Should return HTTP 200 when a product is successfully added")
    void addProduct_ok() throws Exception {
        ProductDto productDto = productDto(1L, "iphone 15", "iphone 15", BigDecimal.valueOf(1300), 10);

        when(productService.addProduct(any(ProductDto.class))).thenReturn(productDto);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("iphone 15"));

        verify(productService).addProduct(any(ProductDto.class));
        verifyNoMoreInteractions(productService);
    }

    @Test
    @DisplayName("Should return HTTP 200 when a product is successfully updated")
    void updateProduct_ok() throws Exception {
        ProductDto productDtoToUpdate = productDto(1L, "iphone 15", "iphone 15", BigDecimal.valueOf(1300), 10);
        ProductDto productDtoUpdated = productDto(1L, "updated iphone 15", "iphone 15", BigDecimal.valueOf(1300), 10);

        when(productService.updateProduct(1L, productDtoToUpdate)).thenReturn(productDtoUpdated);

        mockMvc.perform(put("/api/products/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productDtoToUpdate)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("updated iphone 15"));

        verify(productService).updateProduct(1L, productDtoToUpdate);
        verifyNoMoreInteractions(productService);
    }

    @Test
    @DisplayName("Should return HTTP 200 when a product is successfully deleted")
    void deleteProduct_ok() throws Exception {
        String responseMessage = "Product deleted successfully";

        when(productService.deleteProduct(1L)).thenReturn(responseMessage);

        mockMvc.perform(delete("/api/products/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string(responseMessage));

        verify(productService).deleteProduct(1L);
        verifyNoMoreInteractions(productService);
    }

    private ProductDto productDto(Long id, String name, String description, BigDecimal price, Integer stock) {
        ProductDto productDto = new ProductDto();
        productDto.setId(id);
        productDto.setName(name);
        productDto.setDescription(description);
        productDto.setPrice(price);
        productDto.setStockQuantity(stock);
        productDto.setCategory("Electronic");
        return productDto;
    }
}