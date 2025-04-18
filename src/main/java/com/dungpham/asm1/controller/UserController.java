package com.dungpham.asm1.controller;

import com.dungpham.asm1.entity.User;
import com.dungpham.asm1.facade.UserFacade;
import com.dungpham.asm1.infrastructure.aspect.Logged;
import com.dungpham.asm1.request.CreateUserRequest;
import com.dungpham.asm1.request.LoginRequest;
import com.dungpham.asm1.request.UpdateUserProfileRequest;
import com.dungpham.asm1.response.BaseResponse;
import com.dungpham.asm1.response.LoginResponse;
import com.dungpham.asm1.response.UserDetailsResponse;
import com.dungpham.asm1.response.UserProfileResponse;
import com.dungpham.asm1.specification.ProductSpecification;
import com.dungpham.asm1.specification.UserSpecification;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/${api.version}/users")
@RequiredArgsConstructor
public class UserController {
    private final String TAG = "Account APIs";

    private final UserFacade userFacade;
    private final PasswordEncoder passwordEncoder;  //TODO: Remove this line on release

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Login account",
            tags = {TAG})
    @Logged
    public BaseResponse<LoginResponse> login(@Validated @RequestBody LoginRequest request) {
        return this.userFacade.login(request);
    }

//    @PostMapping("/register")
//    @ResponseStatus(HttpStatus.CREATED)
//    @Operation(
//            summary = "Register account",
//            tags = {TAG})
//    public BaseResponse<LoginResponse> register(@Validated @RequestBody LoginRequest request) {
//        return this.userFacade.register(request);
//    }
//
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create user for admin",
            tags = {TAG})
    @Logged
    public BaseResponse<UserDetailsResponse> createAccount(@Validated @RequestBody CreateUserRequest request) {
        return userFacade.createUser(request);
    }

    @GetMapping("/current")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get current user profile",
            tags = {TAG})
    @SecurityRequirement(name = "Bearer Authentication")
    @Logged
    public BaseResponse<UserProfileResponse> getCurrentUserProfile() {
        return userFacade.getCurrentUserProfile();
    }

    @PutMapping("/current")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Update current user profile",
            tags = {TAG})
    @SecurityRequirement(name = "Bearer Authentication")
    @Logged
    public BaseResponse<UserProfileResponse> updateCurrentUserProfile(
            @Validated @RequestBody UpdateUserProfileRequest request) {
        return userFacade.updateCurrentUserProfile(request);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get all users for admin",
            tags = {TAG})
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

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Deactivate user for admin",
            tags = {TAG})
    @Logged
    public BaseResponse<String> deactivateUser(@PathVariable Long id) {
        return userFacade.deactivateUser(id);
    }

    @GetMapping("/password")
    @ResponseStatus(HttpStatus.OK)
    public String getTestEncodedPassword(String password) {
        return passwordEncoder.encode(password);
    } //TODO: Remove this method on release
}
