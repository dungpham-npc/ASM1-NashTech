//package com.dungpham.asm1.service.impl;
//
//import com.dungpham.asm1.common.exception.ConflictException;
//import com.dungpham.asm1.common.exception.InvalidArgumentException;
//import com.dungpham.asm1.common.exception.NotFoundException;
//import com.dungpham.asm1.entity.Category;
//import com.dungpham.asm1.entity.Product;
//import com.dungpham.asm1.repository.CategoryRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.lang.reflect.Field;
//import java.lang.reflect.Method;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class CategoryServiceImplTest {
//
//    @Mock
//    private CategoryRepository categoryRepository;
//
//    @InjectMocks
//    private CategoryServiceImpl categoryService;
//
//    private Category validCategory;
//    private List<CategoryListResponse> categoryListResponses;
//
//    @BeforeEach
//    void setUp() {
//        // Create a valid category
//        validCategory = Category.builder()
//                .name("Electronics")
//                .description("Electronic devices")
//                .products(new ArrayList<>())
//                .build();
//
//        // Create category list responses
//        categoryListResponses = List.of(
//                new CategoryListResponse(1L, "Electronics", 5L),
//                new CategoryListResponse(2L, "Books", 3L)
//        );
//    }
//
//    @Test
//    void getAllCategories_Successfully() {
//        // Arrange
//        when(categoryRepository.findCategoryWithProductCount()).thenReturn(categoryListResponses);
//
//        // Act
//        List<CategoryListResponse> result = categoryService.getAllCategories();
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(2, result.size());
//        assertEquals("Electronics", result.get(0).name());
//        assertEquals(5L, result.get(0).numberOfAssociatedProducts());
//        verify(categoryRepository, times(1)).findCategoryWithProductCount();
//    }
//
//    @Test
//    void getCategory_Successfully() {
//        // Arrange
//        Long categoryId = 1L;
//
//        // Set ID using reflection
//        try {
//            Field idField = validCategory.getClass().getSuperclass().getDeclaredField("id");
//            idField.setAccessible(true);
//            idField.set(validCategory, categoryId);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        when(categoryRepository.findByIdAndIsActiveTrue(categoryId)).thenReturn(Optional.of(validCategory));
//
//        // Act
//        Category result = categoryService.getCategory(categoryId);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(categoryId, result.getId());
//        assertEquals("Electronics", result.getName());
//        assertEquals("Electronic devices", result.getDescription());
//        verify(categoryRepository, times(1)).findByIdAndIsActiveTrue(categoryId);
//    }
//
//    @Test
//    void getCategory_NonExistent_ThrowsNotFoundException() {
//        // Arrange
//        Long nonExistentId = 999L;
//        when(categoryRepository.findByIdAndIsActiveTrue(nonExistentId)).thenReturn(Optional.empty());
//
//        // Act & Assert
//        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
//            categoryService.getCategory(nonExistentId);
//        });
//
//        assertEquals("404", exception.getErrorCodeString());
//        verify(categoryRepository, times(1)).findByIdAndIsActiveTrue(nonExistentId);
//    }
//
//    @Test
//    void createCategory_Successfully() {
//        // Arrange
//        when(categoryRepository.save(any(Category.class))).thenReturn(validCategory);
//
//        // Act
//        Category result = categoryService.createCategory(validCategory);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals("Electronics", result.getName());
//        assertEquals("Electronic devices", result.getDescription());
//        verify(categoryRepository, times(1)).save(validCategory);
//    }
//
//    @Test
//    void createCategory_WithEmptyName_ThrowsInvalidArgumentException() {
//        // Arrange
//        Category categoryWithEmptyName = Category.builder()
//                .name("")
//                .description("Electronic devices")
//                .build();
//
//        // Act & Assert
//        InvalidArgumentException exception = assertThrows(InvalidArgumentException.class, () -> {
//            categoryService.createCategory(categoryWithEmptyName);
//        });
//
//        assertEquals("400", exception.getErrorCodeString());
//        verify(categoryRepository, never()).save(any(Category.class));
//    }
//
//    @Test
//    void createCategory_WithDuplicateName_ThrowsConflictException() {
//        // Arrange
//        String duplicateName = "Electronics";
//        Category categoryWithDuplicateName = Category.builder()
//                .name(duplicateName)
//                .description("New description")
//                .build();
//
//        when(categoryRepository.findByNameAndIsActiveTrue(duplicateName))
//                .thenReturn(Optional.of(validCategory));
//
//        // Act & Assert
//        ConflictException exception = assertThrows(ConflictException.class, () -> {
//            categoryService.createCategory(categoryWithDuplicateName);
//        });
//
//        assertEquals("409", exception.getErrorCodeString());
//        verify(categoryRepository, times(1)).findByNameAndIsActiveTrue(duplicateName);
//        verify(categoryRepository, never()).save(any(Category.class));
//    }
//
//
//    @Test
//    void updateCategory_Successfully() {
//        // Arrange
//        Long categoryId = 1L;
//        Category existingCategory = Category.builder()
//                .name("Old Name")
//                .description("Old Description")
//                .build();
//
//        // Set ID using reflection
//        try {
//            Field idField = existingCategory.getClass().getSuperclass().getDeclaredField("id");
//            idField.setAccessible(true);
//            idField.set(existingCategory, categoryId);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
//        when(categoryRepository.save(any(Category.class))).thenReturn(existingCategory);
//
//        // Act
//        Category result = categoryService.updateCategory(validCategory, categoryId);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(categoryId, result.getId());
//        assertEquals("Electronics", result.getName());
//        assertEquals("Electronic devices", result.getDescription());
//        verify(categoryRepository, times(1)).findById(categoryId);
//        verify(categoryRepository, times(1)).save(existingCategory);
//    }
//
//    @Test
//    void removeCategory_Successfully() {
//        // Arrange
//        Long categoryId = 1L;
//
//        // Set ID using reflection
//        try {
//            Field idField = validCategory.getClass().getSuperclass().getDeclaredField("id");
//            idField.setAccessible(true);
//            idField.set(validCategory, categoryId);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(validCategory));
//        when(categoryRepository.save(any(Category.class))).thenReturn(validCategory);
//
//        // Act
//        categoryService.removeCategory(categoryId);
//
//        // Assert
//        assertFalse(validCategory.isActive());
//        verify(categoryRepository, times(1)).findById(categoryId);
//        verify(categoryRepository, times(1)).save(validCategory);
//    }
//
//    @Test
//    void removeCategory_WithProducts_ThrowsInvalidArgumentException() {
//        // Arrange
//        Long categoryId = 1L;
//        Category categoryWithProducts = Category.builder()
//                .name("Electronics")
//                .description("Electronic devices")
//                .products(List.of(new Product()))
//                .build();
//
//        // Set ID using reflection
//        try {
//            Field idField = categoryWithProducts.getClass().getSuperclass().getDeclaredField("id");
//            idField.setAccessible(true);
//            idField.set(categoryWithProducts, categoryId);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(categoryWithProducts));
//
//        // Act & Assert
//        InvalidArgumentException exception = assertThrows(InvalidArgumentException.class, () -> {
//            categoryService.removeCategory(categoryId);
//        });
//
//        assertEquals("400", exception.getErrorCodeString());
//        verify(categoryRepository, times(1)).findById(categoryId);
//        verify(categoryRepository, never()).save(any(Category.class));
//    }
//
//    @Test
//    void createCategory_WithNullName_ThrowsInvalidArgumentException() {
//        // Arrange
//        Category categoryWithNullName = Category.builder()
//                .name(null)
//                .description("Electronic devices")
//                .build();
//
//        // Act & Assert
//        InvalidArgumentException exception = assertThrows(InvalidArgumentException.class, () -> {
//            categoryService.createCategory(categoryWithNullName);
//        });
//
//        assertEquals("400", exception.getErrorCodeString());
//        verify(categoryRepository, never()).save(any(Category.class));
//    }
//
//    @Test
//    void createCategory_WithNullDescription_ThrowsInvalidArgumentException() {
//        // Arrange
//        Category categoryWithNullDescription = Category.builder()
//                .name("Electronics")
//                .description(null)
//                .build();
//
//        // Act & Assert
//        InvalidArgumentException exception = assertThrows(InvalidArgumentException.class, () -> {
//            categoryService.createCategory(categoryWithNullDescription);
//        });
//
//        assertEquals("400", exception.getErrorCodeString());
//        verify(categoryRepository, never()).save(any(Category.class));
//    }
//
//    @Test
//    void createCategory_WithEmptyDescription_ThrowsInvalidArgumentException() {
//        // Arrange
//        Category categoryWithEmptyDescription = Category.builder()
//                .name("Electronics")
//                .description("")
//                .build();
//
//        // Act & Assert
//        InvalidArgumentException exception = assertThrows(InvalidArgumentException.class, () -> {
//            categoryService.createCategory(categoryWithEmptyDescription);
//        });
//
//        assertEquals("400", exception.getErrorCodeString());
//        verify(categoryRepository, never()).save(any(Category.class));
//    }
//
//    @Test
//    void updateCategory_WithNullName_ThrowsInvalidArgumentException() {
//        // Arrange
//        Long categoryId = 1L;
//        Category existingCategory = Category.builder()
//                .name("Old Name")
//                .description("Old Description")
//                .build();
//
//        // Set ID using reflection
//        try {
//            Field idField = existingCategory.getClass().getSuperclass().getDeclaredField("id");
//            idField.setAccessible(true);
//            idField.set(existingCategory, categoryId);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        Category updateWithNullName = Category.builder()
//                .name(null)
//                .description("New Description")
//                .build();
//
//        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
//
//        // Act & Assert
//        InvalidArgumentException exception = assertThrows(InvalidArgumentException.class, () -> {
//            categoryService.updateCategory(updateWithNullName, categoryId);
//        });
//
//        assertEquals("400", exception.getErrorCodeString());
//        verify(categoryRepository, times(1)).findById(categoryId);
//        verify(categoryRepository, never()).save(any(Category.class));
//    }
//
//    @Test
//    void updateCategory_WithEmptyDescription_ThrowsInvalidArgumentException() {
//        // Arrange
//        Long categoryId = 1L;
//        Category existingCategory = Category.builder()
//                .name("Old Name")
//                .description("Old Description")
//                .build();
//
//        // Set ID using reflection
//        try {
//            Field idField = existingCategory.getClass().getSuperclass().getDeclaredField("id");
//            idField.setAccessible(true);
//            idField.set(existingCategory, categoryId);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        Category updateWithEmptyDescription = Category.builder()
//                .name("New Name")
//                .description("")
//                .build();
//
//        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
//
//        // Act & Assert
//        InvalidArgumentException exception = assertThrows(InvalidArgumentException.class, () -> {
//            categoryService.updateCategory(updateWithEmptyDescription, categoryId);
//        });
//
//        assertEquals("400", exception.getErrorCodeString());
//        verify(categoryRepository, times(1)).findById(categoryId);
//        verify(categoryRepository, never()).save(any(Category.class));
//    }
//
//    @Test
//    void validateCategory_WithUnknownOperation_ThrowsIllegalArgumentException() {
//        // This test directly tests the validateCategory method using reflection
//        try {
//            // Get the private method
//            Method validateCategoryMethod = CategoryServiceImpl.class.getDeclaredMethod(
//                    "validateCategory", Category.class, String.class);
//            validateCategoryMethod.setAccessible(true);
//
//            // Act & Assert
//            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
//                validateCategoryMethod.invoke(categoryService, validCategory, "unknown");
//            });
//
//            assertTrue(exception.getCause().getMessage().contains("Unknown operation: unknown"));
//            Throwable cause = exception.getCause();
//            assertInstanceOf(IllegalArgumentException.class, cause);
//            assertTrue(cause.getMessage().contains("Unknown operation: unknown"));
//
//        } catch (Exception e) {
//            fail("Test failed due to reflection error: " + e.getMessage());
//        }
//    }
//
//}