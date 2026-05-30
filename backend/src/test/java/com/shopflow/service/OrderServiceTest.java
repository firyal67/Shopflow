package com.shopflow.service;

import com.shopflow.dto.response.OrderResponse;
import com.shopflow.entity.*;
import com.shopflow.exception.BusinessException;
import com.shopflow.exception.UnauthorizedException;
import com.shopflow.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private ProductRepository productRepository;
    @Mock private AddressRepository addressRepository;
    @Mock private UserRepository userRepository;
    @Mock private CouponRepository couponRepository;
    @Mock private CartService cartService;

    @InjectMocks
    private OrderService orderService;

    private User customer;
    private Order order;

    @BeforeEach
    void setUp() {
        customer = User.builder().id(1L).email("client@test.com").role(Role.CUSTOMER).actif(true).build();
        order = Order.builder()
                .id(1L)
                .customer(customer)
                .numeroCommande("ORD-2026-00001")
                .statut(OrderStatus.PENDING)
                .sousTotal(new BigDecimal("99.99"))
                .fraisLivraison(new BigDecimal("5.99"))
                .remiseCoupon(BigDecimal.ZERO)
                .totalTTC(new BigDecimal("105.98"))
                .lignes(new ArrayList<>())
                .build();
    }

    @Test
    void cancelOrder_shouldCancelPendingOrder() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(customer));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderResponse response = orderService.cancelOrder(1L, "client@test.com");

        assertThat(response).isNotNull();
        verify(orderRepository).save(argThat(o -> o.getStatut() == OrderStatus.CANCELLED));
    }

    @Test
    void cancelOrder_shouldThrow_whenOrderAlreadyShipped() {
        order.setStatut(OrderStatus.SHIPPED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(customer));

        assertThatThrownBy(() -> orderService.cancelOrder(1L, "client@test.com"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Impossible d'annuler");
    }

    @Test
    void cancelOrder_shouldRefund_whenOrderPaid() {
        order.setStatut(OrderStatus.PAID);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(customer));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        orderService.cancelOrder(1L, "client@test.com");

        verify(orderRepository).save(argThat(o -> o.getStatut() == OrderStatus.REFUNDED));
    }

    @Test
    void cancelOrder_shouldThrow_whenCustomerNotOwner() {
        User otherUser = User.builder().id(2L).email("other@test.com").role(Role.CUSTOMER).build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(otherUser));

        assertThatThrownBy(() -> orderService.cancelOrder(1L, "other@test.com"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void updateStatus_shouldAllowAdminAnyTransition() {
        User admin = User.builder().id(3L).email("admin@test.com").role(Role.ADMIN).build();
        order.setStatut(OrderStatus.DELIVERED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(admin));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        assertThatNoException().isThrownBy(() ->
                orderService.updateStatus(1L, OrderStatus.CANCELLED, "admin@test.com"));
    }

    @Test
    void updateStatus_shouldThrow_whenInvalidTransition() {
        User seller = User.builder().id(4L).email("seller@test.com").role(Role.SELLER).build();
        order.setStatut(OrderStatus.DELIVERED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(seller));

        assertThatThrownBy(() ->
                orderService.updateStatus(1L, OrderStatus.PENDING, "seller@test.com"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Transition de statut invalide");
    }
}
