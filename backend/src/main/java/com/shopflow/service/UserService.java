package com.shopflow.service;

import com.shopflow.dto.request.AddressRequest;
import com.shopflow.dto.response.AddressResponse;
import com.shopflow.dto.response.UserResponse;
import com.shopflow.entity.Address;
import com.shopflow.entity.User;
import com.shopflow.exception.BusinessException;
import com.shopflow.exception.ResourceNotFoundException;
import com.shopflow.exception.UnauthorizedException;
import com.shopflow.repository.AddressRepository;
import com.shopflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional
    public UserResponse toggleUserStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", id));
        user.setActif(!user.isActif());
        return toResponse(userRepository.save(user));
    }

    // Adresses
    @Transactional(readOnly = true)
    public List<AddressResponse> getAddresses(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
        return addressRepository.findByUserId(user.getId())
                .stream().map(this::toAddressResponse).collect(Collectors.toList());
    }

    @Transactional
    public AddressResponse addAddress(AddressRequest request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        // Si principal, retirer le flag des autres
        if (request.isPrincipal()) {
            addressRepository.findByUserIdAndPrincipalTrue(user.getId())
                    .ifPresent(a -> { a.setPrincipal(false); addressRepository.save(a); });
        }

        Address address = Address.builder()
                .user(user)
                .rue(request.getRue())
                .ville(request.getVille())
                .codePostal(request.getCodePostal())
                .pays(request.getPays())
                .principal(request.isPrincipal())
                .build();

        return toAddressResponse(addressRepository.save(address));
    }

    @Transactional
    public void deleteAddress(Long id, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Adresse", id));
        if (!address.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("Cette adresse ne vous appartient pas");
        }
        addressRepository.delete(address);
    }

    // ---- Réinitialisation mot de passe ----

    @Transactional
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Aucun compte associé à cet email"));
        String token = UUID.randomUUID().toString();
        user.setResetPasswordToken(token);
        user.setResetPasswordTokenExpiry(LocalDateTime.now().plusHours(1));
        userRepository.save(user);
        // En production : envoyer un email avec le token
        // Pour la démo : le token est retourné dans la réponse API
    }

    @Transactional
    public String getResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Aucun compte associé à cet email"));
        return user.getResetPasswordToken();
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetPasswordToken(token)
                .orElseThrow(() -> new BusinessException("Token de réinitialisation invalide"));
        if (user.getResetPasswordTokenExpiry() == null ||
                user.getResetPasswordTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Token de réinitialisation expiré");
        }
        user.setMotDePasse(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpiry(null);
        userRepository.save(user);
    }

    private UserResponse toResponse(User user) {        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .prenom(user.getPrenom())
                .nom(user.getNom())
                .role(user.getRole())
                .actif(user.isActif())
                .dateCreation(user.getDateCreation())
                .build();
    }

    private AddressResponse toAddressResponse(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .rue(address.getRue())
                .ville(address.getVille())
                .codePostal(address.getCodePostal())
                .pays(address.getPays())
                .principal(address.isPrincipal())
                .build();
    }
}
