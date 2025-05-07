package com.dungpham.asm1.controller;

import com.dungpham.asm1.entity.User;
import com.dungpham.asm1.facade.UserFacade;
import com.dungpham.asm1.infrastructure.aspect.Logged;
import com.dungpham.asm1.request.*;
import com.dungpham.asm1.response.BaseResponse;
import com.dungpham.asm1.response.LoginResponse;
import com.dungpham.asm1.response.UserDetailsResponse;
import com.dungpham.asm1.response.UserProfileResponse;
import com.dungpham.asm1.service.MailService;
import com.dungpham.asm1.specification.ProductSpecification;
import com.dungpham.asm1.specification.UserSpecification;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/${api.version}/users")
@RequiredArgsConstructor
@Validated
public class UserController {
    private final String TAG = "Account APIs";

    private final UserFacade userFacade;

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Login account",
            tags = {TAG})
    @Logged
    public BaseResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return userFacade.login(request);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Logout account",
            tags = {TAG})
    @SecurityRequirement(name = "Bearer Authentication")
    @Logged
    public BaseResponse<String> logout() {
        return userFacade.logout();
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Register account",
            tags = {TAG})
    public BaseResponse<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        return userFacade.register(request);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create user for admin",
            tags = {TAG})
    @SecurityRequirement(name = "Bearer Authentication")
    @Logged
    public BaseResponse<UserDetailsResponse> createAccount(@Valid @RequestBody CreateUserRequest request) {
        return userFacade.createUser(request);
    }

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get current user profile",
            tags = {TAG})
    @SecurityRequirement(name = "Bearer Authentication")
    @Logged
    public BaseResponse<UserProfileResponse> getCurrentUserProfile() {
        return userFacade.getCurrentUserProfile();
    }

    @PutMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Update current user profile",
            tags = {TAG})
    @SecurityRequirement(name = "Bearer Authentication")
    @Logged
    public BaseResponse<UserProfileResponse> updateCurrentUserProfile(
            @Valid @RequestBody UpdateUserProfileRequest request) {
        return userFacade.updateCurrentUserProfile(request);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get all users for admin",
            tags = {TAG})
    @SecurityRequirement(name = "Bearer Authentication")
    @Logged
    public BaseResponse<Page<UserDetailsResponse>> getAllUsers(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Long roleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort) {

        Sort sortObj = Sort.by(Sort.Direction.fromString(sort[1]), sort[0]);
        Pageable pageable = PageRequest.of(page, size, sortObj);

        Specification<User> spec = Specification
                .where(UserSpecification.hasEmail(email))
                .and(UserSpecification.hasRole(roleId));

        return userFacade.getAllUsers(spec, pageable);
    }

    @PostMapping("/change-password")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Change password",
            tags = {TAG})
    @SecurityRequirement(name = "Bearer Authentication")
    @Logged
    public BaseResponse<String> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        return userFacade.changePassword(request);
    }

    @PostMapping("/verify-otp")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Verify OTP and complete password change",
            tags = {TAG})
    @SecurityRequirement(name = "Bearer Authentication")
    @Logged
    public BaseResponse<String> verifyOtpAndChangePassword(@Valid @RequestBody OtpRequest request) {
        return userFacade.verifyOtpAndChangePassword(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Deactivate user for admin",
            tags = {TAG})
    @SecurityRequirement(name = "Bearer Authentication")
    @Logged
    public BaseResponse<String> deactivateUser(@PathVariable Long id) {
        return userFacade.deactivateUser(id);
    }

    @PutMapping("/{id}/activate")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Activate user for admin",
            tags = {TAG})
    @SecurityRequirement(name = "Bearer Authentication")
    @Logged
    public BaseResponse<String> activateUser(@PathVariable Long id) {
        return userFacade.activateUser(id);
    }
}
