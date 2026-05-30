package com.shopflow.mapper;

import com.shopflow.dto.response.CategoryResponse;
import com.shopflow.dto.response.ProductResponse;
import com.shopflow.dto.response.VariantResponse;
import com.shopflow.entity.Category;
import com.shopflow.entity.Product;
import com.shopflow.entity.ProductVariant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "sellerId", source = "seller.id")
    @Mapping(target = "sellerNomBoutique", source = "seller.sellerProfile.nomBoutique")
    @Mapping(target = "enPromotion", expression = "java(product.isEnPromotion())")
    @Mapping(target = "pourcentageRemise", expression = "java(product.getPourcentageRemise())")
    @Mapping(target = "totalAvis", ignore = true)
    ProductResponse toResponse(Product product);

    VariantResponse toVariantResponse(ProductVariant variant);

    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "children", ignore = true)
    CategoryResponse toCategoryResponse(Category category);
}
