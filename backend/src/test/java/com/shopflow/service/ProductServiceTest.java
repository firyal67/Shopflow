package com.shopflow.service;

import com.shopflow.dto.request.ProductRequest;
import com.shopflow.dto.response.ProductResponse;
import com.shopflow.entity.Product;
import com.shopflow.entity.Role;
import com.shopflow.entity.User;
import com.shopflow.exception.ResourceNotFoundException;
import com.shopflow.exception.UnauthorizedException;
import com.shopflow.repository.CategoryRepository;
import com.shopflow.repository.ProductRepository;
import com.shopflow.repository.ReviewRepository;
import com.shopflow.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private UserRepository userRepository;
    @Mock private ReviewRepository reviewRepository;

    @InjectMocks
    private ProductService productService;

    private User seller;
    private Product product;

    @BeforeEach
    void setUp() {
        seller = User.builder()
                .id(1L).email("seller@test.com").role(Role.SELLER).actif(true).build();

        product = Product.builder()
                .id(1L).seller(seller).nom("T-Shirt").prix(new BigDecimal("29.99"))
                .stock(10).actif(true).build();
    }

    @Test
    void createProduct_shouldCreateSuccessfully() {
        ProductRequest request = new ProductRequest();
        request.setNom("T-Shirt");
        request.setPrix(new BigDecimal("29.99"));
        request.setStock(10);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(seller));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductResponse response = productService.createProduct(request, "seller@test.com");

        assertThat(response).isNotNull();
        assertThat(response.getNom()).isEqualTo("T-Shirt");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void getProductById_shouldReturnProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductResponse response = productService.getProductById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    void getProductById_shouldThrowException_whenNotFound() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deactivateProduct_shouldSetActifFalse() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(seller));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        productService.deactivateProduct(1L, "seller@test.com");

        verify(productRepository).save(argThat(p -> !p.isActif()));
    }

    @Test
    void deactivateProduct_shouldThrowException_whenNotOwner() {
        User otherSeller = User.builder()
                .id(2L).email("other@test.com").role(Role.SELLER).actif(true).build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(otherSeller));

        assertThatThrownBy(() -> productService.deactivateProduct(1L, "other@test.com"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void getTopSelling_shouldReturnTop10() {
        List<Product> products = List.of(product);
        when(productRepository.findTopSelling(any())).thenReturn(products);

        List<ProductResponse> result = productService.getTopSelling();

        assertThat(result).hasSize(1);
    }

    @Test
    void product_isEnPromotion_whenPrixPromoLowerThanPrix() {
        product.setPrixPromo(new BigDecimal("19.99"));

        assertThat(product.isEnPromotion()).isTrue();
        assertThat(product.getPourcentageRemise()).isGreaterThan(0);
    }

    @Test
    void product_isNotEnPromotion_whenNoPrixPromo() {
        assertThat(product.isEnPromotion()).isFalse();
        assertThat(product.getPourcentageRemise()).isEqualTo(0.0);
    }
}
