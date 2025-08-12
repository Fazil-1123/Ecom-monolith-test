package com.ecom.monolith.Mapper;

import com.ecom.monolith.Dto.CartResponse;
import com.ecom.monolith.model.CartItem;
import com.ecom.monolith.model.Product;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import static org.assertj.core.api.Assertions.*;

public class CartMapperTest {

    private final CartMapper mapper = Mappers.getMapper(CartMapper.class);

    @Test
    void toDto_flattensProductId() {
        Product p = new Product();
        p.setId(42L);

        CartItem item = new CartItem();
        item.setProduct(p);

        CartResponse dto = mapper.toDto(item);

        assertThat(dto.getProductId()).isEqualTo("42");
    }

    @Test
    void nullInputs_returnNull() {
        assertThat(mapper.toDto(null)).isNull();
        assertThat(mapper.toEntity(null)).isNull();
    }
}
