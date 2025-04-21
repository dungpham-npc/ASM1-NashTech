package com.dungpham.asm1.response;

import com.dungpham.asm1.entity.Role;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@Setter
public class LoginResponse {
    private String email;
    private String accessToken;
    private String role;
}
