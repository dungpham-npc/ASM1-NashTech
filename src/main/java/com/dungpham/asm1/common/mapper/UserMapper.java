package com.dungpham.asm1.common.mapper;

import com.dungpham.asm1.common.util.Util;
import com.dungpham.asm1.entity.RecipientInformation;
import com.dungpham.asm1.entity.User;
import com.dungpham.asm1.request.CreateUserRequest;
import com.dungpham.asm1.request.RecipientInfoRequest;
import com.dungpham.asm1.request.RegisterRequest;
import com.dungpham.asm1.request.UpdateUserProfileRequest;
import com.dungpham.asm1.response.LoginResponse;
import com.dungpham.asm1.response.RecipientInfoResponse;
import com.dungpham.asm1.response.UserDetailsResponse;
import com.dungpham.asm1.response.UserProfileResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {Util.class})
public interface UserMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "active", target = "isActive")
    @Mapping(source = "createdAt", target = "createdAt", qualifiedByName = "convertTimestampToLocalDateTime")
    @Mapping(source = "updatedAt", target = "updatedAt", qualifiedByName = "convertTimestampToLocalDateTime")
    @Mapping(source = "role.name", target = "role")
    UserDetailsResponse toUserDetailsResponse(User user);

    @Mapping(source = "email", target = "email")
    @Mapping(source = "recipientInformation", target = "recipientInfo")
    UserProfileResponse toUserProfileResponse(User user);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "firstName", target = "firstName")
    @Mapping(source = "lastName", target = "lastName")
    @Mapping(source = "phone", target = "phone")
    @Mapping(source = "address", target = "address")
    @Mapping(source = "isDefault", target = "isDefault")
    RecipientInfoResponse toRecipientInfoResponse(RecipientInformation recipientInformation);

    @Mapping(source = "email", target = "email")
    @Mapping(source = "role.name", target = "role")
    @Mapping(target = "accessToken", ignore = true)
    LoginResponse toLoginResponse(User user);

    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "user.role.name", target = "role")
    @Mapping(source = "accessToken", target = "accessToken")
    LoginResponse toLoginResponseWithToken(User user, @MappingTarget LoginResponse loginResponse, String accessToken);

    @Mapping(source = "email", target = "email")
    @Mapping(source = "password", target = "password")
    @Mapping(target = "role", ignore = true)
    User toEntity(RegisterRequest request);

    @Mapping(source = "email", target = "email")
    @Mapping(target = "role", ignore = true)
    User toEntity(CreateUserRequest request);

    @Mapping(source = "email", target = "email")
    @Mapping(source = "newPassword", target = "password")
    @Mapping(target = "recipientInformation", ignore = true)
    @Mapping(target = "role", ignore = true)
    void updateEntity(UpdateUserProfileRequest request, @MappingTarget User user);

    @Mapping(source = "firstName", target = "firstName")
    @Mapping(source = "lastName", target = "lastName")
    @Mapping(source = "phone", target = "phone")
    @Mapping(source = "address", target = "address")
    @Mapping(source = "isDefault", target = "isDefault", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
    @Mapping(target = "user", ignore = true)
    RecipientInformation toEntity(RecipientInfoRequest request);
}