package com.dungpham.asm1.response;

import com.dungpham.asm1.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class LoginResponse {
    private String email;
    private String accessToken;
    private String role;
}
