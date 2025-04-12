package com.dungpham.asm1.facade;

import com.dungpham.asm1.request.LoginRequest;
import com.dungpham.asm1.request.RegisterRequest;
import com.dungpham.asm1.response.BaseResponse;
import com.dungpham.asm1.response.LoginResponse;

public interface UserFacade {
    BaseResponse<LoginResponse> login(LoginRequest request);

    BaseResponse<LoginResponse> register(RegisterRequest request);
}
