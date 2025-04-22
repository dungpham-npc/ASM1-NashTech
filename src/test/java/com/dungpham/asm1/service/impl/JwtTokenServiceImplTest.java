package com.dungpham.asm1.service.impl;

import com.dungpham.asm1.infrastructure.security.SecurityUserDetails;
import com.dungpham.asm1.service.RedisService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtTokenServiceImplTest {

    @Mock
    private RedisService redisService;

    @InjectMocks
    private JwtTokenServiceImpl jwtTokenService;

    private SecurityUserDetails userDetails;
    private String secretKey;
    private final String accessTokenExpirationTime = "3600000"; // 1 hour in milliseconds

    @BeforeEach
    void setUp() {
        // Generate a secure key for HS512 algorithm
        secretKey = Base64.getEncoder().encodeToString(Keys.secretKeyFor(SignatureAlgorithm.HS512).getEncoded());
        
        // Set up the secret key and expiration time using reflection
        ReflectionTestUtils.setField(jwtTokenService, "secretKey", secretKey);
        ReflectionTestUtils.setField(jwtTokenService, "accessTokenExpirationTime", accessTokenExpirationTime);

        // Create a mock user details
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_CUSTOMER")
        );

        userDetails = SecurityUserDetails.builder()
                .id(1L)
                .email("test@example.com")
                .password("password")
                .authorities(authorities)
                .build();
    }

    @Test
    void generateToken_Successfully() {
        // Act
        String token = jwtTokenService.generateToken(userDetails);

        // Assert
        assertNotNull(token);
        assertTrue(token.length() > 0);

        // Verify the token can be parsed
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertEquals("test@example.com", claims.getSubject());
        assertEquals(1, claims.get("userId"));
        assertEquals("test@example.com", claims.get("mail"));
        assertEquals("ROLE_CUSTOMER", claims.get("roles"));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());

        // Verify expiration time is set correctly (with some tolerance for test execution time)
        long expectedExpirationTime = System.currentTimeMillis() + Long.parseLong(accessTokenExpirationTime);
        long actualExpirationTime = claims.getExpiration().getTime();
        assertTrue(Math.abs(expectedExpirationTime - actualExpirationTime) < 5000); // 5 seconds tolerance
    }

    @Test
    void validateToken_ValidToken_ReturnsTrue() {
        // Arrange
        String token = jwtTokenService.generateToken(userDetails);
        when(redisService.exists(anyString())).thenReturn(false);

        // Act
        boolean isValid = jwtTokenService.validateToken(token);

        // Assert
        assertTrue(isValid);
        verify(redisService).exists("blacklisted_token:" + token);
    }

    @Test
    void invalidateToken_Successfully() {
        // Arrange
        String token = jwtTokenService.generateToken(userDetails);
        
        // Act
        jwtTokenService.invalidateToken(token);

        // Assert
        // Verify that the token is added to the blacklist with the correct TTL
        verify(redisService).setValue(eq("blacklisted_token:" + token), eq("true"), anyLong(), eq(TimeUnit.SECONDS));
    }

    @Test
    void getEmailFromJwtToken_Successfully() {
        // Arrange
        String token = jwtTokenService.generateToken(userDetails);

        // Act
        String email = jwtTokenService.getEmailFromJwtToken(token);

        // Assert
        assertEquals("test@example.com", email);
    }

    @Test
    void validateToken_BlacklistedToken_ReturnsFalse() {
        // Arrange
        String token = jwtTokenService.generateToken(userDetails);
        when(redisService.exists("blacklisted_token:" + token)).thenReturn(true);

        // Act
        boolean isValid = jwtTokenService.validateToken(token);

        // Assert
        assertFalse(isValid);
        verify(redisService).exists("blacklisted_token:" + token);
    }

    @Test
    void validateToken_NullToken_ReturnsFalse() {
        // Act
        boolean isValid = jwtTokenService.validateToken(null);

        // Assert
        assertFalse(isValid);
        verify(redisService, never()).exists(anyString());
    }

    @Test
    void validateToken_MalformedToken_ReturnsFalse() {
        // Arrange
        String malformedToken = "invalid.jwt.token";
        when(redisService.exists(anyString())).thenReturn(false);

        // Act
        boolean isValid = jwtTokenService.validateToken(malformedToken);

        // Assert
        assertFalse(isValid);
        verify(redisService).exists("blacklisted_token:" + malformedToken);
    }

    @Test
    void invalidateToken_HandlesException() {
        // Arrange
        String invalidToken = "invalid.token";
        
        // Act
        jwtTokenService.invalidateToken(invalidToken);
        
        // Assert
        // Verify that no value is set in Redis when token is invalid
        verify(redisService, never()).setValue(anyString(), anyString(), anyLong(), any(TimeUnit.class));
    }

    @Test
    void validateToken_ExpiredToken_ReturnsFalse() {
        // Arrange
        String expiredToken = Jwts.builder()
                .setSubject(userDetails.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() - 1000)) // Already expired
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();

        when(redisService.exists(anyString())).thenReturn(false);

        // Act
        boolean isValid = jwtTokenService.validateToken(expiredToken);

        // Assert
        assertFalse(isValid);
        verify(redisService).exists("blacklisted_token:" + expiredToken);
    }

    @Test
    void validateToken_IllegalArgumentToken_ReturnsFalse() {
        // Arrange
        String illegalToken = "";
        when(redisService.exists(anyString())).thenReturn(false);

        // Act
        boolean isValid = jwtTokenService.validateToken(illegalToken);

        // Assert
        assertFalse(isValid);
        verify(redisService).exists("blacklisted_token:" + illegalToken);
    }

    @Test
    void getEmailFromJwtToken_WithoutSubjectButWithMailClaim_ReturnsEmail() {
        // Arrange
        // Create a token without subject but with mail claim
        String token = Jwts.builder()
                .claim("mail", "test@example.com")
                .claim("userId", 1L)
                .claim("roles", "ROLE_CUSTOMER")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + Long.parseLong(accessTokenExpirationTime)))
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();

        // Act
        String email = jwtTokenService.getEmailFromJwtToken(token);

        // Assert
        assertEquals("test@example.com", email);
    }

    @Test
    void generateToken_WithNonPrefixedRole_AddsRolePrefix() {
        // Arrange
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("CUSTOMER") // No ROLE_ prefix
        );

        SecurityUserDetails userWithoutRolePrefix = SecurityUserDetails.builder()
                .id(1L)
                .email("test@example.com")
                .password("password")
                .authorities(authorities)
                .build();

        // Act
        String token = jwtTokenService.generateToken(userWithoutRolePrefix);

        // Assert
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertEquals("ROLE_CUSTOMER", claims.get("roles"));
    }

    @Test
    void generateToken_WithEmptyAuthorities_UsesDefaultRole() {
        // Arrange
        SecurityUserDetails userWithoutAuthorities = SecurityUserDetails.builder()
                .id(1L)
                .email("test@example.com")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        // Act
        String token = jwtTokenService.generateToken(userWithoutAuthorities);

        // Assert
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertEquals("ROLE_CUSTOMER", claims.get("roles"));
    }

    @Test
    void invalidateToken_WithValidToken_CalculatesCorrectTTL() {
        // Arrange
        // Create a token with known expiration time
        long currentTime = System.currentTimeMillis();
        long expirationTime = currentTime + 60000; // 1 minute from now

        String token = Jwts.builder()
                .setSubject(userDetails.getEmail())
                .setIssuedAt(new Date(currentTime))
                .setExpiration(new Date(expirationTime))
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();

        // Act
        jwtTokenService.invalidateToken(token);

        // Assert
        // Verify that the TTL is correctly calculated (about 60 seconds with some tolerance)
        verify(redisService).setValue(eq("blacklisted_token:" + token), eq("true"), longThat(ttl ->
                ttl > 50 && ttl <= 60), eq(TimeUnit.SECONDS));
    }
}