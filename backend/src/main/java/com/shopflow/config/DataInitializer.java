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

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final SellerProfileRepository sellerProfileRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) return;

        log.info("Initialisation des données de démonstration...");
        String hash = passwordEncoder.encode("Password1");

        // Utilisateurs
        User vendeur1 = userRepository.save(User.builder()
                .email("vendeur1@shopflow.com").motDePasse(hash)
                .prenom("Marie").nom("Martin").role(Role.SELLER).actif(true).build());

        User vendeur2 = userRepository.save(User.builder()
                .email("vendeur2@shopflow.com").motDePasse(hash)
                .prenom("Pierre").nom("Dupont").role(Role.SELLER).actif(true).build());

        User client1 = userRepository.save(User.builder()
                .email("client1@shopflow.com").motDePasse(hash)
                .prenom("Jean").nom("Bernard").role(Role.CUSTOMER).actif(true).build());

        userRepository.save(User.builder()
                .email("client2@shopflow.com").motDePasse(hash)
                .prenom("Sophie").nom("Leroy").role(Role.CUSTOMER).actif(true).build());

        userRepository.save(User.builder()
                .email("admin@shopflow.com").motDePasse(hash)
                .prenom("Admin").nom("ShopFlow").role(Role.ADMIN).actif(true).build());

        // Profils vendeurs
        sellerProfileRepository.save(SellerProfile.builder()
                .user(vendeur1).nomBoutique("Mode et Style")
                .description("Vêtements tendance pour homme et femme")
                .logo("https://picsum.photos/seed/shop1/200").note(4.5).build());

        sellerProfileRepository.save(SellerProfile.builder()
                .user(vendeur2).nomBoutique("Tech Universe")
                .description("Accessoires et gadgets high-tech")
                .logo("https://picsum.photos/seed/shop2/200").note(4.2).build());

        // Adresses
        addressRepository.save(Address.builder().user(client1).rue("12 Rue de la Paix")
                .ville("Paris").codePostal("75001").pays("France").principal(true).build());
        addressRepository.save(Address.builder().user(client1).rue("5 Avenue des Fleurs")
                .ville("Lyon").codePostal("69001").pays("France").principal(false).build());

        // Catégories
        Category vetements = categoryRepository.save(Category.builder().nom("Vêtements").description("Mode et habillement").build());
        Category electronique = categoryRepository.save(Category.builder().nom("Électronique").description("Appareils et accessoires tech").build());
        categoryRepository.save(Category.builder().nom("Maison").description("Décoration et mobilier").build());
        Category tshirts = categoryRepository.save(Category.builder().nom("T-Shirts").description("T-shirts et hauts").parent(vetements).build());
        Category pantalons = categoryRepository.save(Category.builder().nom("Pantalons").description("Jeans et pantalons").parent(vetements).build());
        categoryRepository.save(Category.builder().nom("Smartphones").description("Téléphones mobiles").parent(electronique).build());
        Category accessoires = categoryRepository.save(Category.builder().nom("Accessoires").description("Accessoires tech").parent(electronique).build());

        // Produits vendeur1
        Product p1 = Product.builder()
                .seller(vendeur1).nom("T-Shirt Premium Coton Bio")
                .description("T-shirt en coton bio, coupe moderne").prix(new BigDecimal("29.99"))
                .prixPromo(new BigDecimal("19.99")).stock(50).actif(true)
                .images(List.of("https://picsum.photos/seed/tshirt1/400/400", "https://picsum.photos/seed/tshirt2/400/400"))
                .categories(Set.of(vetements, tshirts)).noteMoyenne(4.5).totalVentes(120).build();
        p1.getVariants().addAll(List.of(
                ProductVariant.builder().product(p1).attribut("Taille").valeur("S").stockSupplementaire(10).prixDelta(BigDecimal.ZERO).build(),
                ProductVariant.builder().product(p1).attribut("Taille").valeur("M").stockSupplementaire(15).prixDelta(BigDecimal.ZERO).build(),
                ProductVariant.builder().product(p1).attribut("Taille").valeur("L").stockSupplementaire(15).prixDelta(BigDecimal.ZERO).build(),
                ProductVariant.builder().product(p1).attribut("Taille").valeur("XL").stockSupplementaire(10).prixDelta(new BigDecimal("2.00")).build()
        ));
        productRepository.save(p1);

        Product p2 = Product.builder()
                .seller(vendeur1).nom("Jean Slim Fit")
                .description("Jean slim fit stretch, confortable et élégant").prix(new BigDecimal("59.99"))
                .stock(30).actif(true).images(List.of("https://picsum.photos/seed/jean1/400/400"))
                .categories(Set.of(vetements, pantalons)).noteMoyenne(4.2).totalVentes(85).build();
        p2.getVariants().addAll(List.of(
                ProductVariant.builder().product(p2).attribut("Taille").valeur("38").stockSupplementaire(8).prixDelta(BigDecimal.ZERO).build(),
                ProductVariant.builder().product(p2).attribut("Taille").valeur("40").stockSupplementaire(10).prixDelta(BigDecimal.ZERO).build(),
                ProductVariant.builder().product(p2).attribut("Taille").valeur("42").stockSupplementaire(8).prixDelta(BigDecimal.ZERO).build()
        ));
        productRepository.save(p2);

        productRepository.save(Product.builder()
                .seller(vendeur1).nom("Robe Été Fleurie")
                .description("Robe légère parfaite pour l'été").prix(new BigDecimal("45.00"))
                .prixPromo(new BigDecimal("35.00")).stock(25).actif(true)
                .images(List.of("https://picsum.photos/seed/robe1/400/400"))
                .categories(Set.of(vetements)).noteMoyenne(4.7).totalVentes(60).build());

        // Produits vendeur2
        Product p5 = Product.builder()
                .seller(vendeur2).nom("Écouteurs Bluetooth Pro")
                .description("Écouteurs sans fil avec réduction de bruit, 30h d'autonomie")
                .prix(new BigDecimal("89.99")).prixPromo(new BigDecimal("69.99")).stock(40).actif(true)
                .images(List.of("https://picsum.photos/seed/ecouteurs1/400/400"))
                .categories(Set.of(electronique, accessoires)).noteMoyenne(4.6).totalVentes(200).build();
        p5.getVariants().addAll(List.of(
                ProductVariant.builder().product(p5).attribut("Couleur").valeur("Noir").stockSupplementaire(15).prixDelta(BigDecimal.ZERO).build(),
                ProductVariant.builder().product(p5).attribut("Couleur").valeur("Blanc").stockSupplementaire(15).prixDelta(BigDecimal.ZERO).build(),
                ProductVariant.builder().product(p5).attribut("Couleur").valeur("Bleu").stockSupplementaire(10).prixDelta(new BigDecimal("5.00")).build()
        ));
        productRepository.save(p5);

        productRepository.save(Product.builder()
                .seller(vendeur2).nom("Chargeur Rapide USB-C 65W")
                .description("Chargeur rapide compatible avec tous les appareils USB-C")
                .prix(new BigDecimal("24.99")).stock(100).actif(true)
                .images(List.of("https://picsum.photos/seed/chargeur1/400/400"))
                .categories(Set.of(electronique, accessoires)).noteMoyenne(4.3).totalVentes(350).build());

        productRepository.save(Product.builder()
                .seller(vendeur2).nom("Coque iPhone 15 Pro")
                .description("Coque de protection premium en silicone liquide")
                .prix(new BigDecimal("19.99")).prixPromo(new BigDecimal("14.99")).stock(80).actif(true)
                .images(List.of("https://picsum.photos/seed/coque1/400/400"))
                .categories(Set.of(electronique, accessoires)).noteMoyenne(4.1).totalVentes(180).build());

        log.info("Données de démo initialisées. Login: client1@shopflow.com / Password1");
    }
}
