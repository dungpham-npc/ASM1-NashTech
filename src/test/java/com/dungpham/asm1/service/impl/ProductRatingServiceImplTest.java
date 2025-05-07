package com.dungpham.asm1.service.impl;

import com.dungpham.asm1.common.exception.ConflictException;
import com.dungpham.asm1.common.exception.InvalidArgumentException;
import com.dungpham.asm1.common.exception.NotFoundException;
import com.dungpham.asm1.entity.Product;
import com.dungpham.asm1.entity.ProductRating;
import com.dungpham.asm1.entity.User;
import com.dungpham.asm1.repository.ProductRatingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductRatingServiceImplTest {

    @Mock
    private ProductRatingRepository productRatingRepository;

    @InjectMocks
    private ProductRatingServiceImpl productRatingService;

    private Product product;
    private User user;

    @BeforeEach
    void setUp() {
        // Create a valid product
        product = Product.builder()
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("100.00"))
                .build();

        // Create a valid user
        user = User.builder()
                .email("test@example.com")
                .password("password")
                .build();

        // Set IDs using reflection
        try {
            Field productIdField = product.getClass().getSuperclass().getDeclaredField("id");
            productIdField.setAccessible(true);
            productIdField.set(product, 1L);

            Field userIdField = user.getClass().getSuperclass().getDeclaredField("id");
            userIdField.setAccessible(true);
            userIdField.set(user, 1L);
        } catch (Exception e) {
            fail("Test setup failed: " + e.getMessage());
        }
    }

    @Test
    void rateProduct_Successfully() {
        // Arrange
        int rating = 5;
        when(productRatingRepository.existsByUserIdAndProductId(user.getId(), product.getId())).thenReturn(false);
        when(productRatingRepository.save(any(ProductRating.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        productRatingService.rateProduct(product, user, rating);

        // Assert
        ArgumentCaptor<ProductRating> ratingCaptor = ArgumentCaptor.forClass(ProductRating.class);
        verify(productRatingRepository).save(ratingCaptor.capture());
        
        ProductRating savedRating = ratingCaptor.getValue();
        assertEquals(product, savedRating.getProduct());
        assertEquals(user, savedRating.getUser());
        assertEquals(rating, savedRating.getRating());
        verify(productRatingRepository).existsByUserIdAndProductId(user.getId(), product.getId());
    }

    @Test
    void getAverageRatingOfProduct_Successfully() {
        // Arrange
        BigDecimal expectedRating = new BigDecimal("4.50");
        when(productRatingRepository.findAverageRatingByProductId(product.getId())).thenReturn(expectedRating);

        // Act
        BigDecimal result = productRatingService.getAverageRatingOfProduct(product);

        // Assert
        assertEquals(expectedRating.setScale(2, RoundingMode.HALF_UP), result);
        verify(productRatingRepository).findAverageRatingByProductId(product.getId());
    }

    @Test
    void rateProduct_WithInvalidRatingValue_ThrowsInvalidArgumentException() {
        // Arrange - Test with rating below minimum
        int invalidLowRating = 0;

        // Act & Assert
        InvalidArgumentException lowException = assertThrows(InvalidArgumentException.class, () -> {
            productRatingService.rateProduct(product, user, invalidLowRating);
        });

        assertEquals("400", lowException.getErrorCodeString());
        verify(productRatingRepository, never()).save(any(ProductRating.class));

        // Arrange - Test with rating above maximum
        int invalidHighRating = 6;

        // Act & Assert
        InvalidArgumentException highException = assertThrows(InvalidArgumentException.class, () -> {
            productRatingService.rateProduct(product, user, invalidHighRating);
        });

        assertEquals("400", highException.getErrorCodeString());
        verify(productRatingRepository, never()).save(any(ProductRating.class));
    }

    @Test
    void rateProduct_WithNullProduct_ThrowsNotFoundException() {
        // Arrange
        int rating = 5;

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            productRatingService.rateProduct(null, user, rating);
        });

        assertEquals("404", exception.getErrorCodeString());
        verify(productRatingRepository, never()).save(any(ProductRating.class));
    }

    @Test
    void rateProduct_WithNullUser_ThrowsNotFoundException() {
        // Arrange
        int rating = 5;

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            productRatingService.rateProduct(product, null, rating);
        });

        assertEquals("404", exception.getErrorCodeString());
        verify(productRatingRepository, never()).save(any(ProductRating.class));
    }

    @Test
    void rateProduct_UserAlreadyRated_ThrowsConflictException() {
        // Arrange
        int rating = 5;
        when(productRatingRepository.existsByUserIdAndProductId(user.getId(), product.getId())).thenReturn(true);

        // Act & Assert
        ConflictException exception = assertThrows(ConflictException.class, () -> {
            productRatingService.rateProduct(product, user, rating);
        });

        assertEquals("409", exception.getErrorCodeString());
        verify(productRatingRepository).existsByUserIdAndProductId(user.getId(), product.getId());
        verify(productRatingRepository, never()).save(any(ProductRating.class));
    }

    @Test
    void getAverageRatingOfProduct_WithNullProduct_ThrowsNotFoundException() {
        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            productRatingService.getAverageRatingOfProduct(null);
        });

        assertEquals("404", exception.getErrorCodeString());
        verify(productRatingRepository, never()).findAverageRatingByProductId(any());
    }

    @Test
    void getAverageRatingOfProduct_NoRatings_ReturnsZero() {
        // Arrange
        when(productRatingRepository.findAverageRatingByProductId(product.getId())).thenReturn(null);

        // Act
        BigDecimal result = productRatingService.getAverageRatingOfProduct(product);

        // Assert
        assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), result);
        verify(productRatingRepository).findAverageRatingByProductId(product.getId());
    }
}