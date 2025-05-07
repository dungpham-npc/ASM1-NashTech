package com.dungpham.asm1.facade.impl;

import com.dungpham.asm1.common.exception.ForbiddenException;
import com.dungpham.asm1.common.exception.InvalidArgumentException;
import com.dungpham.asm1.common.exception.NotFoundException;
import com.dungpham.asm1.common.exception.UnauthorizedException;
import com.dungpham.asm1.common.mapper.UserMapper;
import com.dungpham.asm1.entity.RecipientInformation;
import com.dungpham.asm1.entity.Role;
import com.dungpham.asm1.entity.User;
import com.dungpham.asm1.infrastructure.security.SecurityUserDetails;
import com.dungpham.asm1.request.CreateUserRequest;
import com.dungpham.asm1.request.LoginRequest;
import com.dungpham.asm1.request.RecipientInfoRequest;
import com.dungpham.asm1.request.RegisterRequest;
import com.dungpham.asm1.request.UpdateUserProfileRequest;
import com.dungpham.asm1.response.BaseResponse;
import com.dungpham.asm1.response.LoginResponse;
import com.dungpham.asm1.response.UserDetailsResponse;
import com.dungpham.asm1.response.UserProfileResponse;
import com.dungpham.asm1.service.JwtTokenService;
import com.dungpham.asm1.service.MailService;
import com.dungpham.asm1.service.RoleService;
import com.dungpham.asm1.service.UserService;
import com.dungpham.asm1.service.impl.UserDetailsServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserFacadeImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserService userService;

    @Mock
    private JwtTokenService jwtService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private RoleService roleService;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private HttpServletRequest request;

    @Mock
    private MailService mailService;

    @InjectMocks
    private UserFacadeImpl userFacade;

    private User user;
    private Role role;
    private SecurityUserDetails securityUserDetails;
    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;
    private UpdateUserProfileRequest updateUserProfileRequest;
    private CreateUserRequest createUserRequest;
    private LoginResponse loginResponse;
    private UserProfileResponse userProfileResponse;
    private UserDetailsResponse userDetailsResponse;
    private RecipientInfoRequest recipientInfoRequest;
    private RecipientInformation recipientInformation;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        // Create role
        role = Role.builder()
                .name("CUSTOMER")
                .build();

        // Create user
        user = User.builder()
                .email("test@example.com")
                .password("password")
                .role(role)
                .recipientInformation(new ArrayList<>())
                .build();

        // Create recipient information
        recipientInformation = RecipientInformation.builder()
                .firstName("John")
                .lastName("Doe")
                .phone("1234567890")
                .address("123 Test St")
                .isDefault(true)
                .user(user)
                .build();
        user.getRecipientInformation().add(recipientInformation);

        // Create security user details
        securityUserDetails = new SecurityUserDetails();
        Field userField = SecurityUserDetails.class.getDeclaredField("user");
        userField.setAccessible(true);
        userField.set(securityUserDetails, user);

        // Create login request
        loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("password")
                .build();

        // Create register request
        registerRequest = RegisterRequest.builder()
                .email("test@example.com")
                .password("Password123!")
                .confirmPassword("Password123!")
                .build();

        // Create recipient info request
        recipientInfoRequest = RecipientInfoRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .phone("1234567890")
                .address("123 Test St")
                .isDefault(true)
                .build();

        // Create update user profile request
        updateUserProfileRequest = UpdateUserProfileRequest.builder()
                .email("test@example.com")
                .newPassword("newpassword")
                .confirmNewPassword("newpassword")
                .recipientInfo(Collections.singletonList(recipientInfoRequest))
                .build();

        // Create user request
        createUserRequest = CreateUserRequest.builder()
                .email("test@example.com")
                .roleId(1L)
                .build();

        // Create login response
        loginResponse = LoginResponse.builder()
                .email("test@example.com")
                .accessToken("jwt-token")
                .role("CUSTOMER")
                .build();

        // Create user profile response
        userProfileResponse = UserProfileResponse.builder()
                .email("test@example.com")
                .recipientInfo(Collections.emptyList())
                .build();

        // Create user details response
        userDetailsResponse = UserDetailsResponse.builder()
                .id(1L)
                .email("test@example.com")
                .isActive(true)
                .role("CUSTOMER")
                .build();

        // Set IDs using reflection
        try {
            Field roleIdField = role.getClass().getSuperclass().getDeclaredField("id");
            roleIdField.setAccessible(true);
            roleIdField.set(role, 1L);

            Field userIdField = user.getClass().getSuperclass().getDeclaredField("id");
            userIdField.setAccessible(true);
            userIdField.set(user, 1L);

            Field recipientInfoIdField = recipientInformation.getClass().getSuperclass().getDeclaredField("id");
            recipientInfoIdField.setAccessible(true);
            recipientInfoIdField.set(recipientInformation, 1L);
        } catch (Exception e) {
            fail("Test setup failed: " + e.getMessage());
        }

        // Mock security context
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void login_WithValidCredentials_ReturnsLoginResponse() {
        // Arrange
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(), loginRequest.getPassword());

        when(authenticationManager.authenticate(authToken)).thenReturn(authentication);
        when(userService.getUserByEmail(loginRequest.getEmail())).thenReturn(user);
        when(authentication.getPrincipal()).thenReturn(securityUserDetails);
        when(jwtService.generateToken(securityUserDetails)).thenReturn("jwt-token");
        when(userMapper.toLoginResponseWithToken(any(User.class), any(LoginResponse.class), anyString()))
                .thenReturn(loginResponse);

        // Act
        BaseResponse<LoginResponse> response = userFacade.login(loginRequest);

        // Assert
        assertTrue(response.isStatus());
        assertEquals(loginResponse, response.getData());
        verify(securityContext).setAuthentication(authentication);
        verify(userService).getUserByEmail(loginRequest.getEmail());
        verify(jwtService).generateToken(securityUserDetails);
        verify(userMapper).toLoginResponseWithToken(any(User.class), any(LoginResponse.class), anyString());
    }

    @Test
    void login_WithInactiveUser_ThrowsForbiddenException() {
        // Arrange
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(), loginRequest.getPassword());
        user.setActive(false);

        when(authenticationManager.authenticate(authToken)).thenReturn(authentication);
        when(userService.getUserByEmail(loginRequest.getEmail())).thenReturn(user);

        // Act & Assert
        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> {
            userFacade.login(loginRequest);
        });

        assertEquals("Access to this user account is forbidden", exception.getMessage());
        verify(securityContext).setAuthentication(authentication);
        verify(userService).getUserByEmail(loginRequest.getEmail());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void register_Successfully_ReturnsLoginResponse() {
        // Arrange
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                registerRequest.getEmail(), registerRequest.getPassword());

        when(userMapper.toEntity(registerRequest)).thenReturn(user);
        when(roleService.getRoleByName("CUSTOMER")).thenReturn(role);
        when(userService.createUser(user)).thenReturn(user);
        when(authenticationManager.authenticate(authToken)).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(securityUserDetails);
        when(jwtService.generateToken(securityUserDetails)).thenReturn("jwt-token");
        when(userMapper.toLoginResponseWithToken(any(User.class), any(LoginResponse.class), anyString()))
                .thenReturn(loginResponse);

        // Act
        BaseResponse<LoginResponse> response = userFacade.register(registerRequest);

        // Assert
        assertTrue(response.isStatus());
        assertEquals(loginResponse, response.getData());
        verify(userMapper).toEntity(registerRequest);
        verify(roleService).getRoleByName("CUSTOMER");
        verify(userService).createUser(user);
        verify(securityContext).setAuthentication(authentication);
        verify(jwtService).generateToken(securityUserDetails);
        verify(userMapper).toLoginResponseWithToken(any(User.class), any(LoginResponse.class), anyString());
    }

    @Test
    void register_WithFailedUserCreation_ThrowsNotFoundException() {
        // Arrange
        when(userMapper.toEntity(registerRequest)).thenReturn(user);
        when(roleService.getRoleByName("CUSTOMER")).thenReturn(role);
        when(userService.createUser(user)).thenReturn(null);

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            userFacade.register(registerRequest);
        });

        assertEquals("User not found", exception.getMessage());
        verify(userMapper).toEntity(registerRequest);
        verify(roleService).getRoleByName("CUSTOMER");
        verify(userService).createUser(user);
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void register_WithInactiveUser_ThrowsForbiddenException() {
        // Arrange
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                registerRequest.getEmail(), registerRequest.getPassword());
        user.setActive(false);

        when(userMapper.toEntity(registerRequest)).thenReturn(user);
        when(roleService.getRoleByName("CUSTOMER")).thenReturn(role);
        when(userService.createUser(user)).thenReturn(user);
        when(authenticationManager.authenticate(authToken)).thenReturn(authentication);

        // Act & Assert
        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> {
            userFacade.register(registerRequest);
        });

        assertEquals("Access to this user account is forbidden", exception.getMessage());
        verify(userMapper).toEntity(registerRequest);
        verify(roleService).getRoleByName("CUSTOMER");
        verify(userService).createUser(user);
        verify(securityContext).setAuthentication(authentication);
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void logout_WithAuthenticatedUser_InvalidatesTokenAndClearsContext() {
        // Arrange
        ServletRequestAttributes requestAttributes = mock(ServletRequestAttributes.class);
        RequestContextHolder.setRequestAttributes(requestAttributes);

        when(requestAttributes.getRequest()).thenReturn(request);
        when(request.getHeader("Authorization")).thenReturn("Bearer jwt-token");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        doNothing().when(jwtService).invalidateToken("jwt-token");

        // Act
        BaseResponse<String> response = userFacade.logout();

        // Assert
        assertTrue(response.isStatus());
        assertEquals("Logged out successfully", response.getData());
        verify(securityContext).getAuthentication();
        verify(jwtService).invalidateToken("jwt-token");

        // Cleanup
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void logout_WithoutToken_StillReturnsSuccess() {
        // Arrange
        ServletRequestAttributes requestAttributes = mock(ServletRequestAttributes.class);
        RequestContextHolder.setRequestAttributes(requestAttributes);

        when(requestAttributes.getRequest()).thenReturn(request);
        when(request.getHeader("Authorization")).thenReturn(null);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);

        // Act
        BaseResponse<String> response = userFacade.logout();

        // Assert
        assertTrue(response.isStatus());
        assertEquals("Logged out successfully", response.getData());
        verify(securityContext).getAuthentication();
        verify(jwtService, never()).invalidateToken(anyString());

        // Cleanup
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void getCurrentUserProfile_WithAuthenticatedUser_ReturnsUserProfile() {
        // Arrange
        when(userDetailsService.getCurrentUser()).thenReturn(Optional.of(user));
        when(userMapper.toUserProfileResponse(user)).thenReturn(userProfileResponse);

        // Act
        BaseResponse<UserProfileResponse> response = userFacade.getCurrentUserProfile();

        // Assert
        assertTrue(response.isStatus());
        assertEquals(userProfileResponse, response.getData());
        verify(userDetailsService).getCurrentUser();
        verify(userMapper).toUserProfileResponse(user);
    }

    @Test
    void getCurrentUserProfile_WithoutUser_ThrowsUnauthorizedException() {
        // Arrange
        when(userDetailsService.getCurrentUser()).thenReturn(Optional.empty());

        // Act & Assert
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            userFacade.getCurrentUserProfile();
        });

        assertEquals("Unauthorized access to this feature", exception.getMessage());
        verify(userDetailsService).getCurrentUser();
        verify(userMapper, never()).toUserProfileResponse(any());
    }

    @Test
    void updateCurrentUserProfile_Successfully_ReturnsUpdatedProfile() {
        // Arrange
        when(userDetailsService.getCurrentUser()).thenReturn(Optional.of(user));
        doNothing().when(userMapper).updateEntity(updateUserProfileRequest, user);
        when(userMapper.toEntity(recipientInfoRequest)).thenReturn(recipientInformation);
        when(userService.updateUserProfile(user)).thenReturn(user);
        when(userMapper.toUserProfileResponse(user)).thenReturn(userProfileResponse);

        // Act
        BaseResponse<UserProfileResponse> response = userFacade.updateCurrentUserProfile(updateUserProfileRequest);

        // Assert
        assertTrue(response.isStatus());
        assertEquals(userProfileResponse, response.getData());
        verify(userDetailsService).getCurrentUser();
        verify(userMapper).updateEntity(updateUserProfileRequest, user);
        verify(userMapper).toEntity(recipientInfoRequest);
        verify(userService).updateUserProfile(user);
        verify(userMapper).toUserProfileResponse(user);
    }

    @Test
    void updateCurrentUserProfile_WithPasswordMismatch_ThrowsInvalidArgumentException() {
        // Arrange
        UpdateUserProfileRequest requestWithMismatchedPasswords = UpdateUserProfileRequest.builder()
                .email("test@example.com")
                .newPassword("newpassword")
                .confirmNewPassword("differentpassword")
                .recipientInfo(Collections.singletonList(recipientInfoRequest))
                .build();

        when(userDetailsService.getCurrentUser()).thenReturn(Optional.of(user));

        // Act & Assert
        InvalidArgumentException exception = assertThrows(InvalidArgumentException.class, () -> {
            userFacade.updateCurrentUserProfile(requestWithMismatchedPasswords);
        });

        assertEquals("Invalid argument: Password - New password and confirmation do not match.", exception.getMessage());
        verify(userDetailsService).getCurrentUser();
        verify(userMapper, never()).updateEntity(any(), any());
        verify(userService, never()).updateUserProfile(any());
    }

    @Test
    void updateCurrentUserProfile_WithoutUser_ThrowsUnauthorizedException() {
        // Arrange
        when(userDetailsService.getCurrentUser()).thenReturn(Optional.empty());

        // Act & Assert
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            userFacade.updateCurrentUserProfile(updateUserProfileRequest);
        });

        assertEquals("Unauthorized access to this user account", exception.getMessage());
        verify(userDetailsService).getCurrentUser();
        verify(userMapper, never()).updateEntity(any(), any());
        verify(userService, never()).updateUserProfile(any());
    }

    @Test
    void getAllUsers_ReturnsPageOfUserDetailsResponse() {
        // Arrange
        Specification<User> spec = mock(Specification.class);
        Pageable pageable = mock(Pageable.class);
        List<User> users = Collections.singletonList(user);
        Page<User> userPage = new PageImpl<>(users);

        when(userService.getAllUsers(spec, pageable)).thenReturn(userPage);
        when(userMapper.toUserDetailsResponse(user)).thenReturn(userDetailsResponse);

        // Act
        BaseResponse<Page<UserDetailsResponse>> response = userFacade.getAllUsers(spec, pageable);

        // Assert
        assertTrue(response.isStatus());
        assertEquals(1, response.getData().getTotalElements());
        assertEquals(userDetailsResponse, response.getData().getContent().get(0));
        verify(userService).getAllUsers(spec, pageable);
        verify(userMapper).toUserDetailsResponse(user);
    }

    @Test
    void createUser_Successfully_ReturnsUserDetailsResponse() {
        // Arrange
        when(userMapper.toEntity(createUserRequest)).thenReturn(user);
        when(roleService.getRoleById(createUserRequest.getRoleId())).thenReturn(role);
        when(userService.createUser(user)).thenReturn(user);
        when(userMapper.toUserDetailsResponse(user)).thenReturn(userDetailsResponse);
        doNothing().when(mailService).sendMessage(
                eq(createUserRequest.getEmail()),
                anyString(),
                anyString());

        // Act
        BaseResponse<UserDetailsResponse> response = userFacade.createUser(createUserRequest);

        // Assert
        assertTrue(response.isStatus());
        assertEquals(userDetailsResponse, response.getData());
        verify(userMapper).toEntity(createUserRequest);
        verify(roleService).getRoleById(createUserRequest.getRoleId());
        verify(userService).createUser(user);
        verify(userMapper).toUserDetailsResponse(user);
        verify(mailService).sendMessage(
                eq(createUserRequest.getEmail()),
                eq("Password for ShopFun account"),
                anyString());
    }

    @Test
    void createUser_WithFailedUserCreation_ThrowsNotFoundException() {
        // Arrange
        when(userMapper.toEntity(createUserRequest)).thenReturn(user);
        when(roleService.getRoleById(createUserRequest.getRoleId())).thenReturn(role);
        when(userService.createUser(user)).thenReturn(null);
        doNothing().when(mailService).sendMessage(
                eq(createUserRequest.getEmail()),
                anyString(),
                anyString());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            userFacade.createUser(createUserRequest);
        });

        assertEquals("User not found", exception.getMessage());
        verify(userMapper).toEntity(createUserRequest);
        verify(roleService).getRoleById(createUserRequest.getRoleId());
        verify(userService).createUser(user);
        verify(mailService).sendMessage(
                eq(createUserRequest.getEmail()),
                eq("Password for ShopFun account"),
                anyString());
        verify(userMapper, never()).toUserDetailsResponse(any());
    }

    @Test
    void deactivateUser_Successfully_ReturnsSuccessMessage() {
        // Arrange
        Long userId = 1L;
        doNothing().when(userService).deactivateUser(userId);

        // Act
        BaseResponse<String> response = userFacade.deactivateUser(userId);

        // Assert
        assertTrue(response.isStatus());
        assertEquals("User deactivated successfully", response.getData());
        verify(userService).deactivateUser(userId);
    }
}