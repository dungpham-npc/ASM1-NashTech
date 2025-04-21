package com.dungpham.asm1.service.impl;

import com.dungpham.asm1.infrastructure.aspect.Logged;
import com.dungpham.asm1.infrastructure.security.SecurityUserDetails;
import com.dungpham.asm1.service.JwtTokenService;
import com.dungpham.asm1.service.RedisService;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtTokenServiceImpl implements JwtTokenService {
    @Value("${spring.jwt.secretKey}")
    private String secretKey;

    @Value("${spring.jwt.accessTokenExpirationTime}")
    private String accessTokenExpirationTime;

    private final RedisService redisService;

    private static final String TOKEN_BLACKLIST_PREFIX = "blacklisted_token:";

    @Override
    public String generateToken(SecurityUserDetails user) {
        Map<String, Object> claims = getClaims(user);
        return Jwts.builder()
                .setSubject(user.getEmail())
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() + Long.parseLong(accessTokenExpirationTime)))
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }

    @Override
    public void invalidateToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Date expiration = claims.getExpiration();
            long ttl = Math.max(0, (expiration.getTime() - System.currentTimeMillis()) / 1000);

            redisService.setValue(TOKEN_BLACKLIST_PREFIX + token, "true", ttl, TimeUnit.SECONDS);
            log.info("Token invalidated and added to blacklist");
        } catch (Exception e) {
            log.error("Error invalidating token: {}", e.getMessage());
        }
    }

    @Override
    public Boolean validateToken(String token) {
        if (null == token) return false;

        if (redisService.exists(TOKEN_BLACKLIST_PREFIX + token)) {
            log.info("Token is blacklisted");
            return false;
        }

        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parse(token);
            return true;
        } catch (MalformedJwtException
                 | ExpiredJwtException
                 | UnsupportedJwtException
                 | IllegalArgumentException e) {
            log.error("JWT validation error: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getEmailFromJwtToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        // Try both the subject and the "mail" claim
        String email = claims.getSubject();
        if (email == null || email.isEmpty()) {
            email = claims.get("mail", String.class);
        }
        return email;
    }

    private Map<String, Object> getClaims(SecurityUserDetails userDetail) {
        List<String> roles =
                userDetail.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userDetail.getId());
        claims.put("mail", userDetail.getEmail());

        log.info("User roles: {}", roles);

        // Make sure roles have ROLE_ prefix
        String role = roles.isEmpty() ? "ROLE_CUSTOMER" : roles.getFirst();
        if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role;
        }
        claims.put("roles", role);

        return claims;
    }
}
