package com.ecom.monolith.repositories;

import com.ecom.monolith.model.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ProductRepositoryTest {

    @Autowired
    ProductRepository productRepository;

    @Test
    void findByActiveTrue_active() {

        Product product1 = product( "iphone 15", "iphone 15", BigDecimal.valueOf(1300), true, 10);
        Product product2 = product( "iphone 16", "iphone 16", BigDecimal.valueOf(1500), true, 10);
        Product product3 = product( "iphone 17", "iphone 17", BigDecimal.valueOf(2000), false, 10);

        productRepository.saveAll(List.of(product1, product2, product3));
        productRepository.flush();
        List<Product> activeProducts = productRepository.findByActiveTrue();

        assertThat(activeProducts).extracting(Product::getName)
                .containsExactlyInAnyOrder("iphone 15", "iphone 16");
        assertThat(activeProducts).allMatch(product -> Boolean.TRUE.equals(product.getActive()));
    }

    @Test
    void findByNameContainingIgnoreCaseAndActiveTrue_active() {
        Product product1 = product( "iphone 15", "iphone 15", BigDecimal.valueOf(1300), true, 10);
        Product product2 = product( "iphone 16", "iphone 16", BigDecimal.valueOf(1500), true, 10);
        Product product3 = product( "samsung s25", "samsung s25", BigDecimal.valueOf(2000), true, 10);

        productRepository.saveAll(List.of(product1, product2, product3));
        productRepository.flush();
        List<Product> returnedProducts = productRepository.findByNameContainingIgnoreCaseAndActiveTrue("iphon");

        assertThat(returnedProducts).extracting(Product::getName)
                .containsExactlyInAnyOrder("iphone 15", "iphone 16");
        assertThat(returnedProducts).allMatch(product -> Boolean.TRUE.equals(product.getActive()));

    }

    private Product product(String name, String description, BigDecimal price, boolean active, Integer stock) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setActive(active);
        product.setStockQuantity(stock);
        product.setCategory("Electronic");
        return product;
    }
}
