package com.shopflow.mapper;

import com.shopflow.dto.response.ReviewResponse;
import com.shopflow.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerNom", expression = "java(review.getCustomer().getPrenom() + \" \" + review.getCustomer().getNom())")
    @Mapping(target = "productId", source = "product.id")
    ReviewResponse toResponse(Review review);
}
