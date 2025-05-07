package com.dungpham.asm1.facade.impl;

import com.dungpham.asm1.common.exception.*;
import com.dungpham.asm1.common.mapper.UserMapper;
import com.dungpham.asm1.common.util.Util;
import com.dungpham.asm1.entity.User;
import com.dungpham.asm1.facade.UserFacade;
import com.dungpham.asm1.infrastructure.aspect.Logged;
import com.dungpham.asm1.infrastructure.security.SecurityUserDetails;
import com.dungpham.asm1.request.*;
import com.dungpham.asm1.response.*;
import com.dungpham.asm1.service.*;
import com.dungpham.asm1.service.impl.UserDetailsServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserFacadeImpl implements UserFacade {
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtTokenService jwtService;
    private final UserMapper userMapper;
    private final RoleService roleService;
    private final UserDetailsServiceImpl userDetailsService;
    private final MailService mailService;
    private final RedisService redisService;

    @Override
    @Logged
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
    @Logged
    public BaseResponse<LoginResponse> register(RegisterRequest request) {
        User user = userMapper.toEntity(request);
        user.setRole(roleService.getRoleByName("CUSTOMER"));

        User createdUser = userService.createUser(user);
        if (createdUser == null) {
            throw new NotFoundException("User");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new InvalidArgumentException("Password", "New password and confirmation do not match.");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        if (!createdUser.isActive()) {
            throw new ForbiddenException("this user account");
        }

        SecurityUserDetails userPrinciple = (SecurityUserDetails) authentication.getPrincipal();
        return BaseResponse.build(buildLoginResponse(userPrinciple, createdUser), true);
    }

    @Override
    @Logged
    public BaseResponse<String> logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            String token = extractTokenFromCurrentRequest();

            if (token != null) {
                jwtService.invalidateToken(token);

                SecurityContextHolder.clearContext();

                log.info("User successfully logged out");
            } else {
                log.warn("No token found in the request during logout");
            }
        }

        return BaseResponse.build("Logged out successfully", true);
    }

    @Override
    @Logged
    public BaseResponse<String> changePassword(ChangePasswordRequest request) {
        String oldPassword = request.getOldPassword();
        String newPassword = request.getNewPassword();
        User user = userDetailsService.getCurrentUser()
                .orElseThrow(() -> new UnauthorizedException("this feature"));

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getEmail(), oldPassword));

        if (!authentication.isAuthenticated()) {
            throw new InvalidArgumentException("oldPassword", "Current password is incorrect.");
        }

        String otp = generateSixDigitOtp();

        String otpKey = "otp:" + user.getEmail();
        redisService.setValue(otpKey, otp, 5, TimeUnit.MINUTES);

        mailService.sendMessage(
                user.getEmail(),
                "Password change OTP for ShopFun account",
                "Your OTP for password change is: " + otp + "\nThis code will expire in 5 minutes."
        );

        String passwordKey = "password_change:" + user.getEmail();
        redisService.setValue(passwordKey, newPassword, 5, TimeUnit.MINUTES);

        return BaseResponse.build("OTP sent to your email. Please verify to complete password change.", true);
    }

    @Override
    @Logged
    public BaseResponse<String> verifyOtpAndChangePassword(OtpRequest request) {
        String otp = request.getOtp();
        User currentUser = userDetailsService.getCurrentUser()
                .orElseThrow(() -> new UnauthorizedException("this feature"));

        String email = currentUser.getEmail();

        String otpKey = "otp:" + email;
        String passwordKey = "password_change:" + email;

        if (!redisService.exists(otpKey)) {
            throw new InvalidArgumentException("otp", "OTP expired or invalid.");
        }

        String storedOtp = redisService.getValue(otpKey);
        if (!storedOtp.equals(otp)) {
            throw new InvalidArgumentException("otp", "Invalid OTP.");
        }

        if (!redisService.exists(passwordKey)) {
            throw new InvalidArgumentException("password", "Password change session expired.");
        }
        String newPassword = redisService.getValue(passwordKey);

        User user = userService.getUserByEmail(email);
        userService.updatePassword(user, newPassword);

        redisService.deleteKey(otpKey);
        redisService.deleteKey(passwordKey);

        return BaseResponse.build("Password changed successfully.", true);
    }

    private String generateSixDigitOtp() {
        StringBuilder otp = new StringBuilder();
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < 6; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }

    private String extractTokenFromCurrentRequest() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = attr.getRequest();

        String headerAuth = request.getHeader("Authorization");
        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }

    @Override
    @Logged
    public BaseResponse<UserProfileResponse> getCurrentUserProfile() {
        User user = userDetailsService.getCurrentUser()
                .orElseThrow(() -> new UnauthorizedException("this feature"));

        return BaseResponse.build(userMapper.toUserProfileResponse(user), true);
    }

    @Override
    @Logged
    public BaseResponse<UserProfileResponse> updateCurrentUserProfile(UpdateUserProfileRequest request) {
        User user = userDetailsService.getCurrentUser()
                .orElseThrow(() -> new UnauthorizedException("this user account"));

        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new InvalidArgumentException("Password", "New password and confirmation do not match.");
        }

        userMapper.updateEntity(request, user);
        user.getRecipientInformation().clear();
        user.getRecipientInformation().addAll(request.getRecipientInfo()
                .stream()
                .map(userMapper::toEntity)
                .peek(recipientInformation -> recipientInformation.setUser(user))
                .toList());

        return BaseResponse.build(userMapper.toUserProfileResponse(userService.updateUserProfile(user)), true);
    }

    @Override
    @Logged
    public BaseResponse<Page<UserDetailsResponse>> getAllUsers(Specification<User> spec, Pageable pageable) {
        Page<User> userPage = userService.getAllUsers(spec, pageable);

        return BaseResponse.build(userPage.map(userMapper::toUserDetailsResponse), true);
    }

    @Override
    @Logged
    public BaseResponse<UserDetailsResponse> createUser(CreateUserRequest request) {
        User user = userMapper.toEntity(request);
        user.setRole(roleService.getRoleById(request.getRoleId()));

        String password = Util.generateSecurePassword(15);
        user.setPassword(password);
        mailService.sendMessage(request.getEmail(), "Password for ShopFun account", "Your password is: " + password + ".\nPlease change it after logging in.");

        User createdUser = userService.createUser(user);
        if (createdUser == null) {
            throw new NotFoundException("User");
        }

        return BaseResponse.build(userMapper.toUserDetailsResponse(createdUser), true);
    }

    @Override
    @Logged
    public BaseResponse<String> deactivateUser(Long id) {
        userService.deactivateUser(id);
        return BaseResponse.build("User deactivated successfully", true);
    }

    private LoginResponse buildLoginResponse(SecurityUserDetails userDetails, User user) {
        String accessToken = jwtService.generateToken(userDetails);

        return userMapper.toLoginResponseWithToken(user, LoginResponse.builder().build(), accessToken);
    }
}
