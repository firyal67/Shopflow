package com.shopflow.mapper;

import com.shopflow.dto.response.OrderItemResponse;
import com.shopflow.dto.response.OrderResponse;
import com.shopflow.entity.Order;
import com.shopflow.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "couponCode", source = "coupon.code")
    OrderResponse toResponse(Order order);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productNom", source = "product.nom")
    @Mapping(target = "productImage", expression = "java(getFirstImage(orderItem))")
    @Mapping(target = "variantId", source = "variant.id")
    @Mapping(target = "variantAttribut", source = "variant.attribut")
    @Mapping(target = "variantValeur", source = "variant.valeur")
    @Mapping(target = "sousTotal", expression = "java(orderItem.getPrixUnitaire().multiply(java.math.BigDecimal.valueOf(orderItem.getQuantite())))")
    OrderItemResponse toOrderItemResponse(OrderItem orderItem);

    default String getFirstImage(OrderItem orderItem) {
        if (orderItem.getProduct() != null && orderItem.getProduct().getImages() != null 
            && !orderItem.getProduct().getImages().isEmpty()) {
            return orderItem.getProduct().getImages().get(0);
        }
        return null;
    }
}
