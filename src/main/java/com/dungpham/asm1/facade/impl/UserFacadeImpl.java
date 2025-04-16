package com.dungpham.asm1.facade.impl;

import com.dungpham.asm1.common.enums.ErrorCode;
import com.dungpham.asm1.common.exception.ForbiddenException;
import com.dungpham.asm1.entity.User;
import com.dungpham.asm1.facade.UserFacade;
import com.dungpham.asm1.infrastructure.security.SecurityUserDetails;
import com.dungpham.asm1.request.LoginRequest;
import com.dungpham.asm1.request.RegisterRequest;
import com.dungpham.asm1.response.BaseResponse;
import com.dungpham.asm1.response.LoginResponse;
import com.dungpham.asm1.service.JwtTokenService;
import com.dungpham.asm1.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserFacadeImpl implements UserFacade {
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtTokenService jwtService;
    private final PasswordEncoder passwordEncoder;

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

    private LoginResponse buildLoginResponse(SecurityUserDetails userDetails, User user) {
        var accessToken = jwtService.generateToken(userDetails);

        return LoginResponse.builder()
                .email(user.getEmail())
                .accessToken(accessToken)
                .role(user.getRole().getName())
                .build();
    }
}
