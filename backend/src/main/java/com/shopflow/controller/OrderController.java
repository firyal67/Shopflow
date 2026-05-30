package com.shopflow.controller;

import com.shopflow.dto.request.OrderRequest;
import com.shopflow.dto.response.OrderResponse;
import com.shopflow.entity.OrderStatus;
import com.shopflow.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Commandes", description = "Gestion des commandes")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Passer une commande depuis le panier")
    public ResponseEntity<OrderResponse> placeOrder(
            @Valid @RequestBody OrderRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.placeOrder(request, userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détail d'une commande")
    public ResponseEntity<OrderResponse> getOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(orderService.getOrderById(id, userDetails.getUsername()));
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Commandes du client connecté")
    public ResponseEntity<Page<OrderResponse>> getMyOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(orderService.getMyOrders(userDetails.getUsername(), pageable));
    }

    @GetMapping("/seller")
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "Commandes reçues par le vendeur connecté")
    public ResponseEntity<Page<OrderResponse>> getSellerOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(orderService.getSellerOrders(userDetails.getUsername(), pageable));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Toutes les commandes (ADMIN)")
    public ResponseEntity<Page<OrderResponse>> getAllOrders(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(orderService.getAllOrders(pageable));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @Operation(summary = "Mettre à jour le statut")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        OrderStatus status = OrderStatus.valueOf(body.get("statut"));
        return ResponseEntity.ok(orderService.updateStatus(id, status, userDetails.getUsername()));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Annuler une commande")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(orderService.cancelOrder(id, userDetails.getUsername()));
    }
}
