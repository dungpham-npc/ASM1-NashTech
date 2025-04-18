package com.dungpham.asm1.facade.impl;

import com.dungpham.asm1.common.enums.ErrorCode;
import com.dungpham.asm1.common.exception.*;
import com.dungpham.asm1.common.util.Util;
import com.dungpham.asm1.entity.RecipientInformation;
import com.dungpham.asm1.entity.User;
import com.dungpham.asm1.facade.UserFacade;
import com.dungpham.asm1.infrastructure.aspect.Logged;
import com.dungpham.asm1.infrastructure.security.SecurityUserDetails;
import com.dungpham.asm1.request.*;
import com.dungpham.asm1.response.*;
import com.dungpham.asm1.service.JwtTokenService;
import com.dungpham.asm1.service.RoleService;
import com.dungpham.asm1.service.UserService;
import com.dungpham.asm1.service.impl.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserFacadeImpl implements UserFacade {
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtTokenService jwtService;
    private final ModelMapper modelMapper;
    private final RoleService roleService;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    public BaseResponse<LoginResponse> login(LoginRequest request) {
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        User user = userService.getUserByEmail(request.getEmail());

        boolean isNotActive = !user.isActive();
        if (isNotActive) throw new ForbiddenException("this user account");

        SecurityUserDetails userPrinciple = (SecurityUserDetails) authentication.getPrincipal();
        return BaseResponse.build(buildLoginResponse(userPrinciple, user), true);
    }

    @Override
    public BaseResponse<LoginResponse> register(RegisterRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public BaseResponse<UserProfileResponse> getCurrentUserProfile() {
        User user = userDetailsService.getCurrentUser()
                .orElseThrow(() -> new UnauthorizedException("this user account"));

        return BaseResponse.build(toUserProfileResponse(user), true);
    }

    @Override
    @Logged
    public BaseResponse<UserProfileResponse> updateCurrentUserProfile(UpdateUserProfileRequest request) {
        User user = userDetailsService.getCurrentUser()
                .orElseThrow(() -> new UnauthorizedException("this user account"));

        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new InvalidArgumentException("Password", "New password and confirmation do not match.");
        }

        user.setEmail(request.getEmail());
        user.setPassword(request.getNewPassword());
        user.getRecipientInformation().clear();
        user.getRecipientInformation().addAll(request.getRecipientInfo()
                .stream()
                .map(recipientInfo -> {
                    RecipientInformation recipientInformation = modelMapper.map(recipientInfo, RecipientInformation.class);
                    recipientInformation.setUser(user);
                    recipientInformation.setIsDefault(recipientInfo.getIsDefault() != null ?
                            recipientInfo.getIsDefault() : false);
                    return recipientInformation;
                })
                .toList());

        return BaseResponse.build(toUserProfileResponse(userService.updateUserProfile(user)), true);
    }

    @Override
    public BaseResponse<Page<UserDetailsResponse>> getAllUsers(Specification<User> spec, Pageable pageable) {
        Page<User> userPage = userService.getAllUsers(spec, pageable);

        return BaseResponse.build(userPage.map(user -> {
            UserDetailsResponse response = modelMapper.map(user, UserDetailsResponse.class);
            response.setCreatedAt(Util.convertTimestampToLocalDateTime(user.getCreatedAt()));
            response.setUpdatedAt(Util.convertTimestampToLocalDateTime(user.getUpdatedAt()));
            response.setRole(user.getRole().getName());
            return response;
        }), true);
    }

    @Override
    public BaseResponse<UserDetailsResponse> createUser(CreateUserRequest request) {
        User user = modelMapper.map(request, User.class);
        user.setRole(roleService.getRoleById(request.getRoleId()));

        User createdUser = userService.createUser(user);
        if (createdUser == null) {
            throw new NotFoundException("User");
        }
        UserDetailsResponse response = modelMapper.map(user, UserDetailsResponse.class);
        response.setCreatedAt(Util.convertTimestampToLocalDateTime(user.getCreatedAt()));
        response.setUpdatedAt(Util.convertTimestampToLocalDateTime(user.getUpdatedAt()));
        response.setRole(user.getRole().getName());

        return BaseResponse.build(response, true);
    }

    @Override
    public BaseResponse<String> deactivateUser(Long id) {
        userService.deactivateUser(id);
        return BaseResponse.build("User deactivated successfully", true);
    }

    private LoginResponse buildLoginResponse(SecurityUserDetails userDetails, User user) {
        var accessToken = jwtService.generateToken(userDetails);

        return LoginResponse.builder()
                .email(user.getEmail())
                .accessToken(accessToken)
                .role(user.getRole().getName())
                .build();
    }

    private UserProfileResponse toUserProfileResponse(User user) {
        List<RecipientInfoResponse> recipientInfoList = user.getRecipientInformation()
                .stream()
                .map(recipientInformation -> modelMapper.map(recipientInformation, RecipientInfoResponse.class))
                .toList();

        return UserProfileResponse.builder()
                .email(user.getEmail())
                .recipientInfo(recipientInfoList)
                .build();
    }
}
