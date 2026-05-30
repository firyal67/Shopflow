package com.shopflow.service;

import com.shopflow.dto.request.LoginRequest;
import com.shopflow.dto.request.RegisterRequest;
import com.shopflow.dto.response.AuthResponse;
import com.shopflow.entity.Role;
import com.shopflow.entity.User;
import com.shopflow.exception.BusinessException;
import com.shopflow.repository.RefreshTokenRepository;
import com.shopflow.repository.SellerProfileRepository;
import com.shopflow.repository.UserRepository;
import com.shopflow.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private SellerProfileRepository sellerProfileRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private User mockUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setMotDePasse("Password1");
        registerRequest.setPrenom("Jean");
        registerRequest.setNom("Dupont");
        registerRequest.setRole(Role.CUSTOMER);

        mockUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .motDePasse("encoded")
                .prenom("Jean")
                .nom("Dupont")
                .role(Role.CUSTOMER)
                .actif(true)
                .build();
    }

    @Test
    void register_shouldCreateUser_whenEmailNotExists() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(jwtService.generateAccessToken(any())).thenReturn("access_token");
        when(jwtService.generateRefreshToken(any())).thenReturn("refresh_token");
        when(jwtService.getAccessTokenExpiration()).thenReturn(900000L);
        when(refreshTokenRepository.save(any())).thenReturn(null);

        AuthResponse response = authService.register(registerRequest);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access_token");
        assertThat(response.getUser().getEmail()).isEqualTo("test@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_shouldThrowException_whenEmailAlreadyExists() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Email déjà utilisé");
    }

    @Test
    void login_shouldReturnTokens_whenCredentialsValid() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setMotDePasse("Password1");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(jwtService.generateAccessToken(any())).thenReturn("access_token");
        when(jwtService.generateRefreshToken(any())).thenReturn("refresh_token");
        when(jwtService.getAccessTokenExpiration()).thenReturn(900000L);
        when(refreshTokenRepository.save(any())).thenReturn(null);

        AuthResponse response = authService.login(loginRequest);

        assertThat(response.getAccessToken()).isNotNull();
        assertThat(response.getRefreshToken()).isNotNull();
    }

    @Test
    void register_seller_shouldThrowException_whenNomBoutiqueIsNull() {
        registerRequest.setRole(Role.SELLER);
        registerRequest.setNomBoutique(null);

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        mockUser = User.builder().id(1L).email("test@example.com")
                .motDePasse("encoded").prenom("Jean").nom("Dupont")
                .role(Role.SELLER).actif(true).build();
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("nom de la boutique");
    }
}
