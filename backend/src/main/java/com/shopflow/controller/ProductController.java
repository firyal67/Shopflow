package com.shopflow.controller;

import com.shopflow.dto.request.ProductRequest;
import com.shopflow.dto.response.ProductResponse;
import com.shopflow.service.ProductService;
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

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Produits", description = "Gestion du catalogue produits")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    @Operation(summary = "Liste paginée avec filtres")
    public ResponseEntity<Page<ProductResponse>> getProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal prixMin,
            @RequestParam(required = false) BigDecimal prixMax,
            @RequestParam(required = false) Long sellerId,
            @RequestParam(required = false) Boolean promo,
            @RequestParam(required = false, defaultValue = "dateCreation") String sortBy,
            @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok(productService.getProducts(categoryId, prixMin, prixMax, sellerId, promo, sortBy, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détail produit + variantes + avis + note moyenne")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @Operation(summary = "Créer un produit", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody ProductRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.createProduct(request, userDetails.getUsername()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @Operation(summary = "Modifier un produit", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(productService.updateProduct(id, request, userDetails.getUsername()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @Operation(summary = "Désactiver un produit (soft delete)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        productService.deactivateProduct(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Recherche full-text")
    public ResponseEntity<Page<ProductResponse>> search(
            @RequestParam String q,
            @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok(productService.searchProducts(q, pageable));
    }

    @GetMapping("/top-selling")
    @Operation(summary = "Top 10 meilleures ventes")
    public ResponseEntity<List<ProductResponse>> topSelling() {
        return ResponseEntity.ok(productService.getTopSelling());
    }
}
