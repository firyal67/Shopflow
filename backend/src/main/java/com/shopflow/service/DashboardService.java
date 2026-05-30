package com.shopflow.service;

import com.shopflow.dto.response.DashboardAdminResponse;
import com.shopflow.dto.response.DashboardSellerResponse;
import com.shopflow.dto.response.OrderResponse;
import com.shopflow.dto.response.ProductResponse;
import com.shopflow.entity.OrderStatus;
import com.shopflow.entity.User;
import com.shopflow.exception.ResourceNotFoundException;
import com.shopflow.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductService productService;
    private final OrderService orderService;

    @Transactional(readOnly = true)
    public DashboardAdminResponse getAdminDashboard() {
        List<ProductResponse> topProduits = productRepository
                .findTopSelling(PageRequest.of(0, 5))
                .stream().map(productService::toResponse).collect(Collectors.toList());

        List<OrderResponse> commandesRecentes = orderRepository
                .findTop10ByOrderByDateCommandeDesc()
                .stream().map(orderService::toResponse).collect(Collectors.toList());

        return DashboardAdminResponse.builder()
                .chiffreAffairesGlobal(orderRepository.getTotalRevenue())
                .totalCommandes(orderRepository.count())
                .commandesEnAttente(orderRepository.countByStatut(OrderStatus.PENDING))
                .totalUtilisateurs(userRepository.count())
                .totalProduits(productRepository.count())
                .topProduits(topProduits)
                .commandesRecentes(commandesRecentes)
                .build();
    }

    @Transactional(readOnly = true)
    public DashboardSellerResponse getSellerDashboard(String email) {
        User seller = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Vendeur introuvable"));

        List<OrderResponse> commandesRecentes = orderRepository
                .findPendingOrdersForSeller(seller.getId())
                .stream().map(orderService::toResponse).collect(Collectors.toList());

        // Produits avec stock faible (< 5)
        List<ProductResponse> alertesStock = productRepository
                .findBySellerIdAndActifTrue(seller.getId(), PageRequest.of(0, 100))
                .stream()
                .filter(p -> p.getStock() < 5)
                .map(productService::toResponse)
                .collect(Collectors.toList());

        return DashboardSellerResponse.builder()
                .revenus(orderRepository.getRevenueForSeller(seller.getId()))
                .totalCommandes(orderRepository.findBySellerIdViaItems(seller.getId(), PageRequest.of(0, 1)).getTotalElements())
                .commandesEnAttente((long) commandesRecentes.size())
                .commandesRecentes(commandesRecentes)
                .alertesStockFaible(alertesStock)
                .build();
    }
}
