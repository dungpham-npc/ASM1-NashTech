package com.dungpham.asm1.response;

import com.dungpham.asm1.common.enums.Role;
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
    private String firstName;
    private String lastName;
    private String accessToken;
    private Role roles;
}
