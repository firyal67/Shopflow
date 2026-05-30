package com.shopflow.controller;

import com.shopflow.dto.request.AddressRequest;
import com.shopflow.dto.response.AddressResponse;
import com.shopflow.dto.response.UserResponse;
import com.shopflow.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Utilisateurs", description = "Gestion des profils et adresses")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Mon profil")
    public ResponseEntity<UserResponse> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getProfile(userDetails.getUsername()));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Tous les utilisateurs (ADMIN)")
    public ResponseEntity<Page<UserResponse>> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    @PutMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activer/désactiver un compte (ADMIN)")
    public ResponseEntity<UserResponse> toggleStatus(@PathVariable Long id) {
        return ResponseEntity.ok(userService.toggleUserStatus(id));
    }

    @GetMapping("/me/addresses")
    @Operation(summary = "Mes adresses de livraison")
    public ResponseEntity<List<AddressResponse>> getAddresses(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getAddresses(userDetails.getUsername()));
    }

    @PostMapping("/me/addresses")
    @Operation(summary = "Ajouter une adresse")
    public ResponseEntity<AddressResponse> addAddress(
            @Valid @RequestBody AddressRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.addAddress(request, userDetails.getUsername()));
    }

    @DeleteMapping("/me/addresses/{id}")
    @Operation(summary = "Supprimer une adresse")
    public ResponseEntity<Void> deleteAddress(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        userService.deleteAddress(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    // ---- Réinitialisation mot de passe ----

    @PostMapping("/password-reset/request")
    @Operation(summary = "Demander une réinitialisation de mot de passe")
    public ResponseEntity<Map<String, String>> requestPasswordReset(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        userService.requestPasswordReset(email);
        // En production : envoyer un email. Pour la démo, on retourne le token directement.
        String token = userService.getResetToken(email);
        return ResponseEntity.ok(Map.of(
            "message", "Token de réinitialisation généré",
            "token", token != null ? token : ""
        ));
    }

    @PostMapping("/password-reset/confirm")
    @Operation(summary = "Réinitialiser le mot de passe avec le token")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody Map<String, String> body) {
        userService.resetPassword(body.get("token"), body.get("newPassword"));
        return ResponseEntity.ok(Map.of("message", "Mot de passe réinitialisé avec succès"));
    }
}
