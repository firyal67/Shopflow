package com.shopflow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "product_variants")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // ex: "Taille", "Couleur"
    @Column(nullable = false)
    private String attribut;

    // ex: "M", "Rouge"
    @Column(nullable = false)
    private String valeur;

    @Builder.Default
    private Integer stockSupplementaire = 0;

    // Prix additionnel par rapport au prix de base
    @Builder.Default
    @Column(precision = 10, scale = 2)
    private BigDecimal prixDelta = BigDecimal.ZERO;
}
