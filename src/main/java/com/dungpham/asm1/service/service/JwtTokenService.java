package com.dungpham.asm1.service.service;

import com.dungpham.asm1.infrastructure.security.SecurityUserDetails;

public interface JwtTokenService {
    String generateToken(SecurityUserDetails user);

    Boolean validateToken(String token);

    String getEmailFromJwtToken(String token);
}