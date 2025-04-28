package com.dungpham.asm1.controller;

import com.dungpham.asm1.entity.User;
import com.dungpham.asm1.facade.UserFacade;
import com.dungpham.asm1.request.CreateUserRequest;
import com.dungpham.asm1.request.LoginRequest;
import com.dungpham.asm1.request.RegisterRequest;
import com.dungpham.asm1.request.UpdateUserProfileRequest;
import com.dungpham.asm1.response.BaseResponse;
import com.dungpham.asm1.response.LoginResponse;
import com.dungpham.asm1.response.UserDetailsResponse;
import com.dungpham.asm1.response.UserProfileResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserFacade userFacade;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void login_ValidCredentials_ReturnsLoginResponse() throws Exception {
        // Arrange
        LoginRequest loginRequest = LoginRequest.builder()
                .email("dungpham@gmail.com")
                .password("Secured123!")
                .build();

        LoginResponse loginResponse = LoginResponse.builder()
                .accessToken("jwt-token")
                .email("test@example.com")
                .role("USER")
                .build();

        when(userFacade.login(any(LoginRequest.class)))
                .thenReturn(BaseResponse.build(loginResponse, true));

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("200")))
                .andExpect(jsonPath("$.message", is("Success")))
                .andExpect(jsonPath("$.data.accessToken", is("jwt-token")));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"ADMIN", "CUSTOMER"})
    void logout_AuthenticatedUser_LogsOutSuccessfully() throws Exception {
        // Arrange
        when(userFacade.logout())
                .thenReturn(BaseResponse.build("Logout successful", true));

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/logout")
                        .with(csrf()))  // Remove the additional user() method call
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("200")))
                .andExpect(jsonPath("$.message", is("Success")))
                .andExpect(jsonPath("$.data", is("Logout successful")));
    }

    @Test
    void register_ValidUserData_RegistersSuccessfully() throws Exception {
        // Arrange
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("dungpham@gmail.com")
                .password("Secured123!")
                .confirmPassword("Secured123!")
                .build();

        LoginResponse loginResponse = LoginResponse.builder()
                .accessToken("jwt-token")
                .email("new@example.com")
                .role("CUSTOMER")
                .build();

        when(userFacade.register(any(RegisterRequest.class)))
                .thenReturn(BaseResponse.build(loginResponse, true));

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code", is("200")))
                .andExpect(jsonPath("$.message", is("Success")))
                .andExpect(jsonPath("$.data.accessToken", is("jwt-token")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createAccount_AdminUser_CreatesUserSuccessfully() throws Exception {
        // Arrange
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .email("user@email.com")
                .password("Secured123!")
                .roleId(2L)
                .build();

        UserDetailsResponse userResponse = UserDetailsResponse.builder()
                .id(1L)
                .email("new@example.com")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .role("USER")
                .build();

        when(userFacade.createUser(any(CreateUserRequest.class)))
                .thenReturn(BaseResponse.build(userResponse, true));

        // Act & Assert
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserRequest))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code", is("200")))
                .andExpect(jsonPath("$.message", is("Success")))
                .andExpect(jsonPath("$.data.id", is(1)))
                .andExpect(jsonPath("$.data.email", is("new@example.com")));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void getCurrentUserProfile_AuthenticatedUser_ReturnsProfile() throws Exception {
        // Arrange
        UserProfileResponse profileResponse = UserProfileResponse.builder()
                .email("user@example.com")
                .recipientInfo(List.of())
                .build();

        when(userFacade.getCurrentUserProfile())
                .thenReturn(BaseResponse.build(profileResponse, true));

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/current")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("200")))
                .andExpect(jsonPath("$.message", is("Success")))
                .andExpect(jsonPath("$.data.email", is("user@example.com")));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void updateCurrentUserProfile_ValidData_UpdatesSuccessfully() throws Exception {
        // Arrange
        UpdateUserProfileRequest updateRequest = UpdateUserProfileRequest.builder()
                .email("user@example.com")
                .newPassword("NewPassword123!")
                .confirmNewPassword("NewPassword123!")
                .recipientInfo(List.of())
                .build();

        UserProfileResponse updatedProfile = UserProfileResponse.builder()
                .email("user@example.com")
                .recipientInfo(List.of())
                .build();

        when(userFacade.updateCurrentUserProfile(any(UpdateUserProfileRequest.class)))
                .thenReturn(BaseResponse.build(updatedProfile, true));

        // Act & Assert
        mockMvc.perform(put("/api/v1/users/current")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("200")))
                .andExpect(jsonPath("$.message", is("Success")))
                .andExpect(jsonPath("$.data.email", is("user@example.com")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_WithFilters_ReturnsFilteredUsers() throws Exception {
        // Arrange
        UserDetailsResponse user = UserDetailsResponse.builder()
                .id(1L)
                .email("admin@example.com")
                .role("ADMIN")
                .isActive(true)
                .build();

        Page<UserDetailsResponse> userPage = new PageImpl<>(
                List.of(user),
                Pageable.ofSize(10),
                1
        );

        when(userFacade.getAllUsers(any(), any()))
                .thenReturn(BaseResponse.build(userPage, true));

        // Act & Assert
        mockMvc.perform(get("/api/v1/users")
                        .param("email", "admin")
                        .param("roleId", "1")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "email,asc")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("200")))
                .andExpect(jsonPath("$.message", is("Success")))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].email", is("admin@example.com")));

        // Verify that the specification and pageable were passed correctly
        ArgumentCaptor<Specification<User>> specCaptor = ArgumentCaptor.forClass(Specification.class);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(userFacade).getAllUsers(specCaptor.capture(), pageableCaptor.capture());

        Pageable pageable = pageableCaptor.getValue();
        assertEquals(0, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());
        assertEquals("email", pageable.getSort().getOrderFor("email").getProperty());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deactivateUser_ValidId_DeactivatesSuccessfully() throws Exception {
        // Arrange
        when(userFacade.deactivateUser(1L))
                .thenReturn(BaseResponse.build("User deactivated successfully", true));

        // Act & Assert
        mockMvc.perform(delete("/api/v1/users/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("200")))
                .andExpect(jsonPath("$.message", is("Success")))
                .andExpect(jsonPath("$.data", is("User deactivated successfully")));
    }

    @Test
    void login_InvalidCredentials_ReturnsBadRequest() throws Exception {
        // Arrange
        LoginRequest loginRequest = LoginRequest.builder()
                .email("invalid") // Invalid email format
                .password("short") // Too short password
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_InvalidUserData_ReturnsBadRequest() throws Exception {
        // Arrange
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("invalid-email")
                .password("123") // Too short
                .confirmPassword("456") // Doesn't match
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createAccount_InvalidData_ReturnsBadRequest() throws Exception {
        // Arrange
        CreateUserRequest request = CreateUserRequest.builder()
                .email("") // Empty email
                .password("short") // Too short password
                .roleId(null) // Missing role
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void updateCurrentUserProfile_InvalidData_ReturnsBadRequest() throws Exception {
        // Arrange
        UpdateUserProfileRequest request = UpdateUserProfileRequest.builder()
                .email("invalid-email-format")
                .newPassword("123") // Too short
                .confirmNewPassword("456") // Doesn't match
                .build();

        // Act & Assert
        mockMvc.perform(put("/api/v1/users/current")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCurrentUserProfile_NotLoggedIn_ReturnsUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/users/current")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateCurrentUserProfile_NotLoggedIn_ReturnsUnauthorized() throws Exception {
        // Arrange
        UpdateUserProfileRequest request = UpdateUserProfileRequest.builder()
                .email("test@example.com")
                .newPassword("Password123!")
                .confirmNewPassword("Password123!")
                .build();

        // Act & Assert
        mockMvc.perform(put("/api/v1/users/current")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAllUsers_NotLoggedIn_ReturnsUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/users")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deactivateUser_NotLoggedIn_ReturnsUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/users/1")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void createAccount_CustomerRole_ReturnsForbidden() throws Exception {
        // Arrange
        CreateUserRequest request = CreateUserRequest.builder()
                .email("test@example.com")
                .password("Password123!")
                .roleId(1L)
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void getAllUsers_CustomerRole_ReturnsForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/users")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void deactivateUser_CustomerRole_ReturnsForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/users/1")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void logout_WithoutCustomerRole_ReturnsForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/users/logout")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
}