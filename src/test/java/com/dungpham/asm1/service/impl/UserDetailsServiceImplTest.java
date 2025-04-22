package com.dungpham.asm1.service.impl;

import com.dungpham.asm1.common.exception.NotFoundException;
import com.dungpham.asm1.entity.Role;
import com.dungpham.asm1.entity.User;
import com.dungpham.asm1.infrastructure.security.SecurityUserDetails;
import com.dungpham.asm1.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private User validUser;

    @BeforeEach
    void setUp() {
        Role role = new Role();
        role.setName("customer");

        validUser = User.builder()
                .email("test@example.com")
                .password("password")
                .role(role)
                .build();
    }

    @Test
    void loadUserByUsername_ExistingUser_ReturnsUserDetails() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(validUser));

        // Act
        UserDetails result = userDetailsService.loadUserByUsername("test@example.com");

        // Assert
        assertNotNull(result);
        assertInstanceOf(SecurityUserDetails.class, result);
        assertEquals("test@example.com", result.getUsername());
        assertEquals(1, result.getAuthorities().size());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER")));
        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    void loadUserByUsername_NonExistingUser_ThrowsNotFoundException() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            userDetailsService.loadUserByUsername("nonexistent@example.com");
        });

        assertEquals("404", exception.getErrorCodeString());
        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
    }

    @Test
    void getCurrentUser_AuthenticatedUser_ReturnsUser() {
        // Arrange
        SecurityUserDetails securityUserDetails = mock(SecurityUserDetails.class);
        when(securityUserDetails.getUser()).thenReturn(validUser);

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(securityUserDetails);

        // Act
        Optional<User> result = userDetailsService.getCurrentUser();

        // Assert
        assertTrue(result.isPresent());
        assertEquals(validUser, result.get());
    }

    @Test
    void getCurrentUser_NoAuthentication_ReturnsEmpty() {
        // Arrange
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(null);

        // Act
        Optional<User> result = userDetailsService.getCurrentUser();

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void getCurrentUser_NonSecurityUserDetailsPrincipal_ReturnsEmpty() {
        // Arrange
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("anonymousUser");

        // Act
        Optional<User> result = userDetailsService.getCurrentUser();

        // Assert
        assertTrue(result.isEmpty());
    }
}