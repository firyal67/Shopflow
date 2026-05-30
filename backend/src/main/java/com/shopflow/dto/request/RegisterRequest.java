package com.shopflow.dto.request;

import com.shopflow.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @Email(message = "Email invalide")
    @NotBlank(message = "Email obligatoire")
    private String email;

    @NotBlank(message = "Mot de passe obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
             message = "Le mot de passe doit contenir au moins une majuscule, une minuscule et un chiffre")
    private String motDePasse;

    @NotBlank(message = "Prénom obligatoire")
    private String prenom;

    @NotBlank(message = "Nom obligatoire")
    private String nom;

    // CUSTOMER par défaut, SELLER si inscription vendeur
    private Role role = Role.CUSTOMER;

    // Champs vendeur (optionnels si role = SELLER)
    private String nomBoutique;
    private String descriptionBoutique;
    private String logo;
}
