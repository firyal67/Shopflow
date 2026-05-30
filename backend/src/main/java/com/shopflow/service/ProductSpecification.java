package com.shopflow.service;

import com.shopflow.entity.Product;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class ProductSpecification {

    public static Specification<Product> isActive() {
        return (root, query, cb) -> cb.isTrue(root.get("actif"));
    }

    public static Specification<Product> hasCategory(Long categoryId) {
        return (root, query, cb) -> {
            var categories = root.join("categories");
            return cb.equal(categories.get("id"), categoryId);
        };
    }

    public static Specification<Product> prixGreaterThan(BigDecimal min) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("prix"), min);
    }

    public static Specification<Product> prixLessThan(BigDecimal max) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("prix"), max);
    }

    public static Specification<Product> hasSeller(Long sellerId) {
        return (root, query, cb) -> cb.equal(root.get("seller").get("id"), sellerId);
    }

    public static Specification<Product> isEnPromotion() {
        return (root, query, cb) -> cb.and(
                cb.isNotNull(root.get("prixPromo")),
                cb.greaterThan(root.get("prixPromo"), BigDecimal.ZERO),
                cb.lessThan(root.get("prixPromo"), root.get("prix"))
        );
    }
}
