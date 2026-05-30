package com.shopflow.repository;

import com.shopflow.entity.Order;
import com.shopflow.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByCustomerId(Long customerId, Pageable pageable);

    Optional<Order> findByNumeroCommande(String numeroCommande);

    Page<Order> findByStatut(OrderStatus statut, Pageable pageable);

    // Commandes d'un vendeur (via les lignes de commande)
    @Query("SELECT DISTINCT o FROM Order o JOIN o.lignes li WHERE li.product.seller.id = :sellerId")
    Page<Order> findBySellerIdViaItems(@Param("sellerId") Long sellerId, Pageable pageable);

    // Chiffre d'affaires global
    @Query("SELECT COALESCE(SUM(o.totalTTC), 0) FROM Order o WHERE o.statut NOT IN ('CANCELLED', 'REFUNDED')")
    BigDecimal getTotalRevenue();

    // Chiffre d'affaires d'un vendeur
    @Query("SELECT COALESCE(SUM(li.prixUnitaire * li.quantite), 0) FROM OrderItem li WHERE li.product.seller.id = :sellerId AND li.order.statut NOT IN ('CANCELLED', 'REFUNDED')")
    BigDecimal getRevenueForSeller(@Param("sellerId") Long sellerId);

    // Commandes récentes
    List<Order> findTop10ByOrderByDateCommandeDesc();

    // Commandes en attente pour un vendeur
    @Query("SELECT DISTINCT o FROM Order o JOIN o.lignes li WHERE li.product.seller.id = :sellerId AND o.statut = 'PENDING'")
    List<Order> findPendingOrdersForSeller(@Param("sellerId") Long sellerId);

    long countByStatut(OrderStatus statut);
}
