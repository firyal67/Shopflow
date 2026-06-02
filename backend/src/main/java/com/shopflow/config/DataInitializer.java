package com.shopflow.config;

import com.shopflow.entity.*;
import com.shopflow.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // Créer le compte admin par défaut s'il n'existe pas
        if (userRepository.findByEmail("admin@shopflow.com").isEmpty()) {
            userRepository.save(User.builder()
                    .email("admin@shopflow.com")
                    .motDePasse(passwordEncoder.encode("Admin123!"))
                    .prenom("Admin")
                    .nom("ShopFlow")
                    .role(Role.ADMIN)
                    .actif(true)
                    .build());
            log.info("✅ Compte admin créé : admin@shopflow.com / Admin123!");
        }
    }
}
