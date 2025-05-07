package com.dungpham.asm1.facade;

import com.dungpham.asm1.entity.User;
import com.dungpham.asm1.request.*;
import com.dungpham.asm1.response.BaseResponse;
import com.dungpham.asm1.response.LoginResponse;
import com.dungpham.asm1.response.UserDetailsResponse;
import com.dungpham.asm1.response.UserProfileResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface UserFacade {
    BaseResponse<LoginResponse> login(LoginRequest request);

    BaseResponse<LoginResponse> register(RegisterRequest request);

    BaseResponse<String> logout();

    BaseResponse<String> changePassword(ChangePasswordRequest request);

    BaseResponse<String> verifyOtpAndChangePassword(OtpRequest request);

    BaseResponse<UserProfileResponse> getCurrentUserProfile();

    BaseResponse<UserProfileResponse> updateCurrentUserProfile(UpdateUserProfileRequest request);

    BaseResponse<Page<UserDetailsResponse>> getAllUsers(Specification<User> spec, Pageable pageable);

    BaseResponse<UserDetailsResponse> createUser(CreateUserRequest request);

    BaseResponse<String> deactivateUser(Long id);
}
