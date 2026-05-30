package com.shopflow.controller;

import com.shopflow.dto.request.CartItemRequest;
import com.shopflow.dto.response.CartResponse;
import com.shopflow.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@PreAuthorize("isAuthenticated()")
@Tag(name = "Panier", description = "Gestion du panier d'achat")
@SecurityRequirement(name = "bearerAuth")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping
    @Operation(summary = "Panier du client connecté")
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(cartService.getCart(userDetails.getUsername()));
    }

    @PostMapping("/items")
    @Operation(summary = "Ajouter un article")
    public ResponseEntity<CartResponse> addItem(
            @Valid @RequestBody CartItemRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(cartService.addItem(request, userDetails.getUsername()));
    }

    @PutMapping("/items/{itemId}")
    @Operation(summary = "Modifier la quantité d'un article")
    public ResponseEntity<CartResponse> updateItem(
            @PathVariable Long itemId,
            @RequestBody Map<String, Integer> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(cartService.updateItem(itemId, body.get("quantite"), userDetails.getUsername()));
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Retirer un article")
    public ResponseEntity<CartResponse> removeItem(
            @PathVariable Long itemId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(cartService.removeItem(itemId, userDetails.getUsername()));
    }

    @PostMapping("/coupon")
    @Operation(summary = "Appliquer un code promo")
    public ResponseEntity<CartResponse> applyCoupon(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(cartService.applyCoupon(body.get("code"), userDetails.getUsername()));
    }

    @DeleteMapping("/coupon")
    @Operation(summary = "Retirer le code promo")
    public ResponseEntity<CartResponse> removeCoupon(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(cartService.removeCoupon(userDetails.getUsername()));
    }
}
