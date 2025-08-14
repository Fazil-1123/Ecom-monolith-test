package com.ecom.monolith.controller;

import com.ecom.monolith.Dto.OrderItemResponse;
import com.ecom.monolith.Dto.OrderResponse;
import com.ecom.monolith.model.OrderStatus;
import com.ecom.monolith.service.OrderService;
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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for the OrderController class.
 * This class verifies the behavior of the order-related endpoints.
 */
@WebMvcTest(OrderController.class)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @Test
    @DisplayName("Should return HTTP 201 when order is successfully placed")
    void placeOrder_ok() throws Exception {
        long userId = 1L;
        BigDecimal totalAmount = BigDecimal.valueOf(100.00);
        OrderItemResponse orderItemResponse1 = createOrderItemResponse(1L, 1L, 2, BigDecimal.valueOf(50.00));
        OrderItemResponse orderItemResponse2 = createOrderItemResponse(2L, 2L, 1, BigDecimal.valueOf(50.00));

        OrderResponse orderResponse = createOrderResponse(1L, totalAmount, List.of(orderItemResponse1, orderItemResponse2));

        when(orderService.placeOrder(Long.toString(userId))).thenReturn(orderResponse);

        mockMvc.perform(post("/api/orders")
                        .header("X-User-ID", Long.toString(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderResponse)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(orderResponse.getId()))
                .andExpect(jsonPath("$.totalAmount").value(totalAmount.doubleValue()))
                .andExpect(jsonPath("$.status").value(OrderStatus.ORDERED.toString()))
                .andExpect(jsonPath("$.items.length()").value(orderResponse.getItems().size()));

        verify(orderService).placeOrder(Long.toString(userId));
    }

    private OrderResponse createOrderResponse(Long id, BigDecimal totalAmount, List<OrderItemResponse> items) {
        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setId(id);
        orderResponse.setTotalAmount(totalAmount);
        orderResponse.setStatus(OrderStatus.ORDERED);
        return orderResponse;
    }

    private OrderItemResponse createOrderItemResponse(Long id, Long productId, Integer quantity, BigDecimal price) {
        OrderItemResponse orderItemResponse = new OrderItemResponse();
        orderItemResponse.setId(id);
        orderItemResponse.setProductId(productId);
        orderItemResponse.setQuantity(quantity);
        orderItemResponse.setPrice(price);
        return orderItemResponse;
    }
}
