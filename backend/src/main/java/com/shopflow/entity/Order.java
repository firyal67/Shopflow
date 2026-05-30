package com.shopflow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OrderStatus statut = OrderStatus.PENDING;

    @Column(unique = true, nullable = false)
    private String numeroCommande;

    // Snapshot de l'adresse au moment de la commande
    private String adresseLivraison;

    @Column(precision = 10, scale = 2)
    private BigDecimal sousTotal;

    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal fraisLivraison = BigDecimal.ZERO;

    @Column(name = "total_ttc", precision = 10, scale = 2)
    private BigDecimal totalTTC;

    @Column(updatable = false)
    private LocalDateTime dateCommande;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderItem> lignes = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;

    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal remiseCoupon = BigDecimal.ZERO;

    @Builder.Default
    private boolean isNew = true;

    @PrePersist
    protected void onCreate() {
        dateCommande = LocalDateTime.now();
    }
}
