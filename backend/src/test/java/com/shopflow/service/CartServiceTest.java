package com.shopflow.service;

import com.shopflow.dto.request.CartItemRequest;
import com.shopflow.dto.response.CartResponse;
import com.shopflow.entity.*;
import com.shopflow.exception.BusinessException;
import com.shopflow.exception.ResourceNotFoundException;
import com.shopflow.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private ProductRepository productRepository;
    @Mock private ProductVariantRepository variantRepository;
    @Mock private UserRepository userRepository;
    @Mock private CouponRepository couponRepository;

    @InjectMocks
    private CartService cartService;

    private User customer;
    private Product product;
    private Cart cart;

    @BeforeEach
    void setUp() {
        customer = User.builder().id(1L).email("client@test.com").role(Role.CUSTOMER).actif(true).build();
        product = Product.builder().id(1L).nom("T-Shirt").prix(new BigDecimal("29.99")).stock(10).actif(true).build();
        cart = Cart.builder().id(1L).customer(customer).build();
    }

    @Test
    void addItem_shouldAddProductToCart() {
        CartItemRequest request = new CartItemRequest();
        request.setProductId(1L);
        request.setQuantite(2);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(customer));
        when(cartRepository.findByCustomerId(anyLong())).thenReturn(Optional.of(cart));
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartIdAndProductIdAndVariantId(anyLong(), anyLong(), isNull()))
                .thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartResponse response = cartService.addItem(request, "client@test.com");

        assertThat(response).isNotNull();
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void addItem_shouldThrowException_whenStockInsuffisant() {
        CartItemRequest request = new CartItemRequest();
        request.setProductId(1L);
        request.setQuantite(20); // Plus que le stock (10)

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(customer));
        when(cartRepository.findByCustomerId(anyLong())).thenReturn(Optional.of(cart));
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> cartService.addItem(request, "client@test.com"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Stock insuffisant");
    }

    @Test
    void addItem_shouldThrowException_whenProductInactif() {
        product.setActif(false);
        CartItemRequest request = new CartItemRequest();
        request.setProductId(1L);
        request.setQuantite(1);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(customer));
        when(cartRepository.findByCustomerId(anyLong())).thenReturn(Optional.of(cart));
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> cartService.addItem(request, "client@test.com"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("plus disponible");
    }

    @Test
    void applyCoupon_shouldThrowException_whenCouponInvalid() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(customer));
        when(cartRepository.findByCustomerId(anyLong())).thenReturn(Optional.of(cart));
        when(couponRepository.findByCode(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.applyCoupon("INVALID", "client@test.com"))
                .isInstanceOf(BusinessException.class);
    }
}
