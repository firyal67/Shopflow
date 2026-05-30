package com.shopflow.repository;

import com.shopflow.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    Page<Product> findByActifTrue(Pageable pageable);

    Page<Product> findBySellerIdAndActifTrue(Long sellerId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.actif = true AND " +
           "(LOWER(p.nom) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Product> searchFullText(@Param("q") String q, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.actif = true ORDER BY p.totalVentes DESC")
    List<Product> findTopSelling(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.actif = true AND p.prixPromo IS NOT NULL AND p.prixPromo > 0 AND p.prixPromo < p.prix")
    Page<Product> findEnPromotion(Pageable pageable);
}
