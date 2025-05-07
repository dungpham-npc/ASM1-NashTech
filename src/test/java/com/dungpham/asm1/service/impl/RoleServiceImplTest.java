package com.dungpham.asm1.service.impl;

import com.dungpham.asm1.common.exception.NotFoundException;
import com.dungpham.asm1.entity.Role;
import com.dungpham.asm1.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    private Role validRole;

    @BeforeEach
    void setUp() {
        // Create a valid role
        validRole = Role.builder()
                .name("CUSTOMER")
                .build();

        // Set ID using reflection
        try {
            Field idField = validRole.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(validRole, 1L);
        } catch (Exception e) {
            fail("Test setup failed: " + e.getMessage());
        }
    }

    @Test
    void getRoleById_Successfully() {
        // Arrange
        Long roleId = 1L;
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(validRole));

        // Act
        Role result = roleService.getRoleById(roleId);

        // Assert
        assertNotNull(result);
        assertEquals(roleId, result.getId());
        assertEquals("CUSTOMER", result.getName());
        verify(roleRepository, times(1)).findById(roleId);
    }

    @Test
    void getRoleById_NonExistentRole_ThrowsNotFoundException() {
        // Arrange
        Long nonExistentId = 999L;
        when(roleRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            roleService.getRoleById(nonExistentId);
        });

        assertEquals("404", exception.getErrorCodeString());
        verify(roleRepository, times(1)).findById(nonExistentId);
    }

    @Test
    void getRoleByName_Successfully() {
        // Arrange
        String roleName = "CUSTOMER";
        when(roleRepository.findByName(roleName)).thenReturn(Optional.of(validRole));

        // Act
        Role result = roleService.getRoleByName(roleName);

        // Assert
        assertNotNull(result);
        assertEquals(roleName, result.getName());
        assertEquals(1L, result.getId());
        verify(roleRepository, times(1)).findByName(roleName);
    }

    @Test
    void getRoleByName_NonExistentName_ThrowsNotFoundException() {
        // Arrange
        String nonExistentName = "NON_EXISTENT_ROLE";
        when(roleRepository.findByName(nonExistentName)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            roleService.getRoleByName(nonExistentName);
        });

        assertEquals("404", exception.getErrorCodeString());
        verify(roleRepository, times(1)).findByName(nonExistentName);
    }

    @Test
    void getRoleByName_WithNullName_ThrowsNotFoundException() {
        // Arrange
        String nullName = null;
        when(roleRepository.findByName(nullName)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            roleService.getRoleByName(nullName);
        });

        assertEquals("404", exception.getErrorCodeString());
        verify(roleRepository, times(1)).findByName(nullName);
    }

    @Test
    void getRoleByName_WithEmptyName_ThrowsNotFoundException() {
        // Arrange
        String emptyName = "";
        when(roleRepository.findByName(emptyName)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            roleService.getRoleByName(emptyName);
        });

        assertEquals("404", exception.getErrorCodeString());
        verify(roleRepository, times(1)).findByName(emptyName);
    }

    @Test
    void getRoleById_WithNullId_ThrowsNotFoundException() {
        // Arrange
        Long nullId = null;
        when(roleRepository.findById(nullId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            roleService.getRoleById(nullId);
        });

        assertEquals("404", exception.getErrorCodeString());
        verify(roleRepository, times(1)).findById(nullId);
    }
}