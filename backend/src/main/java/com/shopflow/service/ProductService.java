package com.shopflow.service;

import com.shopflow.dto.request.ProductRequest;
import com.shopflow.dto.request.VariantRequest;
import com.shopflow.dto.response.CategoryResponse;
import com.shopflow.dto.response.ProductResponse;
import com.shopflow.dto.response.VariantResponse;
import com.shopflow.entity.*;
import com.shopflow.exception.BusinessException;
import com.shopflow.exception.ResourceNotFoundException;
import com.shopflow.exception.UnauthorizedException;
import com.shopflow.repository.CategoryRepository;
import com.shopflow.repository.ProductRepository;
import com.shopflow.repository.ReviewRepository;
import com.shopflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    @Transactional
    public ProductResponse createProduct(ProductRequest request, String sellerEmail) {
        User seller = userRepository.findByEmail(sellerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Vendeur introuvable"));

        Set<Category> categories = resolveCategories(request.getCategoryIds());

        Product product = Product.builder()
                .seller(seller)
                .nom(request.getNom())
                .description(request.getDescription())
                .prix(request.getPrix())
                .prixPromo(request.getPrixPromo())
                .stock(request.getStock())
                .images(request.getImages() != null ? new java.util.ArrayList<>(request.getImages()) : new java.util.ArrayList<>())
                .categories(categories)
                .build();

        // Variantes
        if (request.getVariants() != null) {
            for (VariantRequest vr : request.getVariants()) {
                ProductVariant variant = ProductVariant.builder()
                        .product(product)
                        .attribut(vr.getAttribut())
                        .valeur(vr.getValeur())
                        .stockSupplementaire(vr.getStockSupplementaire())
                        .prixDelta(vr.getPrixDelta())
                        .build();
                product.getVariants().add(variant);
            }
        }

        product = productRepository.save(product);
        return toResponse(product);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request, String userEmail) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit", id));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        // Tout vendeur ou admin peut modifier n'importe quel produit
        boolean isAdmin = user.getRole() == Role.ADMIN;
        boolean isSeller = user.getRole() == Role.SELLER;
        if (!isAdmin && !isSeller) {
            throw new UnauthorizedException("Vous n'êtes pas autorisé à modifier ce produit");
        }

        product.setNom(request.getNom());
        product.setDescription(request.getDescription());
        product.setPrix(request.getPrix());
        product.setPrixPromo(request.getPrixPromo());
        product.setStock(request.getStock());
        product.setImages(request.getImages() != null ? new java.util.ArrayList<>(request.getImages()) : new java.util.ArrayList<>());
        product.setCategories(resolveCategories(request.getCategoryIds()));

        // Mettre à jour les variantes sans supprimer celles référencées par des commandes/paniers
        List<VariantRequest> newVariants = request.getVariants() != null ? request.getVariants() : List.of();
        List<ProductVariant> existingVariants = product.getVariants();

        // Mettre à jour les variantes existantes en place
        for (int i = 0; i < Math.min(existingVariants.size(), newVariants.size()); i++) {
            VariantRequest vr = newVariants.get(i);
            ProductVariant v = existingVariants.get(i);
            v.setAttribut(vr.getAttribut());
            v.setValeur(vr.getValeur());
            v.setStockSupplementaire(vr.getStockSupplementaire());
            v.setPrixDelta(vr.getPrixDelta());
        }
        // Ajouter les nouvelles variantes supplémentaires
        for (int i = existingVariants.size(); i < newVariants.size(); i++) {
            VariantRequest vr = newVariants.get(i);
            ProductVariant variant = ProductVariant.builder()
                    .product(product)
                    .attribut(vr.getAttribut())
                    .valeur(vr.getValeur())
                    .stockSupplementaire(vr.getStockSupplementaire())
                    .prixDelta(vr.getPrixDelta())
                    .build();
            existingVariants.add(variant);
        }
        // Supprimer les variantes en trop (seulement celles non référencées)
        if (existingVariants.size() > newVariants.size()) {
            existingVariants.subList(newVariants.size(), existingVariants.size()).clear();
        }

        return toResponse(productRepository.save(product));
    }

    @Transactional
    public void deactivateProduct(Long id, String userEmail) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit", id));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        boolean isAdmin = user.getRole() == Role.ADMIN;
        boolean isSeller = user.getRole() == Role.SELLER;
        if (!isAdmin && !isSeller) {
            throw new UnauthorizedException("Vous n'êtes pas autorisé à désactiver ce produit");
        }

        product.setActif(false);
        productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getProducts(
            Long categoryId, BigDecimal prixMin, BigDecimal prixMax,
            Long sellerId, Boolean promo, String sortBy, Pageable pageable) {

        Specification<Product> spec = Specification.where(ProductSpecification.isActive());

        if (categoryId != null) spec = spec.and(ProductSpecification.hasCategory(categoryId));
        if (prixMin != null) spec = spec.and(ProductSpecification.prixGreaterThan(prixMin));
        if (prixMax != null) spec = spec.and(ProductSpecification.prixLessThan(prixMax));
        if (sellerId != null) spec = spec.and(ProductSpecification.hasSeller(sellerId));
        if (Boolean.TRUE.equals(promo)) spec = spec.and(ProductSpecification.isEnPromotion());

        return productRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit", id));
        return toResponse(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(String q, Pageable pageable) {
        return productRepository.searchFullText(q, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getTopSelling() {
        return productRepository.findTopSelling(PageRequest.of(0, 10))
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    private Set<Category> resolveCategories(Set<Long> categoryIds) {
        Set<Category> categories = new HashSet<>();
        if (categoryIds != null) {
            for (Long catId : categoryIds) {
                Category cat = categoryRepository.findById(catId)
                        .orElseThrow(() -> new ResourceNotFoundException("Catégorie", catId));
                categories.add(cat);
            }
        }
        return categories;
    }

    public ProductResponse toResponse(Product product) {
        Set<CategoryResponse> categoryResponses = product.getCategories().stream()
                .map(c -> CategoryResponse.builder()
                        .id(c.getId())
                        .nom(c.getNom())
                        .description(c.getDescription())
                        .parentId(c.getParent() != null ? c.getParent().getId() : null)
                        .build())
                .collect(Collectors.toSet());

        List<VariantResponse> variantResponses = product.getVariants().stream()
                .map(v -> VariantResponse.builder()
                        .id(v.getId())
                        .attribut(v.getAttribut())
                        .valeur(v.getValeur())
                        .stockSupplementaire(v.getStockSupplementaire())
                        .prixDelta(v.getPrixDelta())
                        .build())
                .collect(Collectors.toList());

        String sellerBoutique = null;
        if (product.getSeller() != null && product.getSeller().getSellerProfile() != null) {
            sellerBoutique = product.getSeller().getSellerProfile().getNomBoutique();
        }

        return ProductResponse.builder()
                .id(product.getId())
                .sellerId(product.getSeller() != null ? product.getSeller().getId() : null)
                .sellerNomBoutique(sellerBoutique)
                .nom(product.getNom())
                .description(product.getDescription())
                .prix(product.getPrix())
                .prixPromo(product.getPrixPromo())
                .enPromotion(product.isEnPromotion())
                .pourcentageRemise(product.getPourcentageRemise())
                .stock(product.getStock())
                .actif(product.isActif())
                .dateCreation(product.getDateCreation())
                .images(product.getImages())
                .categories(categoryResponses)
                .variants(variantResponses)
                .noteMoyenne(product.getNoteMoyenne())
                .totalVentes(product.getTotalVentes())
                .build();
    }
}
