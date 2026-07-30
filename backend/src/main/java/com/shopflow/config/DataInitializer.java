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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final SellerProfileRepository sellerProfileRepository;
    private final AddressRepository addressRepository;
    private final ProductVariantRepository productVariantRepository;
    private final CouponRepository couponRepository;
    private final ReviewRepository reviewRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            log.info("Données déjà présentes — mise à jour des images/prix et ajout de nouveaux produits");
            updateProductImages();
            seedAdditionalProducts();
            return;
        }

        String hash = passwordEncoder.encode("Password1");

        // ── UTILISATEURS ──────────────────────────────────────────────
        User admin = userRepository.save(User.builder()
                .email("admin@shopflow.com").motDePasse(hash).prenom("Admin").nom("ShopFlow")
                .role(Role.ADMIN).actif(true).build());
        User vendeur1 = userRepository.save(User.builder()
                .email("vendeur1@shopflow.com").motDePasse(hash).prenom("Marie").nom("Martin")
                .role(Role.SELLER).actif(true).build());
        User vendeur2 = userRepository.save(User.builder()
                .email("vendeur2@shopflow.com").motDePasse(hash).prenom("Pierre").nom("Dupont")
                .role(Role.SELLER).actif(true).build());
        User client1 = userRepository.save(User.builder()
                .email("client1@shopflow.com").motDePasse(hash).prenom("Jean").nom("Bernard")
                .role(Role.CUSTOMER).actif(true).build());
        User client2 = userRepository.save(User.builder()
                .email("client2@shopflow.com").motDePasse(hash).prenom("Sophie").nom("Leroy")
                .role(Role.CUSTOMER).actif(true).build());

        log.info("5 utilisateurs créés");

        // ── PROFILS VENDEURS ──────────────────────────────────────────
        sellerProfileRepository.save(SellerProfile.builder().user(vendeur1)
                .nomBoutique("Mode et Style").description("Vetements tendance pour homme et femme")
                .logo("https://picsum.photos/seed/shop1/200").note(4.5).build());
        sellerProfileRepository.save(SellerProfile.builder().user(vendeur2)
                .nomBoutique("Tech Universe").description("Accessoires et gadgets high-tech")
                .logo("https://picsum.photos/seed/shop2/200").note(4.2).build());

        // ── ADRESSES ──────────────────────────────────────────────────
        addressRepository.save(Address.builder().user(client1)
                .rue("12 Rue de la Paix").ville("Paris").codePostal("75001").pays("France").principal(true).build());
        addressRepository.save(Address.builder().user(client1)
                .rue("5 Avenue des Fleurs").ville("Lyon").codePostal("69001").pays("France").principal(false).build());
        addressRepository.save(Address.builder().user(client2)
                .rue("8 Boulevard Victor Hugo").ville("Marseille").codePostal("13001").pays("France").principal(true).build());

        // ── CATEGORIES ────────────────────────────────────────────────
        Category vetements = categoryRepository.save(Category.builder().nom("Vetements").description("Mode et habillement").build());
        Category electronique = categoryRepository.save(Category.builder().nom("Electronique").description("Appareils et accessoires tech").build());
        Category maison = categoryRepository.save(Category.builder().nom("Maison").description("Decoration et mobilier").build());
        Category tshirts = categoryRepository.save(Category.builder().nom("T-Shirts").description("T-shirts et hauts").parent(vetements).build());
        Category pantalons = categoryRepository.save(Category.builder().nom("Pantalons").description("Jeans et pantalons").parent(vetements).build());
        Category smartphones = categoryRepository.save(Category.builder().nom("Smartphones").description("Telephones mobiles").parent(electronique).build());
        Category accessoires = categoryRepository.save(Category.builder().nom("Accessoires").description("Accessoires tech").parent(electronique).build());

        // ── PRODUITS ──────────────────────────────────────────────────
        Product p1 = productRepository.save(Product.builder().seller(vendeur1)
                .nom("T-Shirt Premium Coton Bio").description("T-shirt en coton bio, coupe moderne, disponible en plusieurs tailles")
                .prix(new BigDecimal("59.99")).prixPromo(new BigDecimal("39.99")).stock(50)
                .images(List.of("https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=400&h=400&fit=crop", "https://images.unsplash.com/photo-1583743814966-8936f5b7be1a?w=400&h=400&fit=crop"))
                .categories(Set.of(vetements, tshirts)).noteMoyenne(4.5).totalVentes(120).build());

        Product p2 = productRepository.save(Product.builder().seller(vendeur1)
                .nom("Jean Slim Fit").description("Jean slim fit stretch, confortable et elegant")
                .prix(new BigDecimal("129.99")).stock(30)
                .images(List.of("https://images.unsplash.com/photo-1542272454315-4c01d7abdf4a?w=400&h=400&fit=crop", "https://images.unsplash.com/photo-1604176354204-9268737828e4?w=400&h=400&fit=crop"))
                .categories(Set.of(vetements, pantalons)).noteMoyenne(4.2).totalVentes(85).build());

        Product p3 = productRepository.save(Product.builder().seller(vendeur1)
                .nom("Robe Ete Fleurie").description("Robe legere parfaite pour l'ete, motif floral")
                .prix(new BigDecimal("89.99")).prixPromo(new BigDecimal("69.99")).stock(25)
                .images(List.of("https://images.unsplash.com/photo-1595777457583-95e059d581b8?w=400&h=400&fit=crop"))
                .categories(Set.of(vetements)).noteMoyenne(4.7).totalVentes(60).build());

        Product p4 = productRepository.save(Product.builder().seller(vendeur1)
                .nom("Veste en Jean").description("Veste en jean classique, coupe droite")
                .prix(new BigDecimal("169.99")).stock(20)
                .images(List.of("https://images.unsplash.com/photo-1551028719-00167b16eac5?w=400&h=400&fit=crop"))
                .categories(Set.of(vetements)).noteMoyenne(4.0).totalVentes(45).build());

        Product p5 = productRepository.save(Product.builder().seller(vendeur2)
                .nom("Ecouteurs Bluetooth Pro").description("Ecouteurs sans fil avec reduction de bruit active, 30h d'autonomie")
                .prix(new BigDecimal("199.99")).prixPromo(new BigDecimal("149.99")).stock(40)
                .images(List.of("https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400&h=400&fit=crop", "https://images.unsplash.com/photo-1583394838336-acd977736f90?w=400&h=400&fit=crop"))
                .categories(Set.of(electronique, accessoires)).noteMoyenne(4.6).totalVentes(200).build());

        Product p6 = productRepository.save(Product.builder().seller(vendeur2)
                .nom("Chargeur Rapide USB-C 65W").description("Chargeur rapide compatible avec tous les appareils USB-C")
                .prix(new BigDecimal("49.99")).stock(100)
                .images(List.of("https://images.unsplash.com/photo-1583863788434-e58a36330cf0?w=400&h=400&fit=crop"))
                .categories(Set.of(electronique, accessoires)).noteMoyenne(4.3).totalVentes(350).build());

        Product p7 = productRepository.save(Product.builder().seller(vendeur2)
                .nom("Coque iPhone 15 Pro").description("Coque de protection premium en silicone liquide")
                .prix(new BigDecimal("39.99")).prixPromo(new BigDecimal("29.99")).stock(80)
                .images(List.of("https://images.unsplash.com/photo-1601784551446-20c9e07cdbdb?w=400&h=400&fit=crop"))
                .categories(Set.of(electronique, accessoires)).noteMoyenne(4.1).totalVentes(180).build());

        Product p8 = productRepository.save(Product.builder().seller(vendeur2)
                .nom("Support Telephone Voiture").description("Support magnetique universel pour tableau de bord")
                .prix(new BigDecimal("34.99")).stock(60)
                .images(List.of("https://images.unsplash.com/photo-1617469767053-d3b523a0b982?w=400&h=400&fit=crop"))
                .categories(Set.of(electronique, accessoires)).noteMoyenne(3.9).totalVentes(95).build());

        log.info("8 produits créés");

        // ── NOUVEAUX PRODUITS ───────────────────────────────────────────────
        Product p9 = productRepository.save(Product.builder().seller(vendeur1)
                .nom("Chemise Oxford Homme").description("Chemise Oxford en coton, coupe regular, idéale pour le bureau")
                .prix(new BigDecimal("79.99")).stock(35)
                .images(List.of("https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=400&h=400&fit=crop", "https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?w=400&h=400&fit=crop"))
                .categories(Set.of(vetements, tshirts)).noteMoyenne(4.3).totalVentes(75).build());

        Product p10 = productRepository.save(Product.builder().seller(vendeur1)
                .nom("Short en Jean").description("Short en jean décontracté, parfait pour l'été")
                .prix(new BigDecimal("59.99")).prixPromo(new BigDecimal("44.99")).stock(40)
                .images(List.of("https://images.unsplash.com/photo-1591195853828-11db59a44f6b?w=400&h=400&fit=crop"))
                .categories(Set.of(vetements, pantalons)).noteMoyenne(4.1).totalVentes(55).build());

        Product p11 = productRepository.save(Product.builder().seller(vendeur1)
                .nom("Pull Cachemire").description("Pull en pur cachemire, doux et élégant, idéal pour les soirées fraîches")
                .prix(new BigDecimal("149.99")).prixPromo(new BigDecimal("119.99")).stock(15)
                .images(List.of("https://images.unsplash.com/photo-1576871337622-98d48d1cf531?w=400&h=400&fit=crop", "https://images.unsplash.com/photo-1614975059407-5c8c2e53e4ae?w=400&h=400&fit=crop"))
                .categories(Set.of(vetements)).noteMoyenne(4.8).totalVentes(40).build());

        Product p12 = productRepository.save(Product.builder().seller(vendeur2)
                .nom("Montre Connectée Sport").description("Montre connectée avec GPS, cardiofréquencemètre et 5 jours d'autonomie")
                .prix(new BigDecimal("249.99")).prixPromo(new BigDecimal("199.99")).stock(25)
                .images(List.of("https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=400&h=400&fit=crop", "https://images.unsplash.com/photo-1546868871-af0de0ae72e7?w=400&h=400&fit=crop"))
                .categories(Set.of(electronique, accessoires)).noteMoyenne(4.4).totalVentes(160).build());

        Product p13 = productRepository.save(Product.builder().seller(vendeur2)
                .nom("Enceinte Bluetooth Portable").description("Enceinte portable 30W, étanche IPX7, 20h d'autonomie")
                .prix(new BigDecimal("89.99")).prixPromo(new BigDecimal("69.99")).stock(50)
                .images(List.of("https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?w=400&h=400&fit=crop"))
                .categories(Set.of(electronique, accessoires)).noteMoyenne(4.5).totalVentes(230).build());

        Product p14 = productRepository.save(Product.builder().seller(vendeur2)
                .nom("Souris Gaming RGB").description("Souris gaming optique 16000 DPI, RGB personnalisable, 6 boutons")
                .prix(new BigDecimal("59.99")).stock(70)
                .images(List.of("https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=400&h=400&fit=crop"))
                .categories(Set.of(electronique, accessoires)).noteMoyenne(4.2).totalVentes(300).build());

        Product p15 = productRepository.save(Product.builder().seller(vendeur2)
                .nom("Lampe de Bureau LED").description("Lampe LED design avec éclairage réglable, USB de chargement intégré")
                .prix(new BigDecimal("45.99")).stock(45)
                .images(List.of("https://images.unsplash.com/photo-1507473885765-e6ed057ab6fe?w=400&h=400&fit=crop"))
                .categories(Set.of(maison)).noteMoyenne(4.0).totalVentes(110).build());

        Product p16 = productRepository.save(Product.builder().seller(vendeur1)
                .nom("Set de Draps 240 fils").description("Parure de draps en coton égyptien 240 fils au cm², confort luxe")
                .prix(new BigDecimal("69.99")).prixPromo(new BigDecimal("54.99")).stock(30)
                .images(List.of("https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=400&h=400&fit=crop"))
                .categories(Set.of(maison)).noteMoyenne(4.6).totalVentes(85).build());

        log.info("16 produits créés");

        // ── VARIANTES ─────────────────────────────────────────────────
        productVariantRepository.save(ProductVariant.builder().product(p1).attribut("Taille").valeur("S").stockSupplementaire(10).prixDelta(BigDecimal.ZERO).build());
        productVariantRepository.save(ProductVariant.builder().product(p1).attribut("Taille").valeur("M").stockSupplementaire(15).prixDelta(BigDecimal.ZERO).build());
        productVariantRepository.save(ProductVariant.builder().product(p1).attribut("Taille").valeur("L").stockSupplementaire(15).prixDelta(BigDecimal.ZERO).build());
        productVariantRepository.save(ProductVariant.builder().product(p1).attribut("Taille").valeur("XL").stockSupplementaire(10).prixDelta(new BigDecimal("2.00")).build());

        productVariantRepository.save(ProductVariant.builder().product(p2).attribut("Taille").valeur("38").stockSupplementaire(8).prixDelta(BigDecimal.ZERO).build());
        productVariantRepository.save(ProductVariant.builder().product(p2).attribut("Taille").valeur("40").stockSupplementaire(10).prixDelta(BigDecimal.ZERO).build());
        productVariantRepository.save(ProductVariant.builder().product(p2).attribut("Taille").valeur("42").stockSupplementaire(8).prixDelta(BigDecimal.ZERO).build());
        productVariantRepository.save(ProductVariant.builder().product(p2).attribut("Taille").valeur("44").stockSupplementaire(4).prixDelta(BigDecimal.ZERO).build());

        productVariantRepository.save(ProductVariant.builder().product(p3).attribut("Taille").valeur("S").stockSupplementaire(8).prixDelta(BigDecimal.ZERO).build());
        productVariantRepository.save(ProductVariant.builder().product(p3).attribut("Taille").valeur("M").stockSupplementaire(10).prixDelta(BigDecimal.ZERO).build());
        productVariantRepository.save(ProductVariant.builder().product(p3).attribut("Taille").valeur("L").stockSupplementaire(7).prixDelta(BigDecimal.ZERO).build());

        productVariantRepository.save(ProductVariant.builder().product(p5).attribut("Couleur").valeur("Noir").stockSupplementaire(15).prixDelta(BigDecimal.ZERO).build());
        productVariantRepository.save(ProductVariant.builder().product(p5).attribut("Couleur").valeur("Blanc").stockSupplementaire(15).prixDelta(BigDecimal.ZERO).build());
        productVariantRepository.save(ProductVariant.builder().product(p5).attribut("Couleur").valeur("Bleu").stockSupplementaire(10).prixDelta(new BigDecimal("5.00")).build());

        productVariantRepository.save(ProductVariant.builder().product(p9).attribut("Taille").valeur("S").stockSupplementaire(10).prixDelta(BigDecimal.ZERO).build());
        productVariantRepository.save(ProductVariant.builder().product(p9).attribut("Taille").valeur("M").stockSupplementaire(15).prixDelta(BigDecimal.ZERO).build());
        productVariantRepository.save(ProductVariant.builder().product(p9).attribut("Taille").valeur("L").stockSupplementaire(12).prixDelta(BigDecimal.ZERO).build());
        productVariantRepository.save(ProductVariant.builder().product(p9).attribut("Taille").valeur("XL").stockSupplementaire(8).prixDelta(BigDecimal.ZERO).build());

        productVariantRepository.save(ProductVariant.builder().product(p10).attribut("Taille").valeur("S").stockSupplementaire(10).prixDelta(BigDecimal.ZERO).build());
        productVariantRepository.save(ProductVariant.builder().product(p10).attribut("Taille").valeur("M").stockSupplementaire(15).prixDelta(BigDecimal.ZERO).build());
        productVariantRepository.save(ProductVariant.builder().product(p10).attribut("Taille").valeur("L").stockSupplementaire(10).prixDelta(BigDecimal.ZERO).build());

        productVariantRepository.save(ProductVariant.builder().product(p11).attribut("Taille").valeur("M").stockSupplementaire(8).prixDelta(BigDecimal.ZERO).build());
        productVariantRepository.save(ProductVariant.builder().product(p11).attribut("Taille").valeur("L").stockSupplementaire(10).prixDelta(BigDecimal.ZERO).build());
        productVariantRepository.save(ProductVariant.builder().product(p11).attribut("Taille").valeur("XL").stockSupplementaire(5).prixDelta(new BigDecimal("10.00")).build());

        productVariantRepository.save(ProductVariant.builder().product(p12).attribut("Couleur").valeur("Noir").stockSupplementaire(10).prixDelta(BigDecimal.ZERO).build());
        productVariantRepository.save(ProductVariant.builder().product(p12).attribut("Couleur").valeur("Argent").stockSupplementaire(8).prixDelta(BigDecimal.ZERO).build());
        productVariantRepository.save(ProductVariant.builder().product(p12).attribut("Taille").valeur("42mm").stockSupplementaire(12).prixDelta(BigDecimal.ZERO).build());
        productVariantRepository.save(ProductVariant.builder().product(p12).attribut("Taille").valeur("46mm").stockSupplementaire(8).prixDelta(new BigDecimal("20.00")).build());

        productVariantRepository.save(ProductVariant.builder().product(p13).attribut("Couleur").valeur("Noir").stockSupplementaire(20).prixDelta(BigDecimal.ZERO).build());
        productVariantRepository.save(ProductVariant.builder().product(p13).attribut("Couleur").valeur("Bleu").stockSupplementaire(15).prixDelta(BigDecimal.ZERO).build());
        productVariantRepository.save(ProductVariant.builder().product(p13).attribut("Couleur").valeur("Rouge").stockSupplementaire(10).prixDelta(BigDecimal.ZERO).build());

        productVariantRepository.save(ProductVariant.builder().product(p14).attribut("Couleur").valeur("Noir").stockSupplementaire(30).prixDelta(BigDecimal.ZERO).build());
        productVariantRepository.save(ProductVariant.builder().product(p14).attribut("Couleur").valeur("Blanc").stockSupplementaire(20).prixDelta(BigDecimal.ZERO).build());

        // ── COUPONS ───────────────────────────────────────────────────
        couponRepository.save(Coupon.builder().code("BIENVENUE10").type(CouponType.PERCENT).valeur(new BigDecimal("10.00"))
                .dateExpiration(LocalDateTime.of(2026, 10, 21, 0, 0)).usagesMax(100).usagesActuels(5).actif(true).build());
        couponRepository.save(Coupon.builder().code("PROMO20").type(CouponType.PERCENT).valeur(new BigDecimal("20.00"))
                .dateExpiration(LocalDateTime.of(2026, 5, 21, 0, 0)).usagesMax(50).usagesActuels(12).actif(true).build());
        couponRepository.save(Coupon.builder().code("REMISE5").type(CouponType.FIXED).valeur(new BigDecimal("5.00"))
                .dateExpiration(LocalDateTime.of(2026, 7, 21, 0, 0)).usagesMax(200).usagesActuels(30).actif(true).build());
        couponRepository.save(Coupon.builder().code("FLASH15").type(CouponType.PERCENT).valeur(new BigDecimal("15.00"))
                .dateExpiration(LocalDateTime.of(2026, 4, 28, 0, 0)).usagesMax(30).usagesActuels(0).actif(true).build());

        // ── AVIS ──────────────────────────────────────────────────────
        reviewRepository.save(Review.builder().customer(client1).product(p1).note(5)
                .commentaire("Excellent t-shirt, tres confortable et belle qualite !").approuve(true).build());
        reviewRepository.save(Review.builder().customer(client1).product(p6).note(4)
                .commentaire("Chargeur rapide et efficace, livraison rapide.").approuve(true).build());
        reviewRepository.save(Review.builder().customer(client1).product(p8).note(4)
                .commentaire("Bon support, tient bien le telephone.").approuve(true).build());
        reviewRepository.save(Review.builder().customer(client1).product(p5).note(5)
                .commentaire("Son exceptionnel, reduction de bruit parfaite !").approuve(false).build());
        reviewRepository.save(Review.builder().customer(client2).product(p3).note(5)
                .commentaire("Magnifique robe, coupe parfaite et tissu agreable.").approuve(false).build());

        reviewRepository.save(Review.builder().customer(client1).product(p9).note(4)
                .commentaire("Belle chemise, bon rapport qualite-prix.").approuve(true).build());
        reviewRepository.save(Review.builder().customer(client2).product(p11).note(5)
                .commentaire("Cachemire superbe ! Tres doux et confortable.").approuve(true).build());
        reviewRepository.save(Review.builder().customer(client2).product(p12).note(4)
                .commentaire("Excellente montre, GPS precis et bonne autonomie.").approuve(true).build());
        reviewRepository.save(Review.builder().customer(client1).product(p13).note(5)
                .commentaire("Son incroyable pour la taille, je recommande !").approuve(true).build());
        reviewRepository.save(Review.builder().customer(client1).product(p14).note(3)
                .commentaire("Bon rapport qualite-prix, les RGB sont sympas.").approuve(true).build());

        log.info("Données de démonstration chargées avec succès");
        log.info("→ admin@shopflow.com / Password1 (ADMIN)");
        log.info("→ vendeur1@shopflow.com / Password1 (SELLER)");
        log.info("→ client1@shopflow.com / Password1 (CUSTOMER)");
    }

    @Transactional
    protected void updateProductImages() {
        var products = productRepository.findAll();

        java.util.Map<String, List<String>> imageUpdates = new java.util.HashMap<>();
        imageUpdates.put("T-Shirt Premium Coton Bio", List.of("https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=400&h=400&fit=crop", "https://images.unsplash.com/photo-1583743814966-8936f5b7be1a?w=400&h=400&fit=crop"));
        imageUpdates.put("Jean Slim Fit", List.of("https://images.unsplash.com/photo-1542272454315-4c01d7abdf4a?w=400&h=400&fit=crop", "https://images.unsplash.com/photo-1604176354204-9268737828e4?w=400&h=400&fit=crop"));
        imageUpdates.put("Robe Ete Fleurie", List.of("https://images.unsplash.com/photo-1595777457583-95e059d581b8?w=400&h=400&fit=crop"));
        imageUpdates.put("Veste en Jean", List.of("https://images.unsplash.com/photo-1551028719-00167b16eac5?w=400&h=400&fit=crop"));
        imageUpdates.put("Ecouteurs Bluetooth Pro", List.of("https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400&h=400&fit=crop", "https://images.unsplash.com/photo-1583394838336-acd977736f90?w=400&h=400&fit=crop"));
        imageUpdates.put("Chargeur Rapide USB-C 65W", List.of("https://images.unsplash.com/photo-1583863788434-e58a36330cf0?w=400&h=400&fit=crop"));
        imageUpdates.put("Coque iPhone 15 Pro", List.of("https://images.unsplash.com/photo-1601784551446-20c9e07cdbdb?w=400&h=400&fit=crop"));
        imageUpdates.put("Support Telephone Voiture", List.of("https://images.unsplash.com/photo-1617469767053-d3b523a0b982?w=400&h=400&fit=crop"));
        imageUpdates.put("Chemise Oxford Homme", List.of("https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=400&h=400&fit=crop", "https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?w=400&h=400&fit=crop"));
        imageUpdates.put("Short en Jean", List.of("https://images.unsplash.com/photo-1591195853828-11db59a44f6b?w=400&h=400&fit=crop"));
        imageUpdates.put("Pull Cachemire", List.of("https://images.unsplash.com/photo-1576871337622-98d48d1cf531?w=400&h=400&fit=crop", "https://images.unsplash.com/photo-1614975059407-5c8c2e53e4ae?w=400&h=400&fit=crop"));
        imageUpdates.put("Montre Connectée Sport", List.of("https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=400&h=400&fit=crop", "https://images.unsplash.com/photo-1546868871-af0de0ae72e7?w=400&h=400&fit=crop"));
        imageUpdates.put("Enceinte Bluetooth Portable", List.of("https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?w=400&h=400&fit=crop"));
        imageUpdates.put("Souris Gaming RGB", List.of("https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=400&h=400&fit=crop"));
        imageUpdates.put("Lampe de Bureau LED", List.of("https://images.unsplash.com/photo-1507473885765-e6ed057ab6fe?w=400&h=400&fit=crop"));
        imageUpdates.put("Set de Draps 240 fils", List.of("https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=400&h=400&fit=crop"));

        java.util.Map<String, BigDecimal[]> priceUpdates = new java.util.HashMap<>();
        priceUpdates.put("T-Shirt Premium Coton Bio", new BigDecimal[]{new BigDecimal("59.99"), new BigDecimal("39.99")});
        priceUpdates.put("Jean Slim Fit", new BigDecimal[]{new BigDecimal("129.99"), null});
        priceUpdates.put("Robe Ete Fleurie", new BigDecimal[]{new BigDecimal("89.99"), new BigDecimal("69.99")});
        priceUpdates.put("Veste en Jean", new BigDecimal[]{new BigDecimal("169.99"), null});
        priceUpdates.put("Ecouteurs Bluetooth Pro", new BigDecimal[]{new BigDecimal("199.99"), new BigDecimal("149.99")});
        priceUpdates.put("Chargeur Rapide USB-C 65W", new BigDecimal[]{new BigDecimal("49.99"), null});
        priceUpdates.put("Coque iPhone 15 Pro", new BigDecimal[]{new BigDecimal("39.99"), new BigDecimal("29.99")});
        priceUpdates.put("Support Telephone Voiture", new BigDecimal[]{new BigDecimal("34.99"), null});
        priceUpdates.put("Chemise Oxford Homme", new BigDecimal[]{new BigDecimal("79.99"), null});
        priceUpdates.put("Short en Jean", new BigDecimal[]{new BigDecimal("59.99"), new BigDecimal("44.99")});
        priceUpdates.put("Pull Cachemire", new BigDecimal[]{new BigDecimal("149.99"), new BigDecimal("119.99")});
        priceUpdates.put("Montre Connectée Sport", new BigDecimal[]{new BigDecimal("249.99"), new BigDecimal("199.99")});
        priceUpdates.put("Enceinte Bluetooth Portable", new BigDecimal[]{new BigDecimal("89.99"), new BigDecimal("69.99")});
        priceUpdates.put("Souris Gaming RGB", new BigDecimal[]{new BigDecimal("59.99"), null});
        priceUpdates.put("Lampe de Bureau LED", new BigDecimal[]{new BigDecimal("45.99"), null});
        priceUpdates.put("Set de Draps 240 fils", new BigDecimal[]{new BigDecimal("69.99"), new BigDecimal("54.99")});

        for (var p : products) {
            List<String> newImages = imageUpdates.get(p.getNom());
            BigDecimal[] newPrices = priceUpdates.get(p.getNom());
            if (newImages != null) {
                p.getImages().clear();
                p.getImages().addAll(newImages);
            }
            if (newPrices != null) {
                p.setPrix(newPrices[0]);
                p.setPrixPromo(newPrices[1]);
            }
            if (newImages != null || newPrices != null) {
                productRepository.save(p);
            }
        }
        log.info("Images et prix mis à jour pour {} produits", products.size());
    }

    @Transactional
    protected void seedAdditionalProducts() {
        if (productRepository.count() >= 16) {
            log.info("16+ produits déjà présents");
            return;
        }

        var categories = categoryRepository.findAll();
        var catVetements = categories.stream().filter(c -> c.getNom().equals("Vetements")).findFirst().orElse(null);
        var catElectronique = categories.stream().filter(c -> c.getNom().equals("Electronique")).findFirst().orElse(null);
        var catMaison = categories.stream().filter(c -> c.getNom().equals("Maison")).findFirst().orElse(null);
        var catTshirts = categories.stream().filter(c -> c.getNom().equals("T-Shirts")).findFirst().orElse(null);
        var catPantalons = categories.stream().filter(c -> c.getNom().equals("Pantalons")).findFirst().orElse(null);
        var catAccessoires = categories.stream().filter(c -> c.getNom().equals("Accessoires")).findFirst().orElse(null);

        var sellers = sellerProfileRepository.findAll();
        var vendeur1 = sellers.stream().filter(s -> s.getNomBoutique().equals("Mode et Style")).findFirst().map(s -> s.getUser()).orElse(null);
        var vendeur2 = sellers.stream().filter(s -> s.getNomBoutique().equals("Tech Universe")).findFirst().map(s -> s.getUser()).orElse(null);

        if (vendeur1 == null || vendeur2 == null) return;

        var existingNames = productRepository.findAll().stream().map(p -> p.getNom()).toList();

        if (!existingNames.contains("Chemise Oxford Homme")) {
            productRepository.save(Product.builder().seller(vendeur1)
                .nom("Chemise Oxford Homme").description("Chemise Oxford en coton, coupe regular, idéale pour le bureau")
                .prix(new BigDecimal("79.99")).stock(35)
                .images(List.of("https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=400&h=400&fit=crop", "https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?w=400&h=400&fit=crop"))
                .categories(Set.of(catVetements, catTshirts)).noteMoyenne(4.3).totalVentes(75).build());
        }
        if (!existingNames.contains("Short en Jean")) {
            productRepository.save(Product.builder().seller(vendeur1)
                .nom("Short en Jean").description("Short en jean décontracté, parfait pour l'été")
                .prix(new BigDecimal("59.99")).prixPromo(new BigDecimal("44.99")).stock(40)
                .images(List.of("https://images.unsplash.com/photo-1591195853828-11db59a44f6b?w=400&h=400&fit=crop"))
                .categories(Set.of(catVetements, catPantalons)).noteMoyenne(4.1).totalVentes(55).build());
        }
        if (!existingNames.contains("Pull Cachemire")) {
            productRepository.save(Product.builder().seller(vendeur1)
                .nom("Pull Cachemire").description("Pull en pur cachemire, doux et élégant, idéal pour les soirées fraîches")
                .prix(new BigDecimal("149.99")).prixPromo(new BigDecimal("119.99")).stock(15)
                .images(List.of("https://images.unsplash.com/photo-1576871337622-98d48d1cf531?w=400&h=400&fit=crop", "https://images.unsplash.com/photo-1614975059407-5c8c2e53e4ae?w=400&h=400&fit=crop"))
                .categories(Set.of(catVetements)).noteMoyenne(4.8).totalVentes(40).build());
        }
        if (!existingNames.contains("Montre Connectée Sport")) {
            productRepository.save(Product.builder().seller(vendeur2)
                .nom("Montre Connectée Sport").description("Montre connectée avec GPS, cardiofréquencemètre et 5 jours d'autonomie")
                .prix(new BigDecimal("249.99")).prixPromo(new BigDecimal("199.99")).stock(25)
                .images(List.of("https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=400&h=400&fit=crop", "https://images.unsplash.com/photo-1546868871-af0de0ae72e7?w=400&h=400&fit=crop"))
                .categories(Set.of(catElectronique, catAccessoires)).noteMoyenne(4.4).totalVentes(160).build());
        }
        if (!existingNames.contains("Enceinte Bluetooth Portable")) {
            productRepository.save(Product.builder().seller(vendeur2)
                .nom("Enceinte Bluetooth Portable").description("Enceinte portable 30W, étanche IPX7, 20h d'autonomie")
                .prix(new BigDecimal("89.99")).prixPromo(new BigDecimal("69.99")).stock(50)
                .images(List.of("https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?w=400&h=400&fit=crop"))
                .categories(Set.of(catElectronique, catAccessoires)).noteMoyenne(4.5).totalVentes(230).build());
        }
        if (!existingNames.contains("Souris Gaming RGB")) {
            productRepository.save(Product.builder().seller(vendeur2)
                .nom("Souris Gaming RGB").description("Souris gaming optique 16000 DPI, RGB personnalisable, 6 boutons")
                .prix(new BigDecimal("59.99")).stock(70)
                .images(List.of("https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=400&h=400&fit=crop"))
                .categories(Set.of(catElectronique, catAccessoires)).noteMoyenne(4.2).totalVentes(300).build());
        }
        if (!existingNames.contains("Lampe de Bureau LED")) {
            productRepository.save(Product.builder().seller(vendeur2)
                .nom("Lampe de Bureau LED").description("Lampe LED design avec éclairage réglable, USB de chargement intégré")
                .prix(new BigDecimal("45.99")).stock(45)
                .images(List.of("https://images.unsplash.com/photo-1507473885765-e6ed057ab6fe?w=400&h=400&fit=crop"))
                .categories(Set.of(catMaison)).noteMoyenne(4.0).totalVentes(110).build());
        }
        if (!existingNames.contains("Set de Draps 240 fils")) {
            productRepository.save(Product.builder().seller(vendeur1)
                .nom("Set de Draps 240 fils").description("Parure de draps en coton égyptien 240 fils au cm², confort luxe")
                .prix(new BigDecimal("69.99")).prixPromo(new BigDecimal("54.99")).stock(30)
                .images(List.of("https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=400&h=400&fit=crop"))
                .categories(Set.of(catMaison)).noteMoyenne(4.6).totalVentes(85).build());
        }
        log.info("Produits supplémentaires ajoutés → total: {}", productRepository.count());
    }
}
