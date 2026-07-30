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
            seedMoreProducts();
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

        java.util.Map<String, BigDecimal[]> priceUpdates = new java.util.HashMap<>();
        priceUpdates.put("T-Shirt Premium Coton Bio", new BigDecimal[]{new BigDecimal("59.99"), new BigDecimal("39.99")});
        priceUpdates.put("Jean Slim Fit", new BigDecimal[]{new BigDecimal("129.99"), null});
        priceUpdates.put("Robe Ete Fleurie", new BigDecimal[]{new BigDecimal("89.99"), new BigDecimal("69.99")});
        priceUpdates.put("Veste en Jean", new BigDecimal[]{new BigDecimal("169.99"), null});
        priceUpdates.put("Ecouteurs Bluetooth Pro", new BigDecimal[]{new BigDecimal("199.99"), new BigDecimal("149.99")});
        priceUpdates.put("Chargeur Rapide USB-C 65W", new BigDecimal[]{new BigDecimal("49.99"), null});
        priceUpdates.put("Coque iPhone 15 Pro", new BigDecimal[]{new BigDecimal("39.99"), new BigDecimal("29.99")});
        priceUpdates.put("Support Telephone Voiture", new BigDecimal[]{new BigDecimal("34.99"), null});

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
    protected void seedMoreProducts() {
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
        int added = 0;

        var newProducts = List.of(
            // ── VETEMENTS (Mode et Style) ──
            new NewProduct(vendeur1, "Pantalon Chino Homme", "Pantalon chino coupe slim, confortable et élégant pour le quotidien",
                new BigDecimal("69.99"), null, 40, catVetements, catPantalons,
                List.of("https://images.unsplash.com/photo-1473966968600-fa801b869a1a?w=400&h=400&fit=crop", "https://images.unsplash.com/photo-1594938379494-0d2fe6031c81?w=400&h=400&fit=crop")),
            new NewProduct(vendeur1, "Blazer Homme", "Blazer coupe moderne, parfait pour le bureau ou les occasions spéciales",
                new BigDecimal("199.99"), new BigDecimal("169.99"), 20, catVetements,
                List.of("https://images.unsplash.com/photo-1593030761757-71fae45fa0e7?w=400&h=400&fit=crop")),
            new NewProduct(vendeur1, "Jupe Plissée Femme", "Jupe plissée légère et fluide, idéale pour toutes les saisons",
                new BigDecimal("54.99"), null, 35, catVetements,
                List.of("https://images.unsplash.com/photo-1583496661160-fb5886a0d74c?w=400&h=400&fit=crop")),
            new NewProduct(vendeur1, "Basket High Tops", "Baskets montantes en cuir, style streetwear tendance",
                new BigDecimal("89.99"), new BigDecimal("69.99"), 30, catVetements,
                List.of("https://images.unsplash.com/photo-1460353581641-37baddab0fa2?w=400&h=400&fit=crop", "https://images.unsplash.com/photo-1606107557195-0e29a4b5b4aa?w=400&h=400&fit=crop")),
            new NewProduct(vendeur1, "Chemise en Lin", "Chemise en lin léger, coupe décontractée pour l'été",
                new BigDecimal("74.99"), null, 45, catVetements, catTshirts,
                List.of("https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=400&h=400&fit=crop")),
            new NewProduct(vendeur1, "Short de Sport", "Short de sport en tissu technique respirant",
                new BigDecimal("39.99"), null, 60, catVetements, catPantalons,
                List.of("https://images.unsplash.com/photo-1591195853828-11db59a44f6b?w=400&h=400&fit=crop")),
            // ── ELECTRONIQUE (Tech Universe) ──
            new NewProduct(vendeur2, "Clavier Mécanique RGB", "Clavier gaming mécanique avec switches bleus et rétroéclairage RGB",
                new BigDecimal("79.99"), null, 50, catElectronique, catAccessoires,
                List.of("https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=400&h=400&fit=crop", "https://images.unsplash.com/photo-1618384887929-16ec33fab9ef?w=400&h=400&fit=crop")),
            new NewProduct(vendeur2, "Casque Audio Sans Fil", "Casque bluetooth avec réduction de bruit active, 40h d'autonomie",
                new BigDecimal("149.99"), new BigDecimal("119.99"), 30, catElectronique, catAccessoires,
                List.of("https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400&h=400&fit=crop", "https://images.unsplash.com/photo-1583394838336-acd977736f90?w=400&h=400&fit=crop")),
            new NewProduct(vendeur2, "Disque Dur SSD 1To", "SSD NVMe 1To, vitesses de lecture jusqu'à 3500 Mo/s",
                new BigDecimal("129.99"), null, 25, catElectronique, catAccessoires,
                List.of("https://images.unsplash.com/photo-1597872200969-2b65d56bd16b?w=400&h=400&fit=crop")),
            new NewProduct(vendeur2, "Station d'Accueil USB-C", "Hub USB-C 7-en-1 avec HDMI 4K, USB 3.0, SD/microSD",
                new BigDecimal("59.99"), null, 35, catElectronique, catAccessoires,
                List.of("https://images.unsplash.com/photo-1623869675781-80aa31012a5a?w=400&h=400&fit=crop")),
            // ── MAISON (Mode et Style) ──
            new NewProduct(vendeur1, "Coussin Décoratif Velours", "Coussin en velours haut de gamme, disponible en plusieurs couleurs",
                new BigDecimal("29.99"), null, 80, catMaison,
                List.of("https://images.unsplash.com/photo-1584100936595-c0654b55a2e2?w=400&h=400&fit=crop")),
            new NewProduct(vendeur1, "Lampe d'Ambiance LED", "Lampe d'ambiance avec variateur d'intensité et 3 températures",
                new BigDecimal("39.99"), new BigDecimal("29.99"), 50, catMaison,
                List.of("https://images.unsplash.com/photo-1507473885765-e6ed057ab6fe?w=400&h=400&fit=crop")),
            new NewProduct(vendeur1, "Tapis de Salon Shaggy", "Tapis shaggy ultra-doux 160x230cm, idéal pour le salon",
                new BigDecimal("129.99"), null, 15, catMaison,
                List.of("https://images.unsplash.com/photo-1600166898405-da9535204843?w=400&h=400&fit=crop")),
            new NewProduct(vendeur1, "Miroir Mural Design", "Miroir mural design cadre aluminium, 80x120cm",
                new BigDecimal("79.99"), null, 20, catMaison,
                List.of("https://images.unsplash.com/photo-1619810223564-d5fdbe40b0c3?w=400&h=400&fit=crop")),
            new NewProduct(vendeur1, "Set de Cuisine 5 Pièces", "Set de cuisine anti-adhésif composé de 5 ustensiles essentiels",
                new BigDecimal("89.99"), null, 30, catMaison,
                List.of("https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?w=400&h=400&fit=crop"))
        );

        for (var np : newProducts) {
            if (!existingNames.contains(np.nom)) {
                productRepository.save(Product.builder().seller(np.seller)
                    .nom(np.nom).description(np.description)
                    .prix(np.prix).prixPromo(np.prixPromo).stock(np.stock)
                    .images(np.images)
                    .categories(np.categories).noteMoyenne(4.2).totalVentes(0).build());
                added++;
            }
        }

        if (added > 0) {
            log.info("{} nouveaux produits ajoutés → total: {}", added, productRepository.count());
        }
    }

    private record NewProduct(User seller, String nom, String description, BigDecimal prix, BigDecimal prixPromo, int stock, Set<Category> categories, List<String> images) {
        NewProduct(User seller, String nom, String description, BigDecimal prix, BigDecimal prixPromo, int stock, Category c1, Category c2, List<String> images) {
            this(seller, nom, description, prix, prixPromo, stock, Set.of(c1, c2), images);
        }
        NewProduct(User seller, String nom, String description, BigDecimal prix, BigDecimal prixPromo, int stock, Category c1, List<String> images) {
            this(seller, nom, description, prix, prixPromo, stock, Set.of(c1), images);
        }
    }
}
