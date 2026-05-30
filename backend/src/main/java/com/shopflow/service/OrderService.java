package com.shopflow.service;

import com.shopflow.dto.request.OrderRequest;
import com.shopflow.dto.response.CartResponse;
import com.shopflow.dto.response.OrderItemResponse;
import com.shopflow.dto.response.OrderResponse;
import com.shopflow.entity.*;
import com.shopflow.exception.BusinessException;
import com.shopflow.exception.ResourceNotFoundException;
import com.shopflow.exception.UnauthorizedException;
import com.shopflow.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final CouponRepository couponRepository;
    private final CartService cartService;

    @Transactional
    public OrderResponse placeOrder(OrderRequest request, String email) {
        User customer = getUser(email);

        Cart cart = cartRepository.findByCustomerId(customer.getId())
                .orElseThrow(() -> new BusinessException("Votre panier est vide"));

        if (cart.getLignes().isEmpty()) {
            throw new BusinessException("Votre panier est vide");
        }

        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("Adresse", request.getAddressId()));

        if (!address.getUser().getId().equals(customer.getId())) {
            throw new UnauthorizedException("Cette adresse ne vous appartient pas");
        }

        // Vérification finale du stock et calcul
        CartResponse cartResponse = cartService.toResponse(cart);

        Order order = Order.builder()
                .customer(customer)
                .numeroCommande(generateOrderNumber())
                .adresseLivraison(formatAddress(address))
                .sousTotal(cartResponse.getSousTotal())
                .fraisLivraison(cartResponse.getFraisLivraison())
                .remiseCoupon(cartResponse.getRemiseCoupon())
                .totalTTC(cartResponse.getTotalTTC())
                .coupon(cart.getCoupon())
                .build();

        // Créer les lignes de commande et décrémenter le stock
        for (var cartItem : cart.getLignes()) {
            Product product = cartItem.getProduct();
            ProductVariant variant = cartItem.getVariant();

            int stockDispo = variant != null
                    ? product.getStock() + variant.getStockSupplementaire()
                    : product.getStock();

            if (cartItem.getQuantite() > stockDispo) {
                throw new BusinessException("Stock insuffisant pour : " + product.getNom());
            }

            // Décrémenter le stock
            if (variant != null) {
                int toDeduct = Math.min(cartItem.getQuantite(), variant.getStockSupplementaire());
                variant.setStockSupplementaire(variant.getStockSupplementaire() - toDeduct);
                int remaining = cartItem.getQuantite() - toDeduct;
                if (remaining > 0) {
                    product.setStock(product.getStock() - remaining);
                }
            } else {
                product.setStock(product.getStock() - cartItem.getQuantite());
            }

            // Incrémenter les ventes
            product.setTotalVentes(product.getTotalVentes() + cartItem.getQuantite());
            productRepository.save(product);

            BigDecimal prixUnit = product.isEnPromotion() ? product.getPrixPromo() : product.getPrix();
            if (variant != null) prixUnit = prixUnit.add(variant.getPrixDelta());

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .variant(variant)
                    .quantite(cartItem.getQuantite())
                    .prixUnitaire(prixUnit)
                    .build();
            order.getLignes().add(orderItem);
        }

        // Incrémenter l'usage du coupon
        if (cart.getCoupon() != null) {
            Coupon coupon = cart.getCoupon();
            coupon.setUsagesActuels(coupon.getUsagesActuels() + 1);
            couponRepository.save(coupon);
        }

        Order savedOrder = orderRepository.save(order);

        // Vider le panier
        cart.getLignes().clear();
        cart.setCoupon(null);
        cartRepository.save(cart);

        return toResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id, String email) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande", id));

        User user = getUser(email);
        // Seul le client, le vendeur concerné ou l'admin peut voir la commande
        if (user.getRole() == Role.CUSTOMER && !order.getCustomer().getId().equals(user.getId())) {
            throw new UnauthorizedException("Accès refusé");
        }

        // Marquer comme lu
        if (order.isNew()) {
            order.setNew(false);
            orderRepository.save(order);
        }

        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getMyOrders(String email, Pageable pageable) {
        User customer = getUser(email);
        return orderRepository.findByCustomerId(customer.getId(), pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getSellerOrders(String email, Pageable pageable) {
        User seller = getUser(email);
        return orderRepository.findBySellerIdViaItems(seller.getId(), pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatus newStatus, String email) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande", id));

        User user = getUser(email);

        // Validation des transitions de statut
        validateStatusTransition(order.getStatut(), newStatus, user.getRole());

        order.setStatut(newStatus);
        order.setNew(true); // Notifier le client
        return toResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse cancelOrder(Long id, String email) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande", id));

        User user = getUser(email);

        if (user.getRole() == Role.CUSTOMER && !order.getCustomer().getId().equals(user.getId())) {
            throw new UnauthorizedException("Vous ne pouvez pas annuler cette commande");
        }

        if (order.getStatut() != OrderStatus.PENDING && order.getStatut() != OrderStatus.PAID) {
            throw new BusinessException("Impossible d'annuler une commande avec le statut : " + order.getStatut());
        }

        // Remettre le stock
        for (OrderItem item : order.getLignes()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantite());
            product.setTotalVentes(Math.max(0, product.getTotalVentes() - item.getQuantite()));
            productRepository.save(product);
        }

        OrderStatus newStatus = order.getStatut() == OrderStatus.PAID
                ? OrderStatus.REFUNDED : OrderStatus.CANCELLED;
        order.setStatut(newStatus);

        return toResponse(orderRepository.save(order));
    }

    private void validateStatusTransition(OrderStatus current, OrderStatus next, Role role) {
        if (role == Role.ADMIN) return; // L'admin peut tout faire

        // Transitions autorisées pour SELLER
        boolean valid = switch (current) {
            case PENDING -> next == OrderStatus.PAID || next == OrderStatus.CANCELLED;
            case PAID -> next == OrderStatus.PROCESSING || next == OrderStatus.REFUNDED;
            case PROCESSING -> next == OrderStatus.SHIPPED;
            case SHIPPED -> next == OrderStatus.DELIVERED;
            default -> false;
        };

        if (!valid) {
            throw new BusinessException("Transition de statut invalide : " + current + " -> " + next);
        }
    }

    private String generateOrderNumber() {
        String year = String.valueOf(LocalDateTime.now().getYear());
        String random = String.format("%05d", new Random().nextInt(100000));
        return "ORD-" + year + "-" + random;
    }

    private String formatAddress(Address address) {
        return address.getRue() + ", " + address.getCodePostal() + " " + address.getVille() + ", " + address.getPays();
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
    }

    public OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getLignes().stream()
                .map(this::toItemResponse)
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .numeroCommande(order.getNumeroCommande())
                .statut(order.getStatut())
                .adresseLivraison(order.getAdresseLivraison())
                .sousTotal(order.getSousTotal())
                .fraisLivraison(order.getFraisLivraison())
                .remiseCoupon(order.getRemiseCoupon())
                .totalTTC(order.getTotalTTC())
                .dateCommande(order.getDateCommande())
                .lignes(items)
                .isNew(order.isNew())
                .couponCode(order.getCoupon() != null ? order.getCoupon().getCode() : null)
                .build();
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        Product p = item.getProduct();
        String image = p.getImages() != null && !p.getImages().isEmpty() ? p.getImages().get(0) : null;
        BigDecimal sousTotal = item.getPrixUnitaire().multiply(BigDecimal.valueOf(item.getQuantite()));

        return OrderItemResponse.builder()
                .id(item.getId())
                .productId(p.getId())
                .productNom(p.getNom())
                .productImage(image)
                .variantId(item.getVariant() != null ? item.getVariant().getId() : null)
                .variantAttribut(item.getVariant() != null ? item.getVariant().getAttribut() : null)
                .variantValeur(item.getVariant() != null ? item.getVariant().getValeur() : null)
                .quantite(item.getQuantite())
                .prixUnitaire(item.getPrixUnitaire())
                .sousTotal(sousTotal)
                .build();
    }
}
