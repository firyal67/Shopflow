package com.shopflow.controller;

import com.shopflow.dto.response.DashboardAdminResponse;
import com.shopflow.dto.response.DashboardSellerResponse;
import com.shopflow.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Tableaux de bord")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Stats globales (ADMIN)")
    public ResponseEntity<DashboardAdminResponse> adminDashboard() {
        return ResponseEntity.ok(dashboardService.getAdminDashboard());
    }

    @GetMapping("/seller")
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "Stats du vendeur connecté")
    public ResponseEntity<DashboardSellerResponse> sellerDashboard(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(dashboardService.getSellerDashboard(userDetails.getUsername()));
    }
}
