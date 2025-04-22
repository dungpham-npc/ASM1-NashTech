package com.dungpham.asm1.facade.impl;

import com.dungpham.asm1.common.exception.NotFoundException;
import com.dungpham.asm1.common.exception.UnauthorizedException;
import com.dungpham.asm1.common.mapper.ProductMapper;
import com.dungpham.asm1.entity.Category;
import com.dungpham.asm1.entity.Product;
import com.dungpham.asm1.entity.ProductImage;
import com.dungpham.asm1.entity.User;
import com.dungpham.asm1.request.ProductRequest;
import com.dungpham.asm1.response.BaseResponse;
import com.dungpham.asm1.response.ProductResponse;
import com.dungpham.asm1.service.CategoryService;
import com.dungpham.asm1.service.CloudinaryService;
import com.dungpham.asm1.service.ProductImageService;
import com.dungpham.asm1.service.ProductRatingService;
import com.dungpham.asm1.service.ProductService;
import com.dungpham.asm1.service.impl.UserDetailsServiceImpl;
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
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductFacadeImplTest {

    @Mock
    private ProductService productService;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private CategoryService categoryService;

    @Mock
    private ProductRatingService productRatingService;

    @Mock
    private UserDetailsServiceImpl userService;

    @Mock
    private ProductImageService productImageService;

    @Mock
    private CloudinaryService cloudinaryService;

    @Mock
    private MultipartFile multipartFile;

    @Mock
    private Specification<Product> specification;

    @Mock
    private Pageable pageable;

    @InjectMocks
    private ProductFacadeImpl productFacade;

    private Category category;
    private Product product;
    private ProductImage thumbnailImage;
    private ProductImage regularImage;
    private User user;
    private ProductRequest productRequest;
    private ProductResponse productResponse;
    private ProductResponse productDetailsResponse;

    @BeforeEach
    void setUp() {
        // Create category
        category = Category.builder()
                .name("Electronics")
                .description("Electronic devices")
                .build();

        // Create product
        product = Product.builder()
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("100.00"))
                .category(category)
                .isFeatured(true)
                .images(new ArrayList<>())
                .build();

        // Create product images
        thumbnailImage = ProductImage.builder()
                .imageKey("thumbnail_key_123")
                .isThumbnail(true)
                .product(product)
                .build();

        regularImage = ProductImage.builder()
                .imageKey("image_key_456")
                .isThumbnail(false)
                .product(product)
                .build();

        product.getImages().add(thumbnailImage);
        product.getImages().add(regularImage);

        // Create user
        user = User.builder()
                .email("test@example.com")
                .password("password")
                .build();

        // Create product request - using Builder since there are no setters
        productRequest = ProductRequest.builder()
                .name("New Product")
                .description("New Description")
                .price(new BigDecimal("200.00"))
                .categoryId(1L)
                .isFeatured(true)
                .build();

        // Create product responses
        productResponse = new ProductResponse();
        productResponse.setId(1L);
        productResponse.setName("Test Product");
        productResponse.setPrice(new BigDecimal("100.00"));
        productResponse.setThumbnailUrl("https://cloudinary.com/thumbnail_key_123");

        productDetailsResponse = new ProductResponse();
        productDetailsResponse.setId(1L);
        productDetailsResponse.setName("Test Product");
        productDetailsResponse.setDescription("Test Description");
        productDetailsResponse.setPrice(new BigDecimal("100.00"));
        productDetailsResponse.setImageUrls(Arrays.asList("https://cloudinary.com/thumbnail_key_123", "https://cloudinary.com/image_key_456"));
        productDetailsResponse.setAverageRating(new BigDecimal("4.50"));

        // Set IDs using reflection
        try {
            Field categoryIdField = category.getClass().getSuperclass().getDeclaredField("id");
            categoryIdField.setAccessible(true);
            categoryIdField.set(category, 1L);

            Field productIdField = product.getClass().getSuperclass().getDeclaredField("id");
            productIdField.setAccessible(true);
            productIdField.set(product, 1L);

            Field thumbnailIdField = thumbnailImage.getClass().getSuperclass().getDeclaredField("id");
            thumbnailIdField.setAccessible(true);
            thumbnailIdField.set(thumbnailImage, 1L);

            Field regularImageIdField = regularImage.getClass().getSuperclass().getDeclaredField("id");
            regularImageIdField.setAccessible(true);
            regularImageIdField.set(regularImage, 2L);

            Field userIdField = user.getClass().getSuperclass().getDeclaredField("id");
            userIdField.setAccessible(true);
            userIdField.set(user, 1L);
        } catch (Exception e) {
            fail("Test setup failed: " + e.getMessage());
        }
    }

    @Test
    void getFeaturedProducts_ReturnsListOfProductResponses() {
        // Arrange
        List<Product> featuredProducts = Collections.singletonList(product);
        when(productService.getFeaturedProducts()).thenReturn(featuredProducts);
        when(productMapper.toProductResponse(product)).thenReturn(productResponse);
        when(cloudinaryService.getImageUrl(thumbnailImage.getImageKey())).thenReturn("https://cloudinary.com/thumbnail_key_123");

        // Act
        BaseResponse<List<ProductResponse>> response = productFacade.getFeaturedProducts();

        // Assert
        assertTrue(response.isStatus());
        assertEquals(1, response.getData().size());
        assertEquals(productResponse, response.getData().get(0));
        verify(productService).getFeaturedProducts();
        verify(productMapper).toProductResponse(product);
    }

    @Test
    void getAllProducts_ReturnsPageOfProductResponses() {
        // Arrange
        List<Product> products = Collections.singletonList(product);
        Page<Product> productPage = new PageImpl<>(products);
        when(productService.getAllProducts(specification, pageable)).thenReturn(productPage);
        when(productMapper.toProductResponse(product)).thenReturn(productResponse);
        when(cloudinaryService.getImageUrl(thumbnailImage.getImageKey())).thenReturn("https://cloudinary.com/thumbnail_key_123");

        // Act
        BaseResponse<Page<ProductResponse>> response = productFacade.getAllProducts(specification, pageable);

        // Assert
        assertTrue(response.isStatus());
        assertEquals(1, response.getData().getTotalElements());
        assertEquals(productResponse, response.getData().getContent().get(0));
        verify(productService).getAllProducts(specification, pageable);
        verify(productMapper).toProductResponse(product);
    }

    @Test
    void getProductDetails_ReturnsProductDetailsResponse() {
        // Arrange
        when(productService.getProductById(product.getId())).thenReturn(product);
        when(productMapper.toProductDetailsResponse(product)).thenReturn(productDetailsResponse);
        when(productRatingService.getAverageRatingOfProduct(product)).thenReturn(new BigDecimal("4.50"));
        when(cloudinaryService.getImageUrl(thumbnailImage.getImageKey())).thenReturn("https://cloudinary.com/thumbnail_key_123");
        when(cloudinaryService.getImageUrl(regularImage.getImageKey())).thenReturn("https://cloudinary.com/image_key_456");

        // Act
        BaseResponse<ProductResponse> response = productFacade.getProductDetails(product.getId());

        // Assert
        assertTrue(response.isStatus());
        assertEquals(productDetailsResponse, response.getData());
        verify(productService).getProductById(product.getId());
        verify(productMapper).toProductDetailsResponse(product);
        verify(productRatingService).getAverageRatingOfProduct(product);
    }

    @Test
    void createProduct_WithImages_CreatesProductAndSavesImages() {
        // Arrange
        List<MultipartFile> images = Collections.singletonList(multipartFile);
        List<ProductImage> savedImages = Collections.singletonList(thumbnailImage);

        when(productMapper.toEntity(productRequest)).thenReturn(product);
        when(categoryService.getCategory(productRequest.getCategoryId())).thenReturn(category);
        when(productImageService.saveImages(images, product)).thenReturn(savedImages);
        when(productService.createProduct(product)).thenReturn(product);
        when(productMapper.toProductDetailsResponse(product)).thenReturn(productDetailsResponse);
        when(cloudinaryService.getImageUrl(anyString())).thenReturn("https://cloudinary.com/thumbnail_key_123");

        // Act
        BaseResponse<ProductResponse> response = productFacade.createProduct(productRequest, images);

        // Assert
        assertTrue(response.isStatus());
        assertEquals(productDetailsResponse, response.getData());
        verify(productMapper).toEntity(productRequest);
        verify(categoryService).getCategory(productRequest.getCategoryId());
        verify(productImageService).saveImages(images, product);
        verify(productService).createProduct(product);
        verify(productMapper).toProductDetailsResponse(product);
    }

    @Test
    void createProduct_WithoutImages_OnlyCreatesProduct() {
        // Arrange
        when(productMapper.toEntity(productRequest)).thenReturn(product);
        when(categoryService.getCategory(productRequest.getCategoryId())).thenReturn(category);
        when(productService.createProduct(product)).thenReturn(product);
        when(productMapper.toProductDetailsResponse(product)).thenReturn(productDetailsResponse);

        // Act
        BaseResponse<ProductResponse> response = productFacade.createProduct(productRequest, null);

        // Assert
        assertTrue(response.isStatus());
        assertEquals(productDetailsResponse, response.getData());
        verify(productMapper).toEntity(productRequest);
        verify(categoryService).getCategory(productRequest.getCategoryId());
        verify(productService).createProduct(product);
        verify(productMapper).toProductDetailsResponse(product);
        verify(productImageService, never()).saveImages(any(), any());
    }

    @Test
    void createProduct_WithInvalidCategory_ThrowsNotFoundException() {
        // Arrange
        when(productMapper.toEntity(productRequest)).thenReturn(product);
        when(categoryService.getCategory(productRequest.getCategoryId())).thenReturn(null);

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            productFacade.createProduct(productRequest, null);
        });

        assertEquals("Category not found", exception.getMessage());
        verify(productMapper).toEntity(productRequest);
        verify(categoryService).getCategory(productRequest.getCategoryId());
        verify(productService, never()).createProduct(any());
    }

    @Test
    void updateProduct_Successfully() {
        // Arrange
        when(productService.getProductById(product.getId())).thenReturn(product);
        when(categoryService.getCategory(productRequest.getCategoryId())).thenReturn(category);
        when(productService.updateProduct(product)).thenReturn(product);
        when(productMapper.toProductDetailsResponse(product)).thenReturn(productDetailsResponse);
        when(productRatingService.getAverageRatingOfProduct(product)).thenReturn(new BigDecimal("4.50"));
        when(cloudinaryService.getImageUrl(anyString())).thenReturn("https://cloudinary.com/image_url");

        // Act
        BaseResponse<ProductResponse> response = productFacade.updateProduct(productRequest, product.getId());

        // Assert
        assertTrue(response.isStatus());
        assertEquals(productDetailsResponse, response.getData());
        verify(productService).getProductById(product.getId());
        verify(productMapper).updateEntity(productRequest, product);
        verify(categoryService).getCategory(productRequest.getCategoryId());
        verify(productService).updateProduct(product);
        verify(productMapper).toProductDetailsResponse(product);
        verify(productRatingService).getAverageRatingOfProduct(product);
    }

    @Test
    void updateProduct_WithInvalidCategory_ThrowsNotFoundException() {
        // Arrange
        when(productService.getProductById(product.getId())).thenReturn(product);
        when(categoryService.getCategory(productRequest.getCategoryId())).thenReturn(null);

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            productFacade.updateProduct(productRequest, product.getId());
        });

        assertEquals("Category not found", exception.getMessage());
        verify(productService).getProductById(product.getId());
        verify(productMapper).updateEntity(productRequest, product);
        verify(categoryService).getCategory(productRequest.getCategoryId());
        verify(productService, never()).updateProduct(any());
    }

    @Test
    void removeProduct_Successfully() {
        // Arrange
        doNothing().when(productService).removeProduct(product.getId());

        // Act
        BaseResponse<String> response = productFacade.removeProduct(product.getId());

        // Assert
        assertTrue(response.isStatus());
        assertEquals("Product removed successfully", response.getData());
        verify(productService).removeProduct(product.getId());
    }

    @Test
    void rateProduct_Successfully() {
        // Arrange
        int rating = 5;
        when(productService.getProductById(product.getId())).thenReturn(product);
        when(userService.getCurrentUser()).thenReturn(Optional.of(user));
        doNothing().when(productRatingService).rateProduct(product, user, rating);

        // Act
        BaseResponse<String> response = productFacade.rateProduct(product.getId(), rating);

        // Assert
        assertTrue(response.isStatus());
        assertEquals("Rating successful", response.getData());
        verify(productService).getProductById(product.getId());
        verify(userService).getCurrentUser();
        verify(productRatingService).rateProduct(product, user, rating);
    }

    @Test
    void rateProduct_WithoutUser_ThrowsUnauthorizedException() {
        // Arrange
        int rating = 5;
        when(productService.getProductById(product.getId())).thenReturn(product);
        when(userService.getCurrentUser()).thenReturn(Optional.empty());

        // Act & Assert
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
            productFacade.rateProduct(product.getId(), rating);
        });

        assertEquals("Unauthorized access to this feature", exception.getMessage());
        verify(productService).getProductById(product.getId());
        verify(userService).getCurrentUser();
        verify(productRatingService, never()).rateProduct(any(), any(), anyInt());
    }

    @Test
    void uploadProductImages_ThrowsUnsupportedOperationException() {
        // Act & Assert
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class, () -> {
            productFacade.uploadProductImages(product.getId(), Collections.singletonList(multipartFile));
        });

        assertEquals("Not implemented yet", exception.getMessage());
    }

    @Test
    void deleteProductImage_ThrowsUnsupportedOperationException() {
        // Act & Assert
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class, () -> {
            productFacade.deleteProductImage("image_url");
        });

        assertEquals("Not implemented yet", exception.getMessage());
    }

    @Test
    void getProductDetails_WithNonExistingProduct_ThrowsException() {
        // Arrange
        Long nonExistingId = 999L;
        when(productService.getProductById(nonExistingId)).thenThrow(new NotFoundException("Product"));

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            productFacade.getProductDetails(nonExistingId);
        });

        assertEquals("Product not found", exception.getMessage());
        verify(productService).getProductById(nonExistingId);
    }

    @Test
    void getFeaturedProducts_WithEmptyList_ReturnsEmptyResponse() {
        // Arrange
        when(productService.getFeaturedProducts()).thenReturn(Collections.emptyList());

        // Act
        BaseResponse<List<ProductResponse>> response = productFacade.getFeaturedProducts();

        // Assert
        assertTrue(response.isStatus());
        assertTrue(response.getData().isEmpty());
        verify(productService).getFeaturedProducts();
    }
}