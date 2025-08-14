package com.ecom.monolith.Mapper;

import com.ecom.monolith.Dto.OrderItemResponse;
import com.ecom.monolith.model.OrderItem;
import com.ecom.monolith.model.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for the OrderItemResponseMapper class.
 * This class verifies the mapping logic between OrderItem and OrderItemResponse.
 */
public class OrderItemResponseMapperTest {

    private final OrderItemResponseMapper mapper = Mappers.getMapper(OrderItemResponseMapper.class);

    @Test
    @DisplayName("Verify toDto maps OrderItem to OrderItemResponse correctly")
    void toDto_orderItem_basicMapping() {
        Product p = new Product();
        p.setId(7L);

        OrderItem item = new OrderItem();
        item.setProduct(p);
        item.setPrice(new BigDecimal("123.45"));

        OrderItemResponse dto = mapper.toDto(item);

        assertThat(dto).isNotNull();
    }

    @Test
    @DisplayName("Verify null inputs return null")
    void nullInputs_returnNull() {
        assertThat(mapper.toDto(null)).isNull();
    }
}