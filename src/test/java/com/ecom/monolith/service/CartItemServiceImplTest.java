package com.ecom.monolith.service;

import com.ecom.monolith.Dto.CartRequest;
import com.ecom.monolith.Dto.CartResponse;
import com.ecom.monolith.Mapper.CartMapper;
import com.ecom.monolith.exception.ResourceNotFound;
import com.ecom.monolith.model.*;
import com.ecom.monolith.repositories.CartItemRepository;
import com.ecom.monolith.repositories.ProductRepository;
import com.ecom.monolith.repositories.UsersRepository;
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
 * Unit tests for the CartItemServiceImpl class.
 * This class verifies the service methods for managing cart items.
 */
@ExtendWith(MockitoExtension.class)
public class CartItemServiceImplTest {

    @Mock
    CartItemRepository cartItemRepository;

    @Mock
    UsersRepository usersRepository;

    @Mock
    ProductRepository productRepository;

    @Mock
    CartMapper cartMapper;

    @InjectMocks
    CartItemServiceImpl cartItemService;

    private static final Long id = 1L;

    @Test
    @DisplayName("Verify addCartItem throws exception when product is not found")
    void addCartItem_ProductNotFound() {
        CartRequest cartRequest = createCartRequest(String.valueOf(id), 5);
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartItemService.addCartItem(String.valueOf(id), cartRequest))
                .isInstanceOf(ResourceNotFound.class)
                .hasMessageContaining("Product not found with id: " + id);

