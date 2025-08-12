package com.ecom.monolith.Mapper;

import com.ecom.monolith.Dto.OrderItemResponse;
import com.ecom.monolith.model.OrderItem;
import com.ecom.monolith.model.Product;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

public class OrderItemResponseMapperTest {

    private final OrderItemResponseMapper mapper = Mappers.getMapper(OrderItemResponseMapper.class);

    @Test
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
    void nullInputs_returnNull() {
        assertThat(mapper.toDto(null)).isNull();
    }
}
