package com.ecom.monolith.controller;

import com.ecom.monolith.Dto.CartRequest;
import com.ecom.monolith.Dto.CartResponse;
import com.ecom.monolith.service.CartItemService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
public class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CartItemService cartItemService;

    @Test
    void addToCart_ok() throws Exception {
        String productId = "1L";
        int quantity = 2;

        CartRequest cartRequest = createCartRequest(productId, quantity);

        when(cartItemService.addCartItem("1L", cartRequest)).thenReturn(true);

        mockMvc.perform(post("/api/cart")
                        .header("X-User-ID", "1L")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cartRequest)))
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk());


        verify(cartItemService).addCartItem("1L", cartRequest);
    }

    @Test
    void addToCart_outOfStock() throws Exception {
        String productId = "1L";
        int quantity = 2;

        CartRequest cartRequest = createCartRequest(productId, quantity);

        when(cartItemService.addCartItem("1L", cartRequest)).thenReturn(false);

        mockMvc.perform(post("/api/cart")
                        .header("X-User-ID", "1L")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cartRequest)))
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(status().isNotFound());


        verify(cartItemService).addCartItem("1L", cartRequest);
    }

    @Test
    void removeItem_ok() throws Exception {
        Long productId = 1L;

        when(cartItemService.removeItem("1L", productId)).thenReturn(true);

        mockMvc.perform(delete("/api/cart/items/{productId}", productId)
                        .header("X-User-ID", "1L").accept(MediaType.TEXT_PLAIN))
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk());

        verify(cartItemService).removeItem("1L", productId);
    }

    @Test
    void removeItem_CartNotFound() throws Exception {
        Long productId = 1L;

        when(cartItemService.removeItem("1L", productId)).thenReturn(false);

        mockMvc.perform(delete("/api/cart/items/{productId}", productId)
                        .header("X-User-ID", "1L").accept(MediaType.TEXT_PLAIN))
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(status().isNotFound());

        verify(cartItemService).removeItem("1L", productId);
    }

    @Test
    void getCartItems_ok() throws Exception {
        CartResponse cartResponse1 = createCartResponse("1L", 2);
        CartResponse cartResponse2 = createCartResponse("2L", 3);

        when(cartItemService.getCartItems("1L")).thenReturn(List.of(cartResponse1, cartResponse2));

        mockMvc.perform(get("/api/cart")
                        .header("X-User-ID", "1L").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].productId").value("1L"))
                .andExpect(jsonPath("$[0].quantity").value(2))
                .andExpect(jsonPath("$[1].productId").value("2L"))
                .andExpect(jsonPath("$[1].quantity").value(3));

        verify(cartItemService).getCartItems("1L");
    }

    private CartRequest createCartRequest(String productId, int quantity) {
        CartRequest cartRequest = new CartRequest();
        cartRequest.setProductId(productId);
        cartRequest.setQuantity(quantity);
        return cartRequest;
    }

    private CartResponse createCartResponse(String productId, int quantity) {
        CartResponse cartResponse = new CartResponse();
        cartResponse.setProductId(productId);
        cartResponse.setQuantity(quantity);
        return cartResponse;
    }
}
