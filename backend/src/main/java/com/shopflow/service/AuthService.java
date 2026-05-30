package com.shopflow.service;

import com.shopflow.dto.request.LoginRequest;
import com.shopflow.dto.request.RefreshTokenRequest;
import com.shopflow.dto.request.RegisterRequest;
import com.shopflow.dto.response.AuthResponse;
import com.shopflow.dto.response.SellerProfileResponse;
import com.shopflow.dto.response.UserResponse;
import com.shopflow.entity.*;
import com.shopflow.exception.BusinessException;
import com.shopflow.exception.ResourceNotFoundException;
import com.shopflow.repository.RefreshTokenRepository;
import com.shopflow.repository.SellerProfileRepository;
import com.shopflow.repository.UserRepository;
import com.shopflow.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final SellerProfileRepository sellerProfileRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email déjà utilisé : " + request.getEmail());
        }

        User user = User.builder()
                .email(request.getEmail())
                .motDePasse(passwordEncoder.encode(request.getMotDePasse()))
                .prenom(request.getPrenom())
                .nom(request.getNom())
                .role(request.getRole() != null ? request.getRole() : Role.CUSTOMER)
                .actif(true)
                .build();

        user = userRepository.save(user);

        // Créer le profil vendeur si nécessaire
        if (user.getRole() == Role.SELLER) {
            if (request.getNomBoutique() == null || request.getNomBoutique().isBlank()) {
                throw new BusinessException("Le nom de la boutique est obligatoire pour un vendeur");
            }
            SellerProfile profile = SellerProfile.builder()
                    .user(user)
                    .nomBoutique(request.getNomBoutique())
                    .description(request.getDescriptionBoutique())
                    .logo(request.getLogo())
                    .build();
            sellerProfileRepository.save(profile);
        }

        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getMotDePasse())
        );
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        // Révoquer les anciens refresh tokens
        refreshTokenRepository.revokeAllUserTokens(user.getId());

        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new BusinessException("Refresh token invalide"));

        if (refreshToken.isRevoked()) {
            throw new BusinessException("Refresh token révoqué");
        }
        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Refresh token expiré");
        }

        User user = refreshToken.getUser();
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        return buildAuthResponse(user);
    }

    @Transactional
    public void logout(String refreshTokenStr) {
        refreshTokenRepository.findByToken(refreshTokenStr).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshTokenStr = jwtService.generateRefreshToken(user);

        // Supprimer les anciens refresh tokens (au lieu de juste révoquer)
        refreshTokenRepository.deleteAllByUserId(user.getId());

        // Sauvegarder le nouveau refresh token
        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenStr)
                .user(user)
                .expiryDate(LocalDateTime.now().plusSeconds(604800))
                .build();
        refreshTokenRepository.save(refreshToken);

        SellerProfileResponse sellerProfileResponse = null;
        if (user.getRole() == Role.SELLER) {
            sellerProfileRepository.findByUserId(user.getId()).ifPresent(sp -> {});
            var sp = sellerProfileRepository.findByUserId(user.getId()).orElse(null);
            if (sp != null) {
                sellerProfileResponse = SellerProfileResponse.builder()
                        .id(sp.getId())
                        .nomBoutique(sp.getNomBoutique())
                        .description(sp.getDescription())
                        .logo(sp.getLogo())
                        .note(sp.getNote())
                        .build();
            }
        }

        UserResponse userResponse = UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .prenom(user.getPrenom())
                .nom(user.getNom())
                .role(user.getRole())
                .actif(user.isActif())
                .dateCreation(user.getDateCreation())
                .sellerProfile(sellerProfileResponse)
                .build();

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenStr)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpiration())
                .user(userResponse)
                .build();
    }
}
