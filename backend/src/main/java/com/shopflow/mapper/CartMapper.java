package com.shopflow.mapper;

import com.shopflow.dto.response.CartItemResponse;
import com.shopflow.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productNom", source = "product.nom")
    @Mapping(target = "productImage", expression = "java(getFirstImage(cartItem))")
    @Mapping(target = "variantId", source = "variant.id")
    @Mapping(target = "variantAttribut", source = "variant.attribut")
    @Mapping(target = "variantValeur", source = "variant.valeur")
    @Mapping(target = "prixUnitaire", ignore = true)
    @Mapping(target = "sousTotal", ignore = true)
    @Mapping(target = "stockDisponible", ignore = true)
    CartItemResponse toResponse(CartItem cartItem);

    default String getFirstImage(CartItem cartItem) {
        if (cartItem.getProduct() != null && cartItem.getProduct().getImages() != null
            && !cartItem.getProduct().getImages().isEmpty()) {
            return cartItem.getProduct().getImages().get(0);
        }
        return null;
    }
}
