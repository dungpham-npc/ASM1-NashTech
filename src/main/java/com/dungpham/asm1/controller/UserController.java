package com.dungpham.asm1.controller;

import com.dungpham.asm1.facade.facade.UserFacade;
import com.dungpham.asm1.request.LoginRequest;
import com.dungpham.asm1.response.BaseResponse;
import com.dungpham.asm1.response.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/${api.version}/users")
@RequiredArgsConstructor
public class UserController {
    private final UserFacade userFacade;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Login account",
            tags = {"Account APIs"})
    public BaseResponse<LoginResponse> login(@Validated @RequestBody LoginRequest request) {
        return this.userFacade.login(request);
    }

    @GetMapping("/password")
    @ResponseStatus(HttpStatus.OK)
    public String getTestEncodedPassword(String password) {
        return passwordEncoder.encode(password);
    }
}
