package com.shopflow.controller;

import com.shopflow.dto.request.ReviewRequest;
import com.shopflow.dto.response.ReviewResponse;
import com.shopflow.service.ReviewService;
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

@RestController
@RequestMapping("/api/reviews")
@Tag(name = "Avis", description = "Avis et notation des produits")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Poster un avis (achat vérifié)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ReviewResponse> createReview(
            @Valid @RequestBody ReviewRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.createReview(request, userDetails.getUsername()));
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Avis d'un produit (approuvés)")
    public ResponseEntity<Page<ReviewResponse>> getProductReviews(
            @PathVariable Long productId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(reviewService.getProductReviews(productId, pageable));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Approuver un avis (ADMIN)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ReviewResponse> approveReview(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.approveReview(id));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Avis en attente de modération (ADMIN)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Page<ReviewResponse>> getPendingReviews(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(reviewService.getPendingReviews(pageable));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Mes avis (CUSTOMER)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Page<ReviewResponse>> getMyReviews(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(reviewService.getMyReviews(userDetails.getUsername(), pageable));
    }
}
