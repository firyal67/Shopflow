package com.shopflow.service;

import com.shopflow.dto.request.CartItemRequest;
import com.shopflow.dto.response.CartItemResponse;
import com.shopflow.dto.response.CartResponse;
import com.shopflow.entity.*;
import com.shopflow.exception.BusinessException;
import com.shopflow.exception.ResourceNotFoundException;
import com.shopflow.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final UserRepository userRepository;
    private final CouponRepository couponRepository;

    private static final BigDecimal FRAIS_LIVRAISON = new BigDecimal("5.99");
    private static final BigDecimal SEUIL_LIVRAISON_GRATUITE = new BigDecimal("50.00");

    @Transactional(readOnly = true)
    public CartResponse getCart(String email) {
        User customer = getUser(email);
        Cart cart = getOrCreateCart(customer);
        return toResponse(cart);
    }

    @Transactional
    public CartResponse addItem(CartItemRequest request, String email) {
        User customer = getUser(email);
        Cart cart = getOrCreateCart(customer);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Produit", request.getProductId()));

        if (!product.isActif()) {
            throw new BusinessException("Ce produit n'est plus disponible");
        }

        ProductVariant variant = null;
        if (request.getVariantId() != null) {
            variant = variantRepository.findById(request.getVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Variante", request.getVariantId()));
        }

        // Vérifier le stock
        int stockDispo = getStockDisponible(product, variant);
        if (request.getQuantite() > stockDispo) {
            throw new BusinessException("Stock insuffisant. Disponible : " + stockDispo);
        }

        // Chercher si l'article existe déjà dans le panier
        Long variantId = variant != null ? variant.getId() : null;
        var existingItem = cartItemRepository.findByCartIdAndProductIdAndVariantId(
                cart.getId(), product.getId(), variantId);

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int newQty = item.getQuantite() + request.getQuantite();
            if (newQty > stockDispo) {
                throw new BusinessException("Stock insuffisant. Disponible : " + stockDispo);
            }
            item.setQuantite(newQty);
            cartItemRepository.save(item);
        } else {
            CartItem item = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .variant(variant)
                    .quantite(request.getQuantite())
                    .build();
            cart.getLignes().add(item);
        }

        cartRepository.save(cart);
        return toResponse(cart);
    }

    @Transactional
    public CartResponse updateItem(Long itemId, Integer quantite, String email) {
        User customer = getUser(email);
        Cart cart = getOrCreateCart(customer);

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Article panier", itemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new BusinessException("Cet article n'appartient pas à votre panier");
        }

        if (quantite <= 0) {
            cart.getLignes().remove(item);
            cartItemRepository.delete(item);
        } else {
            int stockDispo = getStockDisponible(item.getProduct(), item.getVariant());
            if (quantite > stockDispo) {
                throw new BusinessException("Stock insuffisant. Disponible : " + stockDispo);
            }
            item.setQuantite(quantite);
            cartItemRepository.save(item);
        }

        return toResponse(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse removeItem(Long itemId, String email) {
        User customer = getUser(email);
        Cart cart = getOrCreateCart(customer);

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Article panier", itemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new BusinessException("Cet article n'appartient pas à votre panier");
        }

        cart.getLignes().remove(item);
        cartItemRepository.delete(item);
        return toResponse(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse applyCoupon(String code, String email) {
        User customer = getUser(email);
        Cart cart = getOrCreateCart(customer);

        Coupon coupon = couponRepository.findByCode(code)
                .orElseThrow(() -> new BusinessException("Code promo invalide : " + code));

        validateCoupon(coupon);
        cart.setCoupon(coupon);
        return toResponse(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse removeCoupon(String email) {
        User customer = getUser(email);
        Cart cart = getOrCreateCart(customer);
        cart.setCoupon(null);
        return toResponse(cartRepository.save(cart));
    }

    private void validateCoupon(Coupon coupon) {
        if (!coupon.isActif()) throw new BusinessException("Ce coupon est inactif");
        if (coupon.getDateExpiration() != null &&
                coupon.getDateExpiration().isBefore(java.time.LocalDateTime.now())) {
            throw new BusinessException("Ce coupon a expiré");
        }
        if (coupon.getUsagesMax() != null && coupon.getUsagesActuels() >= coupon.getUsagesMax()) {
            throw new BusinessException("Ce coupon a atteint son nombre maximum d'utilisations");
        }
    }

    private int getStockDisponible(Product product, ProductVariant variant) {
        if (variant != null) {
            return product.getStock() + variant.getStockSupplementaire();
        }
        return product.getStock();
    }

    private Cart getOrCreateCart(User customer) {
        return cartRepository.findByCustomerId(customer.getId())
                .orElseGet(() -> {
                    Cart newCart = Cart.builder().customer(customer).build();
                    return cartRepository.save(newCart);
                });
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
    }

    public CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cart.getLignes().stream()
                .map(this::toItemResponse)
                .collect(Collectors.toList());

        BigDecimal sousTotal = items.stream()
                .map(CartItemResponse::getSousTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal fraisLivraison = sousTotal.compareTo(SEUIL_LIVRAISON_GRATUITE) >= 0
                ? BigDecimal.ZERO : FRAIS_LIVRAISON;

        BigDecimal remise = BigDecimal.ZERO;
        String couponCode = null;
        if (cart.getCoupon() != null) {
            couponCode = cart.getCoupon().getCode();
            remise = calculerRemise(sousTotal, cart.getCoupon());
        }

        BigDecimal total = sousTotal.add(fraisLivraison).subtract(remise).max(BigDecimal.ZERO);

        return CartResponse.builder()
                .id(cart.getId())
                .lignes(items)
                .sousTotal(sousTotal.setScale(2, RoundingMode.HALF_UP))
                .fraisLivraison(fraisLivraison)
                .remiseCoupon(remise.setScale(2, RoundingMode.HALF_UP))
                .totalTTC(total.setScale(2, RoundingMode.HALF_UP))
                .couponCode(couponCode)
                .dateModification(cart.getDateModification())
                .build();
    }

    private BigDecimal calculerRemise(BigDecimal sousTotal, Coupon coupon) {
        if (coupon.getType() == CouponType.PERCENT) {
            return sousTotal.multiply(coupon.getValeur()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            return coupon.getValeur().min(sousTotal);
        }
    }

    private CartItemResponse toItemResponse(CartItem item) {
        Product p = item.getProduct();
        BigDecimal prix = p.isEnPromotion() ? p.getPrixPromo() : p.getPrix();
        if (item.getVariant() != null) {
            prix = prix.add(item.getVariant().getPrixDelta());
        }
        BigDecimal sousTotal = prix.multiply(BigDecimal.valueOf(item.getQuantite()));

        String image = p.getImages() != null && !p.getImages().isEmpty() ? p.getImages().get(0) : null;

        return CartItemResponse.builder()
                .id(item.getId())
                .productId(p.getId())
                .productNom(p.getNom())
                .productImage(image)
                .prixUnitaire(prix)
                .variantId(item.getVariant() != null ? item.getVariant().getId() : null)
                .variantAttribut(item.getVariant() != null ? item.getVariant().getAttribut() : null)
                .variantValeur(item.getVariant() != null ? item.getVariant().getValeur() : null)
                .quantite(item.getQuantite())
                .sousTotal(sousTotal)
                .stockDisponible(getStockDisponible(p, item.getVariant()))
                .build();
    }
}
