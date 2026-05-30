package com.shopflow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"categories", "variants", "reviews"})
@ToString(exclude = {"categories", "variants", "reviews"})
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Column(nullable = false)
    private String nom;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal prix;

    @Column(precision = 10, scale = 2)
    private BigDecimal prixPromo;

    @Builder.Default
    private Integer stock = 0;

    @Builder.Default
    private boolean actif = true;

    @Column(updatable = false)
    private LocalDateTime dateCreation;

    // Images stockées comme liste de URLs
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url", columnDefinition = "TEXT")
    @Builder.Default
    private List<String> images = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "product_categories",
        joinColumns = @JoinColumn(name = "product_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    @Builder.Default
    private Set<Category> categories = new HashSet<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<ProductVariant> variants = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();

    @Builder.Default
    private Integer totalVentes = 0;

    @Builder.Default
    private Double noteMoyenne = 0.0;

    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
    }

    public boolean isEnPromotion() {
        return prixPromo != null && prixPromo.compareTo(BigDecimal.ZERO) > 0 && prixPromo.compareTo(prix) < 0;
    }

    public Double getPourcentageRemise() {
        if (!isEnPromotion()) return 0.0;
        return (1 - prixPromo.doubleValue() / prix.doubleValue()) * 100;
    }
}
