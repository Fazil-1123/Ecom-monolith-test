package com.ecom.monolith.Mapper;

import com.ecom.monolith.Dto.CartResponse;
import com.ecom.monolith.model.CartItem;
import com.ecom.monolith.model.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for the CartMapper class.
 * This class verifies the mapping logic between CartItem and CartResponse.
 */
public class CartMapperTest {

    private final CartMapper mapper = Mappers.getMapper(CartMapper.class);

    @Test
    @DisplayName("Verify toDto maps product ID correctly")
    void toDto_flattensProductId() {
        Product p = new Product();
        p.setId(42L);

        CartItem item = new CartItem();
        item.setProduct(p);

        CartResponse dto = mapper.toDto(item);

        assertThat(dto.getProductId()).isEqualTo("42");
    }

    @Test
    @DisplayName("Verify null inputs return null")
    void nullInputs_returnNull() {
        assertThat(mapper.toDto(null)).isNull();
        assertThat(mapper.toEntity(null)).isNull();
    }
}