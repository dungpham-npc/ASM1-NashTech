package com.dungpham.asm1.service.impl;

import com.dungpham.asm1.common.exception.ConflictException;
import com.dungpham.asm1.common.exception.ForbiddenException;
import com.dungpham.asm1.common.exception.InvalidArgumentException;
import com.dungpham.asm1.common.exception.NotFoundException;
import com.dungpham.asm1.entity.RecipientInformation;
import com.dungpham.asm1.entity.Role;
import com.dungpham.asm1.entity.User;
import com.dungpham.asm1.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User activeUser;
    private User inactiveUser;
    private Role userRole;

    @BeforeEach
    void setUp() {
        // Create a role
        userRole = Role.builder()
                .name("CUSTOMER")
                .build();

        // Set ID using reflection
        try {
            Field roleIdField = userRole.getClass().getSuperclass().getDeclaredField("id");
            roleIdField.setAccessible(true);
            roleIdField.set(userRole, 1L);
        } catch (Exception e) {
            fail("Test setup failed: " + e.getMessage());
        }

        // Create recipient information
        RecipientInformation recipientInfo = RecipientInformation.builder()
                .firstName("John")
                .lastName("Doe")
                .phone("1234567890")
                .address("123 Main St")
                .isDefault(true)
                .build();

        // Create an active user
        activeUser = User.builder()
                .email("active@example.com")
                .password("password")
                .role(userRole)
                .recipientInformation(new ArrayList<>())
                .build();
        activeUser.getRecipientInformation().add(recipientInfo);
        recipientInfo.setUser(activeUser);

        // Create an inactive user
        inactiveUser = User.builder()
                .email("inactive@example.com")
                .password("password")
                .role(userRole)
                .recipientInformation(new ArrayList<>())
                .build();

        // Set IDs and active status using reflection
        try {
            Field userIdField = activeUser.getClass().getSuperclass().getDeclaredField("id");
            userIdField.setAccessible(true);
            userIdField.set(activeUser, 1L);

            Field inactiveUserIdField = inactiveUser.getClass().getSuperclass().getDeclaredField("id");
            inactiveUserIdField.setAccessible(true);
            inactiveUserIdField.set(inactiveUser, 2L);

            Field isActiveField = inactiveUser.getClass().getSuperclass().getDeclaredField("isActive");
            isActiveField.setAccessible(true);
            isActiveField.set(inactiveUser, false);

            Field recipientIdField = recipientInfo.getClass().getSuperclass().getDeclaredField("id");
            recipientIdField.setAccessible(true);
            recipientIdField.set(recipientInfo, 1L);
        } catch (Exception e) {
            fail("Test setup failed: " + e.getMessage());
        }
    }

    @Test
    void getUserByEmail_WithValidEmail_ReturnsUser() {
        // Arrange
        String email = "active@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(activeUser));

        // Act
        User result = userService.getUserByEmail(email);

        // Assert
        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertEquals(1L, result.getId());
        verify(userRepository).findByEmail(email);
    }

    @Test
    void getUserByEmail_WithNonExistentEmail_ThrowsNotFoundException() {
        // Arrange
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            userService.getUserByEmail(email);
        });

        assertEquals("404", exception.getErrorCodeString());
        verify(userRepository).findByEmail(email);
    }

    @Test
    void getUserByEmail_WithInactiveUser_ThrowsForbiddenException() {
        // Arrange
        String email = "inactive@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(inactiveUser));

        // Act & Assert
        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> {
            userService.getUserByEmail(email);
        });

        assertEquals("403", exception.getErrorCodeString());
        verify(userRepository).findByEmail(email);
    }

    @Test
    void getAllUsers_Successfully() {
        // Arrange
        List<User> userList = Arrays.asList(activeUser, inactiveUser);
        Page<User> userPage = new PageImpl<>(userList);
        
        Specification<User> spec = mock(Specification.class);
        Pageable pageable = mock(Pageable.class);
        
        when(userRepository.findAll(spec, pageable)).thenReturn(userPage);

        // Act
        Page<User> result = userService.getAllUsers(spec, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(userList, result.getContent());
        verify(userRepository).findAll(spec, pageable);
    }

    @Test
    void updateUserProfile_Successfully() {
        // Arrange
        User userToUpdate = User.builder()
                .email("update@example.com")
                .password("newPassword")
                .role(userRole)
                .recipientInformation(new ArrayList<>())
                .build();
        
        // Set ID using reflection
        try {
            Field userIdField = userToUpdate.getClass().getSuperclass().getDeclaredField("id");
            userIdField.setAccessible(true);
            userIdField.set(userToUpdate, 3L);
        } catch (Exception e) {
            fail("Test setup failed: " + e.getMessage());
        }

        RecipientInformation newRecipient = RecipientInformation.builder()
                .firstName("Jane")
                .lastName("Smith")
                .phone("9876543210")
                .address("456 Oak St")
                .isDefault(true)
                .user(userToUpdate)
                .build();
        userToUpdate.getRecipientInformation().add(newRecipient);

        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(userToUpdate);

        // Act
        User result = userService.updateUserProfile(userToUpdate);

        // Assert
        assertNotNull(result);
        assertEquals("update@example.com", result.getEmail());
        assertEquals("encodedNewPassword", result.getPassword());
        assertEquals(1, result.getRecipientInformation().size());
        assertEquals("Jane", result.getRecipientInformation().get(0).getFirstName());
        
        verify(passwordEncoder).encode("newPassword");
        verify(userRepository).save(userToUpdate);
    }

    @Test
    void updateUserProfile_WithEmptyEmail_ThrowsInvalidArgumentException() {
        // Arrange
        User userWithEmptyEmail = User.builder()
                .email("")
                .password("password")
                .role(userRole)
                .build();

        // Act & Assert
        InvalidArgumentException exception = assertThrows(InvalidArgumentException.class, () -> {
            userService.updateUserProfile(userWithEmptyEmail);
        });

        assertEquals("400", exception.getErrorCodeString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUserProfile_WithEmptyPassword_ThrowsInvalidArgumentException() {
        // Arrange
        User userWithEmptyPassword = User.builder()
                .email("test@example.com")
                .password("")
                .role(userRole)
                .build();

        // Act & Assert
        InvalidArgumentException exception = assertThrows(InvalidArgumentException.class, () -> {
            userService.updateUserProfile(userWithEmptyPassword);
        });

        assertEquals("400", exception.getErrorCodeString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUserProfile_WithMultipleDefaultRecipients_ThrowsInvalidArgumentException() {
        // Arrange
        User userWithMultipleDefaults = User.builder()
                .email("test@example.com")
                .password("password")
                .role(userRole)
                .recipientInformation(new ArrayList<>())
                .build();

        RecipientInformation recipient1 = RecipientInformation.builder()
                .firstName("John")
                .lastName("Doe")
                .phone("1234567890")
                .address("123 Main St")
                .isDefault(true)
                .user(userWithMultipleDefaults)
                .build();

        RecipientInformation recipient2 = RecipientInformation.builder()
                .firstName("Jane")
                .lastName("Smith")
                .phone("9876543210")
                .address("456 Oak St")
                .isDefault(true)
                .user(userWithMultipleDefaults)
                .build();

        userWithMultipleDefaults.getRecipientInformation().add(recipient1);
        userWithMultipleDefaults.getRecipientInformation().add(recipient2);

        // Act & Assert
        InvalidArgumentException exception = assertThrows(InvalidArgumentException.class, () -> {
            userService.updateUserProfile(userWithMultipleDefaults);
        });

        assertEquals("400", exception.getErrorCodeString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_Successfully() {
        // Arrange
        User newUser = User.builder()
                .email("new@example.com")
                .password("password")
                .role(userRole)
                .build();

        when(userRepository.existsByEmail(newUser.getEmail())).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        // Act
        User result = userService.createUser(newUser);

        // Assert
        assertNotNull(result);
        assertEquals("new@example.com", result.getEmail());
        assertEquals("encodedPassword", result.getPassword());
        
        verify(userRepository).existsByEmail(newUser.getEmail());
        verify(passwordEncoder).encode("password");
        verify(userRepository).save(newUser);
    }

    @Test
    void createUser_WithExistingEmail_ThrowsConflictException() {
        // Arrange
        User userWithExistingEmail = User.builder()
                .email("active@example.com")
                .password("password")
                .role(userRole)
                .build();

        when(userRepository.existsByEmail(userWithExistingEmail.getEmail())).thenReturn(true);

        // Act & Assert
        ConflictException exception = assertThrows(ConflictException.class, () -> {
            userService.createUser(userWithExistingEmail);
        });

        assertEquals("409", exception.getErrorCodeString());
        verify(userRepository).existsByEmail(userWithExistingEmail.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_WithEmptyPassword_ThrowsNotFoundException() {
        // Arrange
        User userWithEmptyPassword = User.builder()
                .email("test@example.com")
                .password("")
                .role(userRole)
                .build();

        when(userRepository.existsByEmail(userWithEmptyPassword.getEmail())).thenReturn(false);

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            userService.createUser(userWithEmptyPassword);
        });

        assertEquals("404", exception.getErrorCodeString());
        verify(userRepository).existsByEmail(userWithEmptyPassword.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_WithEmptyEmail_ThrowsNotFoundException() {
        // Arrange
        User userWithEmptyEmail = User.builder()
                .email("")
                .password("password")
                .role(userRole)
                .build();

        when(userRepository.existsByEmail(userWithEmptyEmail.getEmail())).thenReturn(false);

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            userService.createUser(userWithEmptyEmail);
        });

        assertEquals("404", exception.getErrorCodeString());
        verify(userRepository).existsByEmail(userWithEmptyEmail.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_WithNullRole_ThrowsNotFoundException() {
        // Arrange
        User userWithNullRole = User.builder()
                .email("test@example.com")
                .password("password")
                .role(null)
                .build();

        when(userRepository.existsByEmail(userWithNullRole.getEmail())).thenReturn(false);

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            userService.createUser(userWithNullRole);
        });

        assertEquals("404", exception.getErrorCodeString());
        verify(userRepository).existsByEmail(userWithNullRole.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserById_Successfully() {
        // Arrange
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser));

        // Act
        User result = userService.getUserById(userId);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("active@example.com", result.getEmail());
        verify(userRepository).findById(userId);
    }

    @Test
    void getUserById_WithNonExistentId_ThrowsNotFoundException() {
        // Arrange
        Long nonExistentId = 999L;
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            userService.getUserById(nonExistentId);
        });

        assertEquals("404", exception.getErrorCodeString());
        verify(userRepository).findById(nonExistentId);
    }

    @Test
    void deactivateUser_Successfully() {
        // Arrange
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(activeUser));
        when(userRepository.save(any(User.class))).thenReturn(activeUser);

        // Act
        userService.deactivateUser(userId);

        // Assert
        assertFalse(activeUser.isActive());
        verify(userRepository).findById(userId);
        verify(userRepository).save(activeUser);
    }

    @Test
    void getUserByEmail_WithNullEmail_ThrowsNotFoundException() {
        // Arrange
        String email = null;
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            userService.getUserByEmail(email);
        });

        assertEquals("404", exception.getErrorCodeString());
        verify(userRepository).findByEmail(email);
    }

    @Test
    void updateUserProfile_WithNullPassword_ThrowsInvalidArgumentException() {
        // Arrange
        User userWithNullPassword = User.builder()
                .email("test@example.com")
                .password(null)
                .role(userRole)
                .build();

        // Act & Assert
        InvalidArgumentException exception = assertThrows(InvalidArgumentException.class, () -> {
            userService.updateUserProfile(userWithNullPassword);
        });

        assertEquals("400", exception.getErrorCodeString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUserProfile_WithNullRecipientInformation_DoesNotThrowException() {
        // Arrange
        User userWithNullRecipients = User.builder()
                .email("test@example.com")
                .password("password")
                .role(userRole)
                .recipientInformation(null)
                .build();

        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(userWithNullRecipients);

        // Act
        User result = userService.updateUserProfile(userWithNullRecipients);

        // Assert
        assertNotNull(result);
        assertEquals("encodedPassword", result.getPassword());
        verify(passwordEncoder).encode("password");
        verify(userRepository).save(userWithNullRecipients);
    }

    @Test
    void createUser_WithNullPassword_ThrowsNotFoundException() {
        // Arrange
        User userWithNullPassword = User.builder()
                .email("test@example.com")
                .password(null)
                .role(userRole)
                .build();

        when(userRepository.existsByEmail(userWithNullPassword.getEmail())).thenReturn(false);

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            userService.createUser(userWithNullPassword);
        });

        assertEquals("404", exception.getErrorCodeString());
        verify(userRepository).existsByEmail(userWithNullPassword.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_WithNullEmail_ThrowsNotFoundException() {
        // Arrange
        User userWithNullEmail = User.builder()
                .email(null)
                .password("password")
                .role(userRole)
                .build();

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            userService.createUser(userWithNullEmail);
        });

        assertEquals("404", exception.getErrorCodeString());
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deactivateUser_WithNonExistentUser_ThrowsNotFoundException() {
        // Arrange
        Long nonExistentId = 999L;
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            userService.deactivateUser(nonExistentId);
        });

        assertEquals("404", exception.getErrorCodeString());
        verify(userRepository).findById(nonExistentId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUserProfile_WithEmptyRecipients_CompletesSuccessfully() {
        // Arrange
        User userWithEmptyRecipients = User.builder()
                .email("test@example.com")
                .password("password")
                .role(userRole)
                .recipientInformation(new ArrayList<>())
                .build();

        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(userWithEmptyRecipients);

        // Act
        User result = userService.updateUserProfile(userWithEmptyRecipients);

        // Assert
        assertNotNull(result);
        assertEquals("encodedPassword", result.getPassword());
        assertTrue(result.getRecipientInformation().isEmpty());
        verify(passwordEncoder).encode("password");
        verify(userRepository).save(userWithEmptyRecipients);
    }
}