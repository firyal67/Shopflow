package com.shopflow.repository;

import com.shopflow.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByProductIdAndApprouveTrue(Long productId, Pageable pageable);

    Optional<Review> findByCustomerIdAndProductId(Long customerId, Long productId);

    boolean existsByCustomerIdAndProductId(Long customerId, Long productId);

    @Query("SELECT AVG(r.note) FROM Review r WHERE r.product.id = :productId AND r.approuve = true")
    Double calculateAverageRating(@Param("productId") Long productId);

    // Vérifie si le client a acheté le produit
    @Query("SELECT COUNT(oi) > 0 FROM OrderItem oi WHERE oi.product.id = :productId AND oi.order.customer.id = :customerId AND oi.order.statut = 'DELIVERED'")
    boolean hasCustomerPurchasedProduct(@Param("customerId") Long customerId, @Param("productId") Long productId);

    Page<Review> findByApprouveFalse(Pageable pageable);

    Page<Review> findByCustomerId(Long customerId, Pageable pageable);
}
