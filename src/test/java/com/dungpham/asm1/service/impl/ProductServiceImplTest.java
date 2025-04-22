package com.dungpham.asm1.service.impl;

import com.dungpham.asm1.common.exception.ConflictException;
import com.dungpham.asm1.common.exception.InvalidArgumentException;
import com.dungpham.asm1.common.exception.NotFoundException;
import com.dungpham.asm1.entity.Category;
import com.dungpham.asm1.entity.Product;
import com.dungpham.asm1.repository.ProductRepository;
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

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product validProduct;
    private Category validCategory;

    @BeforeEach
    void setUp() {
        // Create a category with ID using reflection since we can't directly set ID
        validCategory = Category.builder()
                .name("Electronics")
                .description("Electronic devices")
                .build();

        // Use reflection to set the ID
        try {
            Field idField = validCategory.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(validCategory, 1L);
        } catch (Exception e) {
            e.printStackTrace();
        }

        validProduct = Product.builder()
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("100.00"))
                .category(validCategory)
                .isFeatured(true)
                .build();
    }

    @Test
    void createProduct_Successfully() {
        // Arrange
        when(productRepository.save(any(Product.class))).thenReturn(validProduct);

        // Act
        Product result = productService.createProduct(validProduct);

        // Assert
        assertNotNull(result);
        assertEquals("Test Product", result.getName());
        assertEquals("Test Description", result.getDescription());
        assertEquals(new BigDecimal("100.00"), result.getPrice());
        assertEquals(validCategory, result.getCategory());
        verify(productRepository, times(1)).save(validProduct);
    }

    @Test
    void updateProduct_Successfully() {
        // Arrange
        Long productId = 1L;

        // Set ID using reflection
        try {
            Field idField = validProduct.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(validProduct, productId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        when(productRepository.save(any(Product.class))).thenReturn(validProduct);

        // Act
        Product result = productService.updateProduct(validProduct);

        // Assert
        assertNotNull(result);
        assertEquals(productId, result.getId());
        assertEquals("Test Product", result.getName());
        verify(productRepository, times(1)).save(validProduct);
        verify(productRepository, never()).findById(any());
    }

    @Test
    void updateProduct_NonExistentProduct_ThrowsException() {
        // Arrange - Create a product with null ID
        Product productWithNullId = Product.builder()
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("100.00"))
                .category(validCategory)
                .build();

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            productService.updateProduct(productWithNullId);
        });

        assertEquals("404", exception.getErrorCodeString());
        verify(productRepository, never()).findById(any());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void validateProduct_WithInvalidData_ThrowsException() {
        // Test case 1: Null price
        Product productWithNullPrice = Product.builder()
                .name("Test Product")
                .description("Test Description")
                .category(validCategory)
                .build();

        InvalidArgumentException priceException = assertThrows(InvalidArgumentException.class, () -> {
            productService.createProduct(productWithNullPrice);
        });
        assertEquals("400", priceException.getErrorCodeString());

        // Test case 2: Zero price
        Product productWithZeroPrice = Product.builder()
                .name("Test Product")
                .description("Test Description")
                .price(BigDecimal.ZERO)
                .category(validCategory)
                .build();

        InvalidArgumentException zeroPriceException = assertThrows(InvalidArgumentException.class, () -> {
            productService.createProduct(productWithZeroPrice);
        });
        assertEquals("400", zeroPriceException.getErrorCodeString());

        // Test case 3: Empty name
        Product productWithEmptyName = Product.builder()
                .name("")
                .description("Test Description")
                .price(new BigDecimal("100.00"))
                .category(validCategory)
                .build();

        InvalidArgumentException nameException = assertThrows(InvalidArgumentException.class, () -> {
            productService.createProduct(productWithEmptyName);
        });
        assertEquals("400", nameException.getErrorCodeString());

        // Test case 4: Empty description
        Product productWithEmptyDescription = Product.builder()
                .name("Test Product")
                .description("")
                .price(new BigDecimal("100.00"))
                .category(validCategory)
                .build();

        InvalidArgumentException descriptionException = assertThrows(InvalidArgumentException.class, () -> {
            productService.createProduct(productWithEmptyDescription);
        });
        assertEquals("400", descriptionException.getErrorCodeString());

        // Test case 5: Null category
        Product productWithNullCategory = Product.builder()
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("100.00"))
                .build();

        NotFoundException categoryException = assertThrows(NotFoundException.class, () -> {
            productService.createProduct(productWithNullCategory);
        });
        assertEquals("404", categoryException.getErrorCodeString());
    }

    @Test
    void getProductById_Successfully() {
        // Arrange
        Long productId = 1L;

        // Set ID using reflection
        try {
            Field idField = validProduct.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(validProduct, productId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        when(productRepository.findByIdAndIsActiveTrue(productId)).thenReturn(Optional.of(validProduct));

        // Act
        Product result = productService.getProductById(productId);

        // Assert
        assertNotNull(result);
        assertEquals(productId, result.getId());
        assertEquals("Test Product", result.getName());
        verify(productRepository, times(1)).findByIdAndIsActiveTrue(productId);
    }

    @Test
    void getProductById_NonExistentProduct_ThrowsException() {
        // Arrange
        Long nonExistentId = 999L;

        when(productRepository.findByIdAndIsActiveTrue(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            productService.getProductById(nonExistentId);
        });

        assertEquals("404", exception.getErrorCodeString());
        verify(productRepository, times(1)).findByIdAndIsActiveTrue(nonExistentId);
    }

    @Test
    void getFeaturedProducts_Successfully() {
        // Arrange
        List<Product> featuredProducts = new ArrayList<>();
        featuredProducts.add(validProduct);

        when(productRepository.findTop5ByUpdatedAtNotNullAndIsFeaturedTrueAndIsActiveTrueOrderByUpdatedAtDesc())
                .thenReturn(featuredProducts);

        // Act
        List<Product> result = productService.getFeaturedProducts();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Product", result.get(0).getName());
        verify(productRepository, times(1))
                .findTop5ByUpdatedAtNotNullAndIsFeaturedTrueAndIsActiveTrueOrderByUpdatedAtDesc();
    }

    @Test
    void removeProduct_Successfully() {
        // Arrange
        Long productId = 1L;

        // Set ID using reflection
        try {
            Field idField = validProduct.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(validProduct, productId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        when(productRepository.findById(productId)).thenReturn(Optional.of(validProduct));
        when(productRepository.save(any(Product.class))).thenReturn(validProduct);

        // Act
        productService.removeProduct(productId);

        // Assert
        assertFalse(validProduct.isActive());
        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, times(1)).save(validProduct);
    }

    @Test
    void getAllProducts_Successfully() {
        // Arrange
        List<Product> productList = new ArrayList<>();
        productList.add(validProduct);
        Page<Product> productPage = new PageImpl<>(productList);

        Specification<Product> spec = mock(Specification.class);
        Pageable pageable = mock(Pageable.class);

        when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(productPage);

        // Act
        Page<Product> result = productService.getAllProducts(spec, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Product", result.getContent().get(0).getName());
        verify(productRepository, times(1)).findAll(spec, pageable);
    }
}