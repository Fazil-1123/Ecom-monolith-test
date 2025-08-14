package com.ecom.monolith.service;

import com.ecom.monolith.Dto.ProductDto;
import com.ecom.monolith.Mapper.ProductMapper;
import com.ecom.monolith.exception.ResourceNotFound;
import com.ecom.monolith.model.Product;
import com.ecom.monolith.repositories.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Unit tests for the ProductServiceImpl class.
 * This class verifies the service methods for managing products.
 */
@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTest {

    @Mock
    ProductRepository productRepository;

    @Mock
    ProductMapper productMapper;

    @InjectMocks
    ProductServiceImpl productService;

    private static final long id = 1L;

    @Test
    @DisplayName("Verify findById returns mapped ProductDto")
    void findById_returnsMappedDto() {
        Product entity = product(id, "iphone", "latest iphone with AI", BigDecimal.valueOf(1500), true, 10);
        ProductDto dto = productDto(id, "iphone", "latest iphone with AI", BigDecimal.valueOf(1500), 10);
        when(productRepository.findById(id)).thenReturn(Optional.of(entity));
        when(productMapper.toDto(entity)).thenReturn(dto);

        ProductDto returnedDto = productService.findById(id);

        assertThat(returnedDto.getId()).isEqualTo(id);
        assertThat(returnedDto.getName()).isEqualTo("iphone");
        verify(productRepository).findById(id);
        verify(productMapper).toDto(entity);
        verifyNoMoreInteractions(productRepository, productMapper);
    }

    @Test
    @DisplayName("Verify findById throws exception when product is not found")
    void findById_returnsException() {
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(id))
                .isInstanceOf(ResourceNotFound.class)
                .hasMessageContaining("Resource not found with id: " + id);

        verify(productRepository).findById(id);
        verifyNoInteractions(productMapper);
    }

    @Test
    @DisplayName("Verify findByKeyword maps all found active products")
    void findByKeyword_mapsAllFoundActiveProducts() {
        String keyword = "iphon";
        Product product1 = product(1L, "iphone 15", "iphone 15", BigDecimal.valueOf(1300), true, 10);
        Product product2 = product(2L, "iphone 16", "iphone 16", BigDecimal.valueOf(1500), true, 10);

        when(productRepository.findByNameContainingIgnoreCaseAndActiveTrue(keyword))
                .thenReturn(List.of(product1, product2));

        ProductDto dto1 = productDto(id, "iphone 15", "iphone 15", BigDecimal.valueOf(1300), 10);
        ProductDto dto2 = productDto(2L, "iphone 16", "iphone 16", BigDecimal.valueOf(1500), 10);
        when(productMapper.toDto(product1)).thenReturn(dto1);
        when(productMapper.toDto(product2)).thenReturn(dto2);

        List<ProductDto> result = productService.findByKeyword(keyword);

        assertThat(result).extracting(ProductDto::getId).containsExactly(1L, 2L);
        verify(productRepository).findByNameContainingIgnoreCaseAndActiveTrue(keyword);
        verify(productMapper).toDto(product1);
        verify(productMapper).toDto(product2);
        verifyNoMoreInteractions(productRepository, productMapper);
    }

    @Test
    @DisplayName("Verify findByKeyword returns empty list when no products are found")
    void findByKeyword_returnsEmptyList() {
        String keyword = "nope";
        when(productRepository.findByNameContainingIgnoreCaseAndActiveTrue(keyword))
                .thenReturn(List.of());

        List<ProductDto> result = productService.findByKeyword(keyword);

        assertThat(result).isEmpty();
        verify(productRepository).findByNameContainingIgnoreCaseAndActiveTrue(keyword);
        verifyNoInteractions(productMapper);
    }

    @Test
    @DisplayName("Verify addProduct maps and saves product, then returns ProductDto")
    void addProduct_mapsAndSaves_andReturnsDto() {
        Product entity = product(id, "iphone", "latest iphone with AI", BigDecimal.valueOf(1500), true, 10);
        ProductDto dto = productDto(id, "iphone", "latest iphone with AI", BigDecimal.valueOf(1500), 10);

        when(productMapper.toEntity(dto)).thenReturn(entity);
        when(productRepository.save(entity)).thenReturn(entity);
        when(productMapper.toDto(entity)).thenReturn(dto);

        ProductDto savedDto = productService.addProduct(dto);

        assertThat(savedDto.getId()).isEqualTo(id);
        assertThat(savedDto.getName()).isEqualTo("iphone");

        verify(productRepository).save(entity);
        verify(productMapper).toDto(entity);
        verify(productMapper).toEntity(dto);
        verifyNoMoreInteractions(productRepository, productMapper);
    }

    @Test
    @DisplayName("Verify updateProduct merges fields and saves updated product")
    void updateProduct_mergesFields_andSaves() {
        Product entity = product(id, "iphone", "iphone", BigDecimal.valueOf(1500), true, 10);
        ProductDto updatedDto = productDto(id, "iphone 16", "latest iphone with AI", BigDecimal.valueOf(1500), 10);

        when(productRepository.findById(id)).thenReturn(Optional.of(entity));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0, Product.class));
        when(productMapper.toDto(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0, Product.class);
            return productDto(p.getId(), p.getName(), p.getDescription(), p.getPrice(), p.getStockQuantity());
        });

        ProductDto updatedProduct = productService.updateProduct(id, updatedDto);

        assertThat(updatedProduct.getName()).isEqualTo("iphone 16");
        assertThat(updatedProduct.getDescription()).isEqualTo("latest iphone with AI");

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).findById(id);
        verify(productRepository).save(captor.capture());
        Product saved = captor.getValue();

        assertThat(saved.getName()).isEqualTo("iphone 16");
        assertThat(saved.getDescription()).isEqualTo("latest iphone with AI");

        verify(productMapper).toDto(saved);
        verifyNoMoreInteractions(productRepository, productMapper);
    }

    @Test
    @DisplayName("Verify updateProduct throws exception when product is not found")
    void updateProduct_notFound_throws() {
        ProductDto updatedDto = productDto(id, "iphone 16", "latest iphone with AI", BigDecimal.valueOf(1500), 10);
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct(id, updatedDto))
                .isInstanceOf(ResourceNotFound.class)
                .hasMessageContaining("Resource not found with id: " + id);

        verify(productRepository).findById(id);
        verifyNoInteractions(productMapper);
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    @DisplayName("Verify getAllProducts returns all active products")
    void getAllProducts_success() {
        Product product1 = product(1L, "iphone 15", "iphone 15", BigDecimal.valueOf(1300), true, 10);
        Product product2 = product(2L, "iphone 16", "iphone 16", BigDecimal.valueOf(1500), true, 10);
        when(productRepository.findByActiveTrue()).thenReturn(List.of(product1, product2));

        ProductDto dto1 = productDto(id, "iphone 15", "iphone 15", BigDecimal.valueOf(1300), 10);
        ProductDto dto2 = productDto(2L, "iphone 16", "iphone 16", BigDecimal.valueOf(1500), 10);
        when(productMapper.toDto(product1)).thenReturn(dto1);
        when(productMapper.toDto(product2)).thenReturn(dto2);

        List<ProductDto> result = productService.getAllProducts();

        assertThat(result).extracting(ProductDto::getId).containsExactly(1L, 2L);
        verify(productRepository).findByActiveTrue();
        verify(productMapper).toDto(product1);
        verify(productMapper).toDto(product2);
        verifyNoMoreInteractions(productRepository, productMapper);
    }

    @Test
    @DisplayName("Verify deleteProduct deactivates product successfully")
    void deleteProduct_success() {
        Product entity = product(id, "iphone", "iphone", BigDecimal.valueOf(1500), true, 10);
        when(productRepository.findById(id)).thenReturn(Optional.of(entity));
        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, Product.class));

        String response = productService.deleteProduct(id);

        assertThat(response).isEqualTo("Product deleted successfully");

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        Product deletedProduct = captor.getValue();
        assertThat(deletedProduct.getActive()).isFalse();

        verify(productRepository).findById(id);
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    @DisplayName("Verify deleteProduct throws exception when product is not found")
    void deleteProduct_notFound() {
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.deleteProduct(id))
                .isInstanceOf(ResourceNotFound.class)
                .hasMessageContaining("Resource not found with id: " + id);

        verify(productRepository).findById(id);
        verifyNoMoreInteractions(productRepository);
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