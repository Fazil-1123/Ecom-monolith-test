package com.ecom.monolith.service;

import com.ecom.monolith.Dto.OrderItemResponse;
import com.ecom.monolith.Dto.OrderResponse;
import com.ecom.monolith.Mapper.OrderMapper;
import com.ecom.monolith.exception.ResourceNotFound;
import com.ecom.monolith.model.*;
import com.ecom.monolith.repositories.CartItemRepository;
import com.ecom.monolith.repositories.OrderRepository;
import com.ecom.monolith.repositories.UsersRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the OrderServiceImpl class.
 * This class verifies the service methods for managing orders.
 */
@ExtendWith(MockitoExtension.class)
public class OrderServiceImplTest {

    @Mock
    CartItemRepository cartItemRepository;

    @Mock
    UsersRepository usersRepository;

    @Mock
    OrderRepository orderRepository;

    @Mock
    OrderMapper orderMapper;

    @InjectMocks
    OrderServiceImpl orderService;

    private static final Long id = 1L;

    @Test
    @DisplayName("Verify placeOrder throws exception when user is not found")
    void placeOrder_UserNotFound() {
        when(usersRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.placeOrder(id.toString()))
                .isInstanceOf(ResourceNotFound.class)
                .hasMessageContaining("User does not exist with id: " + id);

        verify(usersRepository).findById(id);
        verifyNoMoreInteractions(usersRepository);
        verifyNoInteractions(cartItemRepository, orderRepository, orderMapper);
    }

    @Test
    @DisplayName("Verify placeOrder throws exception when cart is empty")
    void placeOrder_UserExist_CartEmpty() {
        Users user1 = createUser(
                id, "Jane", "Smith", "jane@example.com", "1234567890", UserRole.CUSTOMER,
                createAddress(10L, "221B Baker St", "London", "Greater London", "UK", "NW1")
        );

        when(usersRepository.findById(id)).thenReturn(Optional.of(user1));
        when(cartItemRepository.findByUsersId(id)).thenReturn(List.of());

        assertThatThrownBy(() -> orderService.placeOrder(id.toString()))
                .isInstanceOf(ResourceNotFound.class)
                .hasMessageContaining("User doesn't have any items in cart");

        verify(usersRepository).findById(id);
        verify(cartItemRepository).findByUsersId(id);
        verifyNoMoreInteractions(usersRepository, cartItemRepository);
        verifyNoInteractions(orderRepository, orderMapper);
    }

    @Test
    @DisplayName("Verify placeOrder creates order successfully when cart exists")
    void placeOrder_UserExist_CartExist() {
        Users user1 = createUser(
                id, "Jane", "Smith", "jane@example.com", "1234567890", UserRole.CUSTOMER,
                createAddress(10L, "221B Baker St", "London", "Greater London", "UK", "NW1")
        );
        Product product1 = product(1L, "iphone 15", "iphone 15", BigDecimal.valueOf(1300), true, 10);
        Product product2 = product(2L, "iphone 16", "iphone 16", BigDecimal.valueOf(1500), true, 10);
        CartItem cartItem1 = createCartItem(id, user1, product1, 5, BigDecimal.valueOf(6500));
        CartItem cartItem2 = createCartItem(2L, user1, product2, 5, BigDecimal.valueOf(7500));

        when(usersRepository.findById(id)).thenReturn(Optional.of(user1));
        when(cartItemRepository.findByUsersId(id)).thenReturn(List.of(cartItem1, cartItem2));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0, Order.class));
        when(orderMapper.toDto(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0, Order.class);
            OrderResponse response = new OrderResponse();
            response.setId(o.getId());
            response.setStatus(o.getStatus());
            response.setTotalAmount(o.getTotalAmount());
            List<OrderItemResponse> orderItemResponseList = new ArrayList<>();
            for (OrderItem orderItem : o.getItems()) {
                OrderItemResponse orderItemResponse = new OrderItemResponse();
                orderItemResponse.setId(orderItem.getId());
                orderItemResponse.setQuantity(orderItem.getQuantity());
                orderItemResponse.setPrice(orderItem.getPrice());
                orderItemResponse.setProductId(orderItem.getProduct().getId());
                orderItemResponseList.add(orderItemResponse);
            }
            response.setItems(orderItemResponseList);
            return response;
        });

        OrderResponse finalOrder = orderService.placeOrder(id.toString());

        assertThat(finalOrder.getStatus()).isEqualTo(OrderStatus.ORDERED);
        assertThat(finalOrder.getTotalAmount()).isEqualByComparingTo("14000");

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        Order savedOrder = captor.getValue();

        assertThat(savedOrder.getItems().get(0).getProduct().getName()).isEqualTo("iphone 15");
        assertThat(savedOrder.getItems().get(1).getProduct().getName()).isEqualTo("iphone 16");

        verify(usersRepository).findById(id);
        verify(cartItemRepository).findByUsersId(id);
        verify(cartItemRepository).deleteAll(anyList());
        verify(orderMapper).toDto(any(Order.class));
        verifyNoMoreInteractions(usersRepository, cartItemRepository, orderRepository, orderMapper);
    }

    @Test
    @DisplayName("Verify placeOrder handles null prices in cart items")
    void placeOrder_UserExist_CartExistAndPriceNull() {
        Users user1 = createUser(
                id, "Jane", "Smith", "jane@example.com", "1234567890", UserRole.CUSTOMER,
                createAddress(10L, "221B Baker St", "London", "Greater London", "UK", "NW1")
        );
        Product product1 = product(1L, "iphone 15", "iphone 15", null, true, 10);
        Product product2 = product(2L, "iphone 16", "iphone 16", null, true, 10);
        CartItem cartItem1 = createCartItem(id, user1, product1, 5, null);
        CartItem cartItem2 = createCartItem(2L, user1, product2, 5, null);

        when(usersRepository.findById(id)).thenReturn(Optional.of(user1));
        when(cartItemRepository.findByUsersId(id)).thenReturn(List.of(cartItem1, cartItem2));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0, Order.class));
        when(orderMapper.toDto(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0, Order.class);
            OrderResponse response = new OrderResponse();
            response.setId(o.getId());
            response.setStatus(o.getStatus());
            response.setTotalAmount(o.getTotalAmount());
            List<OrderItemResponse> orderItemResponseList = new ArrayList<>();
            for (OrderItem orderItem : o.getItems()) {
                OrderItemResponse orderItemResponse = new OrderItemResponse();
                orderItemResponse.setId(orderItem.getId());
                orderItemResponse.setQuantity(orderItem.getQuantity());
                orderItemResponse.setPrice(orderItem.getPrice());
                orderItemResponse.setProductId(orderItem.getProduct().getId());
                orderItemResponseList.add(orderItemResponse);
            }
            response.setItems(orderItemResponseList);
            return response;
        });

        OrderResponse finalOrder = orderService.placeOrder(id.toString());

        assertThat(finalOrder.getStatus()).isEqualTo(OrderStatus.ORDERED);
        assertThat(finalOrder.getTotalAmount()).isEqualByComparingTo("0");

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        Order savedOrder = captor.getValue();

        assertThat(savedOrder.getItems().get(0).getProduct().getName()).isEqualTo("iphone 15");
        assertThat(savedOrder.getItems().get(1).getProduct().getName()).isEqualTo("iphone 16");

        verify(usersRepository).findById(id);
        verify(cartItemRepository).findByUsersId(id);
        verify(cartItemRepository).deleteAll(anyList());
        verify(orderMapper).toDto(any(Order.class));
        verifyNoMoreInteractions(usersRepository, cartItemRepository, orderRepository, orderMapper);
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
}