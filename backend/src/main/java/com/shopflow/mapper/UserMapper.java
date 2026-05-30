package com.shopflow.mapper;

import com.shopflow.dto.response.SellerProfileResponse;
import com.shopflow.dto.response.UserResponse;
import com.shopflow.entity.SellerProfile;
import com.shopflow.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "sellerProfile", source = "sellerProfile")
    UserResponse toResponse(User user);

    SellerProfileResponse toSellerProfileResponse(SellerProfile sellerProfile);
}
