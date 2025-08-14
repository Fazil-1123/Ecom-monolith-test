package com.ecom.monolith.Mapper;

import com.ecom.monolith.Dto.ProductDto;
import com.ecom.monolith.model.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;

/**
 * Unit tests for the ProductMapper class.
 * This class verifies the mapping logic between Product and ProductDto.
 */
public class ProductMapperTest {

    private final ProductMapper mapper = Mappers.getMapper(ProductMapper.class);

    @Test
    @DisplayName("Verify toDto maps all fields correctly")
    void toDto_mapsAllFields() {
        Product productToMap = product(1L, "iphone", "new iphone", BigDecimal.valueOf(1400), true, 10);
        ProductDto mappedDto = mapper.toDto(productToMap);
        assertThat(mappedDto.getId()).isEqualTo(1L);
        assertThat(mappedDto.getName()).isEqualTo("iphone");
        assertThat(mappedDto.getDescription()).isEqualTo("new iphone");
        assertThat(mappedDto.getPrice()).isEqualByComparingTo("1400");
        assertThat(mappedDto.getStockQuantity()).isEqualTo(10);
        assertThat(mappedDto.getCategory()).isEqualTo("Electronic");
    }

    @Test
    @DisplayName("Verify toEntity maps all fields correctly")
    void toEntity_mapsAllFields() {
        ProductDto productDtoToMap = productDto(1L, "iphone", "new iphone", BigDecimal.valueOf(1400), 10);
        Product mapperEntity = mapper.toEntity(productDtoToMap);
        assertThat(mapperEntity.getId()).isEqualTo(1L);
        assertThat(mapperEntity.getName()).isEqualTo("iphone");
        assertThat(mapperEntity.getDescription()).isEqualTo("new iphone");
        assertThat(mapperEntity.getPrice()).isEqualByComparingTo("1400");
        assertThat(mapperEntity.getStockQuantity()).isEqualTo(10);
        assertThat(mapperEntity.getCategory()).isEqualTo("Electronic");
    }

    @Test
    @DisplayName("Verify null inputs return null")
    void nullInputs_returnNull() {
        assertThat(mapper.toDto(null)).isNull();
        assertThat(mapper.toEntity(null)).isNull();
    }

    private Product product(Long id, String name, String description, BigDecimal price, boolean active, Integer stock) {
        Product product = new Product();
        product.setName(name);
        product.setId(id);
        product.setDescription(description);
        product.setPrice(price);
        product.setActive(active);
        product.setStockQuantity(stock);
        product.setCategory("Electronic");
        return product;
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