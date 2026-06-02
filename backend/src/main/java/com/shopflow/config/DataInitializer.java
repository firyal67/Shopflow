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

        // ── Utilisateurs ──────────────────────────────────────────────────────
        User vendeur1 = userRepository.save(User.builder()
                .email("vendeur1@shopflow.com").motDePasse(hash)
                .prenom("Marie").nom("Martin").role(Role.SELLER).actif(true).build());

        User vendeur2 = userRepository.save(User.builder()
                .email("vendeur2@shopflow.com").motDePasse(hash)
                .prenom("Pierre").nom("Dupont").role(Role.SELLER).actif(true).build());

        User vendeur3 = userRepository.save(User.builder()
                .email("vendeur3@shopflow.com").motDePasse(hash)
                .prenom("Lucie").nom("Moreau").role(Role.SELLER).actif(true).build());

        User client1 = userRepository.save(User.builder()
                .email("client1@shopflow.com").motDePasse(hash)
                .prenom("Jean").nom("Bernard").role(Role.CUSTOMER).actif(true).build());

        userRepository.save(User.builder()
                .email("client2@shopflow.com").motDePasse(hash)
                .prenom("Sophie").nom("Leroy").role(Role.CUSTOMER).actif(true).build());

        userRepository.save(User.builder()
                .email("admin@shopflow.com").motDePasse(hash)
                .prenom("Admin").nom("ShopFlow").role(Role.ADMIN).actif(true).build());

        // ── Profils vendeurs ──────────────────────────────────────────────────
        sellerProfileRepository.save(SellerProfile.builder()
                .user(vendeur1).nomBoutique("Mode & Style")
                .description("Vêtements tendance pour homme et femme, qualité premium")
                .logo("https://images.unsplash.com/photo-1567401893414-76b7b1e5a7a5?w=200&fit=crop").note(4.5).build());

        sellerProfileRepository.save(SellerProfile.builder()
                .user(vendeur2).nomBoutique("Tech Universe")
                .description("Accessoires et gadgets high-tech au meilleur prix")
                .logo("https://images.unsplash.com/photo-1550009158-9ebf69173e03?w=200&fit=crop").note(4.2).build());

        sellerProfileRepository.save(SellerProfile.builder()
                .user(vendeur3).nomBoutique("Beauté Naturelle")
                .description("Cosmétiques et soins naturels, cruelty-free")
                .logo("https://images.unsplash.com/photo-1522335789203-aabd1fc54bc9?w=200&fit=crop").note(4.8).build());

        // ── Adresses ──────────────────────────────────────────────────────────
        addressRepository.save(Address.builder().user(client1).rue("12 Rue de la Paix")
                .ville("Paris").codePostal("75001").pays("France").principal(true).build());

        // ── Catégories ────────────────────────────────────────────────────────
        Category vetements   = categoryRepository.save(Category.builder().nom("Vêtements").description("Mode et habillement").build());
        Category electronique = categoryRepository.save(Category.builder().nom("Électronique").description("Appareils et accessoires tech").build());
        Category beaute      = categoryRepository.save(Category.builder().nom("Beauté & Soins").description("Cosmétiques et soins du corps").build());
        Category sport       = categoryRepository.save(Category.builder().nom("Sport").description("Équipements et vêtements de sport").build());
        categoryRepository.save(Category.builder().nom("Maison").description("Décoration et mobilier").build());

        Category tshirts     = categoryRepository.save(Category.builder().nom("T-Shirts").description("T-shirts et hauts").parent(vetements).build());
        Category pantalons   = categoryRepository.save(Category.builder().nom("Pantalons").description("Jeans et pantalons").parent(vetements).build());
        Category robes       = categoryRepository.save(Category.builder().nom("Robes").description("Robes et jupes").parent(vetements).build());
        Category vestes      = categoryRepository.save(Category.builder().nom("Vestes").description("Vestes et manteaux").parent(vetements).build());
        Category audio       = categoryRepository.save(Category.builder().nom("Audio").description("Casques et écouteurs").parent(electronique).build());
        Category accessTech  = categoryRepository.save(Category.builder().nom("Accessoires Tech").description("Accessoires pour appareils").parent(electronique).build());
        categoryRepository.save(Category.builder().nom("Smartphones").description("Téléphones mobiles").parent(electronique).build());
        Category soinsPeau   = categoryRepository.save(Category.builder().nom("Soins Peau").description("Crèmes et sérums").parent(beaute).build());
        Category sportVet    = categoryRepository.save(Category.builder().nom("Vêtements Sport").description("Tenues de sport").parent(sport).build());

        // ── Produits Mode & Style ─────────────────────────────────────────────

        // T-shirt blanc
        Product p1 = Product.builder()
                .seller(vendeur1).nom("T-Shirt Essentiel Blanc")
                .description("T-shirt 100% coton bio, coupe droite confortable. Col rond renforcé, coutures doubles pour une durabilité optimale. Idéal au quotidien.")
                .prix(new BigDecimal("24.99")).prixPromo(new BigDecimal("17.99"))
                .stock(80).actif(true).noteMoyenne(4.6).totalVentes(340)
                .images(List.of(
                        "https://source.unsplash.com/500x500/?white,tshirt",
                        "https://source.unsplash.com/500x500/?cotton,shirt"
                ))
                .categories(Set.of(vetements, tshirts)).build();
        p1.getVariants().addAll(List.of(
                ProductVariant.builder().product(p1).attribut("Taille").valeur("XS").stockSupplementaire(5).prixDelta(BigDecimal.ZERO).build(),
                ProductVariant.builder().product(p1).attribut("Taille").valeur("S").stockSupplementaire(15).prixDelta(BigDecimal.ZERO).build(),
                ProductVariant.builder().product(p1).attribut("Taille").valeur("M").stockSupplementaire(20).prixDelta(BigDecimal.ZERO).build(),
                ProductVariant.builder().product(p1).attribut("Taille").valeur("L").stockSupplementaire(20).prixDelta(BigDecimal.ZERO).build(),
                ProductVariant.builder().product(p1).attribut("Taille").valeur("XL").stockSupplementaire(15).prixDelta(new BigDecimal("2.00")).build(),
                ProductVariant.builder().product(p1).attribut("Taille").valeur("XXL").stockSupplementaire(5).prixDelta(new BigDecimal("2.00")).build()
        ));
        productRepository.save(p1);

        // Jean slim
        Product p2 = Product.builder()
                .seller(vendeur1).nom("Jean Slim Fit Stretch")
                .description("Jean slim fit en denim stretch 98% coton, 2% élasthanne. Coupe moderne près du corps, 5 poches classiques. Disponible en bleu indigo.")
                .prix(new BigDecimal("64.99")).stock(45).actif(true).noteMoyenne(4.3).totalVentes(185)
                .images(List.of(
                        "https://source.unsplash.com/500x500/?jeans,denim",
                        "https://source.unsplash.com/500x500/?slim,jeans"
                ))
                .categories(Set.of(vetements, pantalons)).build();
        p2.getVariants().addAll(List.of(
                ProductVariant.builder().product(p2).attribut("Taille").valeur("36").stockSupplementaire(5).prixDelta(BigDecimal.ZERO).build(),
                ProductVariant.builder().product(p2).attribut("Taille").valeur("38").stockSupplementaire(10).prixDelta(BigDecimal.ZERO).build(),
                ProductVariant.builder().product(p2).attribut("Taille").valeur("40").stockSupplementaire(12).prixDelta(BigDecimal.ZERO).build(),
                ProductVariant.builder().product(p2).attribut("Taille").valeur("42").stockSupplementaire(10).prixDelta(BigDecimal.ZERO).build(),
                ProductVariant.builder().product(p2).attribut("Taille").valeur("44").stockSupplementaire(8).prixDelta(BigDecimal.ZERO).build()
        ));
        productRepository.save(p2);

        // Robe fleurie
        Product p3 = Product.builder()
                .seller(vendeur1).nom("Robe Fleurie Légère")
                .description("Robe fluide imprimé floral, 100% viscose douce au toucher. Col V, manches courtes, longueur mi-mollet. Parfaite pour les journées ensoleillées.")
                .prix(new BigDecimal("49.99")).prixPromo(new BigDecimal("37.99"))
                .stock(30).actif(true).noteMoyenne(4.7).totalVentes(95)
                .images(List.of(
                        "https://source.unsplash.com/500x500/?floral,dress",
                        "https://source.unsplash.com/500x500/?summer,dress"
                ))
                .categories(Set.of(vetements, robes)).build();
        p3.getVariants().addAll(List.of(
                ProductVariant.builder().product(p3).attribut("Taille").valeur("S").stockSupplementaire(8).prixDelta(BigDecimal.ZERO).build(),
                ProductVariant.builder().product(p3).attribut("Taille").valeur("M").stockSupplementaire(12).prixDelta(BigDecimal.ZERO).build(),
                ProductVariant.builder().product(p3).attribut("Taille").valeur("L").stockSupplementaire(7).prixDelta(BigDecimal.ZERO).build(),
                ProductVariant.builder().product(p3).attribut("Taille").valeur("XL").stockSupplementaire(3).prixDelta(BigDecimal.ZERO).build()
        ));
        productRepository.save(p3);

        // Veste en jean
        Product p4 = Product.builder()
                .seller(vendeur1).nom("Veste en Jean Oversize")
                .description("Veste en denim délavé coupe oversize. Style vintage, poches poitrine, doublure intérieure légère. Un incontournable de la mode casual.")
                .prix(new BigDecimal("89.99")).prixPromo(new BigDecimal("69.99"))
                .stock(25).actif(true).noteMoyenne(4.5).totalVentes(72)
                .images(List.of(
                        "https://source.unsplash.com/500x500/?denim,jacket",
                        "https://source.unsplash.com/500x500/?jean,jacket"
                ))
                .categories(Set.of(vetements, vestes)).build();
        p4.getVariants().addAll(List.of(
                ProductVariant.builder().product(p4).attribut("Taille").valeur("S").stockSupplementaire(5).prixDelta(BigDecimal.ZERO).build(),
                ProductVariant.builder().product(p4).attribut("Taille").valeur("M").stockSupplementaire(8).prixDelta(BigDecimal.ZERO).build(),
                ProductVariant.builder().product(p4).attribut("Taille").valeur("L").stockSupplementaire(7).prixDelta(BigDecimal.ZERO).build(),
                ProductVariant.builder().product(p4).attribut("Taille").valeur("XL").stockSupplementaire(5).prixDelta(new BigDecimal("5.00")).build()
        ));
        productRepository.save(p4);

        // ── Produits Tech Universe ─────────────────────────────────────────────

        // Écouteurs
        Product p5 = Product.builder()
                .seller(vendeur2).nom("Écouteurs Sans Fil Pro ANC")
                .description("Écouteurs true wireless avec réduction de bruit active (ANC). Autonomie 8h + 24h boîtier, résistance IPX5, son Hi-Fi avec graves profonds.")
                .prix(new BigDecimal("89.99")).prixPromo(new BigDecimal("69.99"))
                .stock(55).actif(true).noteMoyenne(4.6).totalVentes(420)
                .images(List.of(
                        "https://source.unsplash.com/500x500/?wireless,earbuds",
                        "https://source.unsplash.com/500x500/?airpods,earphones"
                ))
                .categories(Set.of(electronique, audio)).build();
        p5.getVariants().addAll(List.of(
                ProductVariant.builder().product(p5).attribut("Couleur").valeur("Noir").stockSupplementaire(20).prixDelta(BigDecimal.ZERO).build(),
                ProductVariant.builder().product(p5).attribut("Couleur").valeur("Blanc").stockSupplementaire(20).prixDelta(BigDecimal.ZERO).build(),
                ProductVariant.builder().product(p5).attribut("Couleur").valeur("Bleu Marine").stockSupplementaire(15).prixDelta(new BigDecimal("5.00")).build()
        ));
        productRepository.save(p5);

        // Casque audio
        Product p6 = Product.builder()
                .seller(vendeur2).nom("Casque Audio Bluetooth Over-Ear")
                .description("Casque circum-aural Bluetooth 5.3, réduction de bruit active hybride. Autonomie 40h, charge rapide USB-C, pliable, coussinets en mousse à mémoire.")
                .prix(new BigDecimal("149.99")).prixPromo(new BigDecimal("119.99"))
                .stock(30).actif(true).noteMoyenne(4.8).totalVentes(285)
                .images(List.of(
                        "https://source.unsplash.com/500x500/?headphones,bluetooth",
                        "https://source.unsplash.com/500x500/?over,ear,headphones"
                ))
                .categories(Set.of(electronique, audio)).build();
        p6.getVariants().addAll(List.of(
                ProductVariant.builder().product(p6).attribut("Couleur").valeur("Noir Mat").stockSupplementaire(15).prixDelta(BigDecimal.ZERO).build(),
                ProductVariant.builder().product(p6).attribut("Couleur").valeur("Argent").stockSupplementaire(10).prixDelta(new BigDecimal("10.00")).build()
        ));
        productRepository.save(p6);

        // Chargeur
        productRepository.save(Product.builder()
                .seller(vendeur2).nom("Chargeur Rapide USB-C 65W GaN")
                .description("Chargeur GaN ultra-compact 65W avec 2 ports USB-C et 1 USB-A. Compatible Power Delivery 3.0, charge en 35 min un smartphone, 2h un laptop.")
                .prix(new BigDecimal("39.99")).stock(120).actif(true).noteMoyenne(4.4).totalVentes(580)
                .images(List.of(
                        "https://source.unsplash.com/500x500/?usb,charger"
                ))
                .categories(Set.of(electronique, accessTech)).build());

        // Coque iPhone
        Product p8 = Product.builder()
                .seller(vendeur2).nom("Coque Protection iPhone 15 Pro")
                .description("Coque MagSafe en silicone liquide premium, compatible recharge sans fil. Protection anti-choc coins renforcés, toucher soyeux, dos mat anti-traces.")
                .prix(new BigDecimal("29.99")).prixPromo(new BigDecimal("22.99"))
                .stock(90).actif(true).noteMoyenne(4.2).totalVentes(315)
                .images(List.of(
                        "https://source.unsplash.com/500x500/?phone,case"
                ))
                .categories(Set.of(electronique, accessTech)).build();
        p8.getVariants().addAll(List.of(
                ProductVariant.builder().product(p8).attribut("Couleur").valeur("Noir").stockSupplementaire(25).prixDelta(BigDecimal.ZERO).build(),
                ProductVariant.builder().product(p8).attribut("Couleur").valeur("Bleu").stockSupplementaire(20).prixDelta(BigDecimal.ZERO).build(),
                ProductVariant.builder().product(p8).attribut("Couleur").valeur("Vert Sauge").stockSupplementaire(15).prixDelta(BigDecimal.ZERO).build(),
                ProductVariant.builder().product(p8).attribut("Couleur").valeur("Rose Poudré").stockSupplementaire(15).prixDelta(BigDecimal.ZERO).build(),
                ProductVariant.builder().product(p8).attribut("Couleur").valeur("Transparent").stockSupplementaire(15).prixDelta(BigDecimal.ZERO).build()
        ));
        productRepository.save(p8);

        // Montre connectée
        Product p9 = Product.builder()
                .seller(vendeur2).nom("Montre Connectée Sport Pro")
                .description("Smartwatch GPS intégré, suivi cardiaque 24h, 100+ modes sportifs. Écran AMOLED 1.43\", autonomie 14 jours, résistance 5ATM, compatible iOS/Android.")
                .prix(new BigDecimal("199.99")).prixPromo(new BigDecimal("159.99"))
                .stock(40).actif(true).noteMoyenne(4.7).totalVentes(190)
                .images(List.of(
                        "https://source.unsplash.com/500x500/?smartwatch",
                        "https://source.unsplash.com/500x500/?sport,watch"
                ))
                .categories(Set.of(electronique, accessTech, sport)).build();
        p9.getVariants().addAll(List.of(
                ProductVariant.builder().product(p9).attribut("Couleur").valeur("Noir").stockSupplementaire(15).prixDelta(BigDecimal.ZERO).build(),
                ProductVariant.builder().product(p9).attribut("Couleur").valeur("Argent").stockSupplementaire(15).prixDelta(BigDecimal.ZERO).build(),
                ProductVariant.builder().product(p9).attribut("Couleur").valeur("Or Rose").stockSupplementaire(10).prixDelta(new BigDecimal("10.00")).build()
        ));
        productRepository.save(p9);

        // ── Produits Beauté Naturelle ──────────────────────────────────────────

        // Sérum vitamine C
        productRepository.save(Product.builder()
                .seller(vendeur3).nom("Sérum Vitamine C Éclat")
                .description("Sérum concentré 15% vitamine C pure + acide hyaluronique. Texture légère à absorption rapide. Uniformise le teint, réduit les taches, protection antioxydante. 30ml.")
                .prix(new BigDecimal("34.99")).prixPromo(new BigDecimal("27.99"))
                .stock(60).actif(true).noteMoyenne(4.8).totalVentes(245)
                .images(List.of(
                        "https://source.unsplash.com/500x500/?serum,skincare"
                ))
                .categories(Set.of(beaute, soinsPeau)).build());

        // Crème hydratante
        productRepository.save(Product.builder()
                .seller(vendeur3).nom("Crème Hydratante Peaux Sensibles")
                .description("Crème riche formule douce sans parfum, sans paraben. Aloe vera bio + beurre de karité. Nourrit et apaise les peaux sensibles et réactives. 50ml.")
                .prix(new BigDecimal("22.99")).stock(75).actif(true).noteMoyenne(4.6).totalVentes(168)
                .images(List.of(
                        "https://source.unsplash.com/500x500/?face,cream"
                ))
                .categories(Set.of(beaute, soinsPeau)).build());

        // Huile argan
        productRepository.save(Product.builder()
                .seller(vendeur3).nom("Huile Capillaire Argan Bio")
                .description("Huile d'argan 100% pure et bio, pressée à froid. Nourrit, répare et fait briller les cheveux secs et abîmés. 2-3 gouttes suffisent. 30ml, certifiée COSMOS.")
                .prix(new BigDecimal("19.99")).prixPromo(new BigDecimal("15.99"))
                .stock(50).actif(true).noteMoyenne(4.7).totalVentes(132)
                .images(List.of(
                        "https://source.unsplash.com/500x500/?argan,oil,hair"
                ))
                .categories(Set.of(beaute)).build());

        // ── Produit Sport ──────────────────────────────────────────────────────

        Product p13 = Product.builder()
                .seller(vendeur1).nom("Legging Sport Gainant Taille Haute")
                .description("Legging de sport taille haute en tissu compressif 4 voies. Poche latérale zippée, coutures plates anti-irritations, séchage rapide. Idéal yoga, running, fitness.")
                .prix(new BigDecimal("44.99")).prixPromo(new BigDecimal("34.99"))
                .stock(55).actif(true).noteMoyenne(4.5).totalVentes(210)
                .images(List.of(
                        "https://source.unsplash.com/500x500/?leggings,sport",
                        "https://source.unsplash.com/500x500/?yoga,pants"
                ))
                .categories(Set.of(sport, sportVet, vetements)).build();
        p13.getVariants().addAll(List.of(
                ProductVariant.builder().product(p13).attribut("Taille").valeur("XS").stockSupplementaire(8).prixDelta(BigDecimal.ZERO).build(),
                ProductVariant.builder().product(p13).attribut("Taille").valeur("S").stockSupplementaire(12).prixDelta(BigDecimal.ZERO).build(),
                ProductVariant.builder().product(p13).attribut("Taille").valeur("M").stockSupplementaire(15).prixDelta(BigDecimal.ZERO).build(),
                ProductVariant.builder().product(p13).attribut("Taille").valeur("L").stockSupplementaire(12).prixDelta(BigDecimal.ZERO).build(),
                ProductVariant.builder().product(p13).attribut("Taille").valeur("XL").stockSupplementaire(8).prixDelta(BigDecimal.ZERO).build()
        ));
        productRepository.save(p13);

        log.info("✅ {} produits de démo initialisés.", productRepository.count());
        log.info("Connexion: client1@shopflow.com / Password1");
    }
}
