package com.ecom.monolith.repositories;

import com.ecom.monolith.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class CartItemRepositoryTest {

    @Autowired
    CartItemRepository cartItemRepository;

    @Autowired
    TestEntityManager entityManager;

    @Test
    void findByUsersAndProduct_success() {
        Users user1 = createUser(
                "Jane", "Smith", "john.doe@example.com", "1234567890", UserRole.CUSTOMER,
                createAddress("221B Baker St", "London", "Greater London", "UK", "NW1")
        );
        Users user2 = createUser(
                "John", "Doe", "jane.smith@example.com", "0987654321", UserRole.CUSTOMER,
                createAddress("742 Evergreen Terrace", "Springfield", "AnyState", "USA", "49007")
        );
        Product product1 = product("iphone 15", "iphone 15", BigDecimal.valueOf(1300), true, 10);
        Product product2 = product("iphone 16", "iphone 16", BigDecimal.valueOf(1500), true, 10);
        CartItem cartItem1 = createCartItem(user1, product1, 5, BigDecimal.valueOf(6500));
        CartItem cartItem2 = createCartItem(user1, product2, 5, BigDecimal.valueOf(6500));
        CartItem cartItem3 = createCartItem(user2, product1, 5, BigDecimal.valueOf(6500));

        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.persist(product1);
        entityManager.persist(product2);
        entityManager.persist(cartItem1);
        entityManager.persist(cartItem2);
        entityManager.persist(cartItem3);
        entityManager.flush();

        List<CartItem> returnedCart = cartItemRepository.findByUsersId(user1.getId());

        assertThat(returnedCart).hasSize(2);
        assertThat(returnedCart).allMatch(ci -> ci.getUsers().getId().equals(user1.getId()));
        assertThat(returnedCart).extracting(ci -> ci.getProduct().getName())
                .containsExactlyInAnyOrder("iphone 15", "iphone 16");


    }

    @Test
    void findByUsersId_success() {
        Users user1 = createUser(
                "Jane", "Smith", "john.doe@example.com", "1234567890", UserRole.CUSTOMER,
                createAddress("221B Baker St", "London", "Greater London", "UK", "NW1")
        );
        Users user2 = createUser(
                "John", "Doe", "jane.smith@example.com", "0987654321", UserRole.CUSTOMER,
                createAddress("742 Evergreen Terrace", "Springfield", "AnyState", "USA", "49007")
        );
        Product product1 = product("iphone 15", "iphone 15", BigDecimal.valueOf(1300), true, 10);
        Product product2 = product("iphone 16", "iphone 16", BigDecimal.valueOf(1500), true, 10);
        CartItem cartItem1 = createCartItem(user1, product1, 5, BigDecimal.valueOf(6500));
        CartItem cartItem2 = createCartItem(user1, product2, 5, BigDecimal.valueOf(6500));
        CartItem cartItem3 = createCartItem(user2, product1, 5, BigDecimal.valueOf(6500));

        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.persist(product1);
        entityManager.persist(product2);
        entityManager.persist(cartItem1);
        entityManager.persist(cartItem2);
        entityManager.persist(cartItem3);
        entityManager.flush();

        List<CartItem> returnedCart = cartItemRepository.findByUsersId(user1.getId());

        assertThat(returnedCart).hasSize(2);
        assertThat(returnedCart).allMatch(ci -> ci.getUsers().getId().equals(user1.getId()));

    }

    private Users createUser(String firstName, String lastName, String email,
                             String phone, UserRole role, Address address) {
        Users user = new Users();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setRole(role);
        user.setAddress(address);
        return user;
    }

    private Address createAddress(String street, String city, String state, String country, String zipcode) {

        Address address = new Address();
        address.setStreet(street);
        address.setCity(city);
        address.setState(state);
        address.setCountry(country);
        address.setZipcode(zipcode);
        return address;

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

    private CartItem createCartItem(Users users, Product product, int quantity, BigDecimal price) {
        CartItem cartItem = new CartItem();
        cartItem.setUsers(users);
        cartItem.setProduct(product);
        cartItem.setQuantity(quantity);
        cartItem.setPrice(price);
        return cartItem;
    }
}
