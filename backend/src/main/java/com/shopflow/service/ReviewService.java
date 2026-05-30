package com.shopflow.service;

import com.shopflow.dto.request.ReviewRequest;
import com.shopflow.dto.response.ReviewResponse;
import com.shopflow.entity.Product;
import com.shopflow.entity.Review;
import com.shopflow.entity.User;
import com.shopflow.exception.BusinessException;
import com.shopflow.exception.ResourceNotFoundException;
import com.shopflow.repository.ProductRepository;
import com.shopflow.repository.ReviewRepository;
import com.shopflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReviewResponse createReview(ReviewRequest request, String email) {
        User customer = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Produit", request.getProductId()));

        // Vérifier que le client a acheté le produit
        if (!reviewRepository.hasCustomerPurchasedProduct(customer.getId(), product.getId())) {
            throw new BusinessException("Vous devez avoir acheté ce produit pour laisser un avis");
        }

        // Un seul avis par produit par client
        if (reviewRepository.existsByCustomerIdAndProductId(customer.getId(), product.getId())) {
            throw new BusinessException("Vous avez déjà laissé un avis pour ce produit");
        }

        Review review = Review.builder()
                .customer(customer)
                .product(product)
                .note(request.getNote())
                .commentaire(request.getCommentaire())
                .build();

        review = reviewRepository.save(review);
        return toResponse(review);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getProductReviews(Long productId, Pageable pageable) {
        return reviewRepository.findByProductIdAndApprouveTrue(productId, pageable).map(this::toResponse);
    }

    @Transactional
    public ReviewResponse approveReview(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Avis", id));

        review.setApprouve(true);
        review = reviewRepository.save(review);

        // Recalculer la note moyenne du produit
        Double avg = reviewRepository.calculateAverageRating(review.getProduct().getId());
        Product product = review.getProduct();
        product.setNoteMoyenne(avg != null ? avg : 0.0);
        productRepository.save(product);

        return toResponse(review);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getPendingReviews(Pageable pageable) {
        return reviewRepository.findByApprouveFalse(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getMyReviews(String email, Pageable pageable) {
        User customer = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
        return reviewRepository.findByCustomerId(customer.getId(), pageable).map(this::toResponse);
    }

    private ReviewResponse toResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .customerId(review.getCustomer().getId())
                .customerNom(review.getCustomer().getPrenom() + " " + review.getCustomer().getNom())
                .productId(review.getProduct().getId())
                .note(review.getNote())
                .commentaire(review.getCommentaire())
                .dateCreation(review.getDateCreation())
                .approuve(review.isApprouve())
                .build();
    }
}