        verify(productRepository).findById(id);
        verifyNoMoreInteractions(productRepository);
        verifyNoInteractions(usersRepository, cartItemRepository);
    }

    @Test
    @DisplayName("Verify addCartItem returns false when product stock is insufficient")
    void addCartItem_ProductExist_LowStock() {
        CartRequest cartRequest = createCartRequest(String.valueOf(id), 15);
        Product product1 = product(1L, "iphone 15", "iphone 15", BigDecimal.valueOf(1300), true, 10);
        when(productRepository.findById(id)).thenReturn(Optional.of(product1));

        assertThat(cartItemService.addCartItem(String.valueOf(id), cartRequest)).isFalse();

        verify(productRepository).findById(id);
        verifyNoMoreInteractions(productRepository);
        verifyNoInteractions(usersRepository, cartItemRepository);
    }

    @Test
    @DisplayName("Verify addCartItem throws exception when user is not found")
    void addCartItem_ProductExist_UserNotFound() {
        CartRequest cartRequest = createCartRequest(String.valueOf(id), 15);
        Product product1 = product(1L, "iphone 15", "iphone 15", BigDecimal.valueOf(1300), true, 20);
        when(productRepository.findById(id)).thenReturn(Optional.of(product1));
        when(usersRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartItemService.addCartItem(String.valueOf(id), cartRequest))
                .isInstanceOf(ResourceNotFound.class)
                .hasMessageContaining("User does not exist with id: " + id);

        verify(productRepository).findById(id);
        verify(usersRepository).findById(id);
        verifyNoMoreInteractions(productRepository, usersRepository);
        verifyNoInteractions(cartItemRepository);
    }

    @Test
    @DisplayName("Verify addCartItem updates existing cart item when product and user exist")
    void addCartItem_ProductAndUserExist_ExistingCart() {
        CartRequest cartRequest = createCartRequest(String.valueOf(id), 15);
        Product product1 = product(1L, "iphone 15", "iphone 15", BigDecimal.valueOf(1300), true, 20);
        Users user1 = createUser(
                id, "Jane", "Smith", "jane@example.com", "1234567890", UserRole.CUSTOMER,
                createAddress(10L, "221B Baker St", "London", "Greater London", "UK", "NW1")
        );
        CartItem cartItem1 = createCartItem(id, user1, product1, 5, BigDecimal.valueOf(6500));
        when(productRepository.findById(id)).thenReturn(Optional.of(product1));
        when(usersRepository.findById(id)).thenReturn(Optional.of(user1));
        when(cartItemRepository.findByUsersAndProduct(user1, product1)).thenReturn(Optional.of(cartItem1));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(
                invocation -> invocation.getArgument(0, CartItem.class)
        );

        Boolean response = cartItemService.addCartItem(String.valueOf(id), cartRequest);

        assertThat(response).isTrue();

        ArgumentCaptor<CartItem> captor = ArgumentCaptor.forClass(CartItem.class);
        verify(cartItemRepository).save(captor.capture());
        CartItem savedCartItem = captor.getValue();

        assertThat(savedCartItem.getQuantity()).isEqualTo(20);
        assertThat(savedCartItem.getPrice()).isEqualByComparingTo("26000");

        verify(productRepository).findById(id);
        verify(usersRepository).findById(id);
        verify(cartItemRepository).findByUsersAndProduct(user1, product1);
        verify(cartItemRepository).save(any(CartItem.class));
        verifyNoMoreInteractions(productRepository, usersRepository, cartItemRepository);
    }

    @Test
    @DisplayName("Verify addCartItem creates a new cart item when product and user exist")
    void addCartItem_ProductAndUserExist_NewCart() {
        CartRequest cartRequest = createCartRequest(String.valueOf(id), 15);
        Product product1 = product(1L, "iphone 15", "iphone 15", BigDecimal.valueOf(1300), true, 20);
        Users user1 = createUser(
                id, "Jane", "Smith", "jane@example.com", "1234567890", UserRole.CUSTOMER,
                createAddress(10L, "221B Baker St", "London", "Greater London", "UK", "NW1")
        );
        when(productRepository.findById(id)).thenReturn(Optional.of(product1));
        when(usersRepository.findById(id)).thenReturn(Optional.of(user1));
        when(cartItemRepository.findByUsersAndProduct(user1, product1)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(
                invocation -> invocation.getArgument(0, CartItem.class)
        );

        Boolean response = cartItemService.addCartItem(String.valueOf(id), cartRequest);

        assertThat(response).isTrue();

        ArgumentCaptor<CartItem> captor = ArgumentCaptor.forClass(CartItem.class);
        verify(cartItemRepository).save(captor.capture());
        CartItem savedCartItem = captor.getValue();

        assertThat(savedCartItem.getQuantity()).isEqualTo(15);
        assertThat(savedCartItem.getPrice()).isEqualByComparingTo("19500");

        verify(productRepository).findById(id);
        verify(usersRepository).findById(id);
        verify(cartItemRepository).findByUsersAndProduct(user1, product1);
        verify(cartItemRepository).save(any(CartItem.class));
        verifyNoMoreInteractions(productRepository, usersRepository, cartItemRepository);
    }

    @Test
    @DisplayName("Verify removeItem throws exception when product is not found")
    void removeItem_ProductNotFound() {
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartItemService.removeItem(String.valueOf(id), id))
                .isInstanceOf(ResourceNotFound.class)
                .hasMessageContaining("Product not found with id: " + id);

        verify(productRepository).findById(id);
        verifyNoMoreInteractions(productRepository);
        verifyNoInteractions(usersRepository, cartItemRepository);
    }

    @Test
    @DisplayName("Verify removeItem throws exception when user is not found")
    void removeItem_UserNotFound() {
        Product product1 = product(1L, "iphone 15", "iphone 15", BigDecimal.valueOf(1300), true, 20);
        when(productRepository.findById(id)).thenReturn(Optional.of(product1));
        when(usersRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartItemService.removeItem(String.valueOf(id), id))
                .isInstanceOf(ResourceNotFound.class)
                .hasMessageContaining("User does not exist with id: " + id);

        verify(productRepository).findById(id);
        verify(usersRepository).findById(id);
        verifyNoMoreInteractions(productRepository, usersRepository);
        verifyNoInteractions(cartItemRepository);
    }

    @Test
    @DisplayName("Verify removeItem returns false when cart item is not found")
    void removeItem_CartNotFound() {
        Product product1 = product(1L, "iphone 15", "iphone 15", BigDecimal.valueOf(1300), true, 20);
        Users user1 = createUser(
                id, "Jane", "Smith", "jane@example.com", "1234567890", UserRole.CUSTOMER,
                createAddress(10L, "221B Baker St", "London", "Greater London", "UK", "NW1")
        );
        when(productRepository.findById(id)).thenReturn(Optional.of(product1));
        when(usersRepository.findById(id)).thenReturn(Optional.of(user1));
        when(cartItemRepository.findByUsersAndProduct(user1, product1)).thenReturn(Optional.empty());

        Boolean response = cartItemService.removeItem(String.valueOf(id), id);

        assertThat(response).isFalse();

        verify(productRepository).findById(id);
        verify(usersRepository).findById(id);
        verify(cartItemRepository).findByUsersAndProduct(user1, product1);
        verifyNoMoreInteractions(productRepository, usersRepository, cartItemRepository);
    }

    @Test
    @DisplayName("Verify removeItem deletes cart item successfully")
    void removeItem_CartExist_Success() {
        Product product1 = product(1L, "iphone 15", "iphone 15", BigDecimal.valueOf(1300), true, 20);
        Users user1 = createUser(
                id, "Jane", "Smith", "jane@example.com", "1234567890", UserRole.CUSTOMER,
                createAddress(10L, "221B Baker St", "London", "Greater London", "UK", "NW1")
        );
        CartItem cartItem1 = createCartItem(id, user1, product1, 5, BigDecimal.valueOf(6500));
        when(productRepository.findById(id)).thenReturn(Optional.of(product1));
        when(usersRepository.findById(id)).thenReturn(Optional.of(user1));
        when(cartItemRepository.findByUsersAndProduct(user1, product1)).thenReturn(Optional.of(cartItem1));

        Boolean response = cartItemService.removeItem(String.valueOf(id), id);

        assertThat(response).isTrue();

        verify(productRepository).findById(id);
        verify(usersRepository).findById(id);
        verify(cartItemRepository).findByUsersAndProduct(user1, product1);
        verify(cartItemRepository).delete(any(CartItem.class));
        verifyNoMoreInteractions(productRepository, usersRepository, cartItemRepository);
    }

    @Test
    @DisplayName("Verify getCartItems throws exception when user is not found")
    void getCartItems_UserNotFound() {
        when(usersRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartItemService.getCartItems(String.valueOf(id)))
                .isInstanceOf(ResourceNotFound.class)
                .hasMessageContaining("User does not exist with id: " + id);

        verify(usersRepository).findById(id);
        verifyNoMoreInteractions(usersRepository);
        verifyNoInteractions(cartItemRepository, cartMapper);
    }

    @Test
    @DisplayName("Verify getCartItems returns cart items successfully")
    void getCartItems_Success() {
        Users user1 = createUser(
                id, "Jane", "Smith", "jane@example.com", "1234567890", UserRole.CUSTOMER,
                createAddress(10L, "221B Baker St", "London", "Greater London", "UK", "NW1")
        );
        Product product1 = product(1L, "iphone 15", "iphone 15", BigDecimal.valueOf(1300), true, 20);
        CartResponse cartResponse = createCartResponse(String.valueOf(id), 5, BigDecimal.valueOf(6500));
        CartItem cartItem1 = createCartItem(id, user1, product1, 5, BigDecimal.valueOf(6500));
        when(usersRepository.findById(id)).thenReturn(Optional.of(user1));
        when(cartItemRepository.findByUsersId(id)).thenReturn(List.of(cartItem1));
        when(cartMapper.toDto(cartItem1)).thenReturn(cartResponse);

        List<CartResponse> responses = cartItemService.getCartItems(String.valueOf(id));

        assertThat(responses.get(0).getProductId()).isEqualTo("1");
        assertThat(responses.get(0).getQuantity()).isEqualTo(5);
        assertThat(responses.get(0).getPrice()).isEqualByComparingTo("6500");

        verify(usersRepository).findById(id);
        verify(cartItemRepository).findByUsersId(id);
        verify(cartMapper).toDto(any(CartItem.class));
        verifyNoMoreInteractions(usersRepository, cartItemRepository, cartMapper);
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

    private CartRequest createCartRequest(String productId, int quantity) {
        CartRequest cartRequest = new CartRequest();
        cartRequest.setQuantity(quantity);
        cartRequest.setProductId(productId);
        return cartRequest;
    }

    private CartResponse createCartResponse(String productId, Integer quantity, BigDecimal price) {
        CartResponse cartResponse = new CartResponse();
        cartResponse.setProductId(productId);
        cartResponse.setQuantity(quantity);
        cartResponse.setPrice(price);
        return cartResponse;
    }

    private Users createUser(Long id, String firstName, String lastName, String email,
                             String phone, UserRole role, Address address) {
        Users user = new Users();
        user.setId(id);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setRole(role);
        user.setAddress(address);
        return user;
    }

    private Address createAddress(Long id, String street, String city, String state, String country, String zipcode) {
        Address address = new Address();
        address.setId(id);
        address.setStreet(street);
        address.setCity(city);
        address.setState(state);
        address.setCountry(country);
        address.setZipcode(zipcode);
        return address;
    }

    private CartItem createCartItem(Long id, Users users, Product product, Integer quantity,
                                    BigDecimal price) {
        CartItem cartItem = new CartItem();
        cartItem.setId(id);
        cartItem.setUsers(users);
        cartItem.setProduct(product);
        cartItem.setQuantity(quantity);
        cartItem.setPrice(price);
        return cartItem;
    }
}