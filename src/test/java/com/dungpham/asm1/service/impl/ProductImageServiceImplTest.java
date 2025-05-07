package com.dungpham.asm1.service.impl;

import com.dungpham.asm1.common.exception.NotFoundException;
import com.dungpham.asm1.entity.Product;
import com.dungpham.asm1.entity.ProductImage;
import com.dungpham.asm1.repository.ProductImageRepository;
import com.dungpham.asm1.service.CloudinaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductImageServiceImplTest {

    @Mock
    private ProductImageRepository productImageRepository;

    @Mock
    private CloudinaryService cloudinaryService;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private ProductImageServiceImpl productImageService;

    private Product validProduct;
    private ProductImage validImage;

    @BeforeEach
    void setUp() {
        // Create a valid product
        validProduct = Product.builder()
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("100.00"))
                .build();

        // Create a valid productImage
        validImage = ProductImage.builder()
                .imageKey("image_key_123")
                .isThumbnail(true)
                .product(validProduct)
                .build();

        // Set IDs using reflection
        try {
            Field productIdField = validProduct.getClass().getSuperclass().getDeclaredField("id");
            productIdField.setAccessible(true);
            productIdField.set(validProduct, 1L);

            Field imageIdField = validImage.getClass().getSuperclass().getDeclaredField("id");
            imageIdField.setAccessible(true);
            imageIdField.set(validImage, 1L);
        } catch (Exception e) {
            fail("Test setup failed: " + e.getMessage());
        }
    }

    @Test
    void saveImage_Successfully() throws Exception {
        // Arrange
        byte[] imageBytes = "test image data".getBytes();
        when(multipartFile.getBytes()).thenReturn(imageBytes);
        when(cloudinaryService.uploadImage(imageBytes)).thenReturn("uploaded_image_key");

        // Act
        ProductImage result = productImageService.saveImage(multipartFile, validProduct, true);

        // Assert
        assertNotNull(result);
        assertEquals("uploaded_image_key", result.getImageKey());
        assertTrue(result.isThumbnail());
        assertEquals(validProduct, result.getProduct());
        verify(cloudinaryService).uploadImage(imageBytes);
    }

    @Test
    void getImagesOfProduct_Successfully() {
        // Arrange
        List<ProductImage> expectedImages = Arrays.asList(validImage);
        when(productImageRepository.findByProductId(validProduct.getId())).thenReturn(expectedImages);

        // Act
        List<ProductImage> result = productImageService.getImagesOfProduct(validProduct);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(validImage, result.get(0));
        verify(productImageRepository).findByProductId(validProduct.getId());
    }

    @Test
    void getProductThumbnail_NoThumbnail_ReturnsFirstImage() {
        // Arrange
        when(productImageRepository.findByProductIdAndIsThumbnail(validProduct.getId(), true))
                .thenReturn(Optional.empty());
        when(productImageRepository.findFirstByProductId(validProduct.getId()))
                .thenReturn(Optional.of(validImage));

        // Act
        ProductImage result = productImageService.getProductThumbnail(validProduct);

        // Assert
        assertNotNull(result);
        assertEquals(validImage, result);
        verify(productImageRepository).findByProductIdAndIsThumbnail(validProduct.getId(), true);
        verify(productImageRepository).findFirstByProductId(validProduct.getId());
    }

    @Test
    void getProductThumbnail_NoImagesAtAll_ReturnsNull() {
        // Arrange
        when(productImageRepository.findByProductIdAndIsThumbnail(validProduct.getId(), true))
                .thenReturn(Optional.empty());
        when(productImageRepository.findFirstByProductId(validProduct.getId()))
                .thenReturn(Optional.empty());

        // Act
        ProductImage result = productImageService.getProductThumbnail(validProduct);

        // Assert
        assertNull(result);
        verify(productImageRepository).findByProductIdAndIsThumbnail(validProduct.getId(), true);
        verify(productImageRepository).findFirstByProductId(validProduct.getId());
    }

    @Test
    void deleteImage_ImageExists_DeletesSuccessfully() {
        // Arrange
        Long imageId = 1L;
        String imageUrl = "https://res.cloudinary.com/demo/image/upload/v1234/folder/image_key_123.jpg";

        when(productImageRepository.findById(imageId)).thenReturn(Optional.of(validImage));
        when(cloudinaryService.getImageUrl(validImage.getImageKey())).thenReturn(imageUrl);

        // Act
        productImageService.deleteImage(imageId);

        // Assert
        verify(productImageRepository).findById(imageId);
        verify(cloudinaryService).getImageUrl(validImage.getImageKey());
        verify(cloudinaryService).deleteImage("folder/image_key_123");
        verify(productImageRepository).delete(validImage);
    }

    @Test
    void deleteImage_ImageDoesNotExist_DoesNothing() {
        // Arrange
        Long imageId = 999L;
        when(productImageRepository.findById(imageId)).thenReturn(Optional.empty());

        // Act
        productImageService.deleteImage(imageId);

        // Assert
        verify(productImageRepository).findById(imageId);
        verify(cloudinaryService, never()).getImageUrl(anyString());
        verify(cloudinaryService, never()).deleteImage(anyString());
        verify(productImageRepository, never()).delete(any(ProductImage.class));
    }

    @Test
    void deleteAllImagesForProduct_WithImages_DeletesAll() {
        // Arrange
        Long productId = 1L;
        List<ProductImage> images = Arrays.asList(validImage);
        String imageUrl = "https://res.cloudinary.com/demo/image/upload/v1234/folder/image_key_123.jpg";

        when(productImageRepository.findByProductId(productId)).thenReturn(images);
        when(cloudinaryService.getImageUrl(validImage.getImageKey())).thenReturn(imageUrl);

        // Act
        productImageService.deleteAllImagesForProduct(productId);

        // Assert
        verify(productImageRepository).findByProductId(productId);
        verify(cloudinaryService).getImageUrl(validImage.getImageKey());
        verify(cloudinaryService).deleteImage("folder/image_key_123");
        verify(productImageRepository).deleteByProductId(productId);
    }

    @Test
    void deleteAllImagesForProduct_NoImagesFound_DoesNothing() {
        // Arrange
        Long productId = 1L;
        when(productImageRepository.findByProductId(productId)).thenReturn(new ArrayList<>());

        // Act
        productImageService.deleteAllImagesForProduct(productId);

        // Assert
        verify(productImageRepository).findByProductId(productId);
        verify(cloudinaryService, never()).deleteImage(anyString());
        verify(productImageRepository).deleteByProductId(productId);
    }

    @Test
    void setAsThumbnail_ValidImage_SetsAsThumbnail() {
        // Arrange
        Long imageId = 1L;

        when(productImageRepository.findById(imageId)).thenReturn(Optional.of(validImage));
        when(productImageRepository.save(any(ProductImage.class))).thenReturn(validImage);

        // Act
        ProductImage result = productImageService.setAsThumbnail(imageId);

        // Assert
        assertNotNull(result);
        assertEquals(validImage, result);
        verify(productImageRepository).findById(imageId);
        verify(productImageRepository).unsetAllProductThumbnails(validProduct.getId());
        verify(productImageRepository).save(validImage);
    }

    @Test
    void setAsThumbnail_InvalidImage_ThrowsNotFoundException() {
        // Arrange
        Long nonExistentId = 999L;
        when(productImageRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            productImageService.setAsThumbnail(nonExistentId);
        });

        assertEquals("404", exception.getErrorCodeString());
        verify(productImageRepository).findById(nonExistentId);
        verify(productImageRepository, never()).unsetAllProductThumbnails(anyLong());
        verify(productImageRepository, never()).save(any(ProductImage.class));
    }

    @Test
    void saveImages_EmptyList_ReturnsEmptyList() {
        // Arrange
        List<MultipartFile> emptyList = new ArrayList<>();

        // Act
        List<ProductImage> result = productImageService.saveImages(emptyList, validProduct);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void saveImages_NewProduct_FirstImageAsThumbnail() throws Exception {
        // Arrange
        // Create product with null ID (new product)
        Product newProduct = Product.builder()
                .name("New Product")
                .description("New Description")
                .price(new BigDecimal("200.00"))
                .build();

        MultipartFile file1 = mock(MultipartFile.class);
        MultipartFile file2 = mock(MultipartFile.class);
        List<MultipartFile> files = Arrays.asList(file1, file2);

        byte[] image1Bytes = "image1 data".getBytes();
        byte[] image2Bytes = "image2 data".getBytes();

        when(file1.getBytes()).thenReturn(image1Bytes);
        when(file2.getBytes()).thenReturn(image2Bytes);
        when(cloudinaryService.uploadImage(image1Bytes)).thenReturn("key1");
        when(cloudinaryService.uploadImage(image2Bytes)).thenReturn("key2");

        // Act
        List<ProductImage> result = productImageService.saveImages(files, newProduct);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.get(0).isThumbnail());
        assertFalse(result.get(1).isThumbnail());
        verify(file1).getBytes();
        verify(file2).getBytes();
        verify(cloudinaryService).uploadImage(image1Bytes);
        verify(cloudinaryService).uploadImage(image2Bytes);
    }

    @Test
    void getProductThumbnail_WithThumbnail_ReturnsThumbnail() {
        // Arrange
        when(productImageRepository.findByProductIdAndIsThumbnail(validProduct.getId(), true))
                .thenReturn(Optional.of(validImage));
        // We need to mock this too because the implementation calls it anyway
        when(productImageRepository.findFirstByProductId(validProduct.getId()))
                .thenReturn(Optional.empty());

        // Act
        ProductImage result = productImageService.getProductThumbnail(validProduct);

        // Assert
        assertNotNull(result);
        assertEquals(validImage, result);
        verify(productImageRepository).findByProductIdAndIsThumbnail(validProduct.getId(), true);
        verify(productImageRepository).findFirstByProductId(validProduct.getId());
    }

    @Test
    void saveImage_ThrowsException_WhenFileReadFails() throws Exception {
        // Arrange
        when(multipartFile.getBytes()).thenThrow(new java.io.IOException("Read error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productImageService.saveImage(multipartFile, validProduct, true);
        });

        assertTrue(exception.getMessage().contains("Failed to save image"));
        verify(cloudinaryService, never()).uploadImage(any(byte[].class));
    }

    @Test
    void saveImages_WithExistingProductHavingThumbnail() throws Exception {
        // Arrange
        MultipartFile file1 = mock(MultipartFile.class);
        MultipartFile file2 = mock(MultipartFile.class);
        List<MultipartFile> files = Arrays.asList(file1, file2);

        byte[] image1Bytes = "image1 data".getBytes();
        byte[] image2Bytes = "image2 data".getBytes();

        when(file1.getBytes()).thenReturn(image1Bytes);
        when(file2.getBytes()).thenReturn(image2Bytes);
        when(cloudinaryService.uploadImage(any(byte[].class))).thenReturn("uploaded_key");
        when(productImageRepository.findByProductIdAndIsThumbnail(validProduct.getId(), true))
                .thenReturn(Optional.of(validImage));

        // Act
        List<ProductImage> result = productImageService.saveImages(files, validProduct);

        // Assert
        assertEquals(2, result.size());
        assertFalse(result.get(0).isThumbnail());
        assertFalse(result.get(1).isThumbnail());
        verify(file1).getBytes();
        verify(file2).getBytes();
        verify(cloudinaryService, times(2)).uploadImage(any(byte[].class));
    }

    @Test
    void saveImages_WithExistingProductWithoutThumbnail() throws Exception {
        // Arrange
        MultipartFile file1 = mock(MultipartFile.class);
        MultipartFile file2 = mock(MultipartFile.class);
        List<MultipartFile> files = Arrays.asList(file1, file2);

        byte[] image1Bytes = "image1 data".getBytes();
        byte[] image2Bytes = "image2 data".getBytes();

        when(file1.getBytes()).thenReturn(image1Bytes);
        when(file2.getBytes()).thenReturn(image2Bytes);
        when(cloudinaryService.uploadImage(any(byte[].class))).thenReturn("uploaded_key");
        when(productImageRepository.findByProductIdAndIsThumbnail(validProduct.getId(), true))
                .thenReturn(Optional.empty());

        // Act
        List<ProductImage> result = productImageService.saveImages(files, validProduct);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.get(0).isThumbnail());
        assertFalse(result.get(1).isThumbnail());
        verify(file1).getBytes();
        verify(file2).getBytes();
        verify(cloudinaryService, times(2)).uploadImage(any(byte[].class));
    }

    @Test
    void saveImages_WithSingleFile_ExistingProductWithoutThumbnail() throws Exception {
        // Arrange
        MultipartFile file1 = mock(MultipartFile.class);
        List<MultipartFile> files = List.of(file1);

        byte[] image1Bytes = "image1 data".getBytes();

        when(file1.getBytes()).thenReturn(image1Bytes);
        when(cloudinaryService.uploadImage(any(byte[].class))).thenReturn("uploaded_key");
        when(productImageRepository.findByProductIdAndIsThumbnail(validProduct.getId(), true))
                .thenReturn(Optional.empty());

        // Act
        List<ProductImage> result = productImageService.saveImages(files, validProduct);

        // Assert
        assertEquals(1, result.size());
        assertTrue(result.get(0).isThumbnail());
        verify(file1).getBytes();
        verify(cloudinaryService).uploadImage(any(byte[].class));
    }

    @Test
    void saveImages_WithSingleFile_NewProduct() throws Exception {
        // Arrange
        // Create product with null ID (new product)
        Product newProduct = Product.builder()
                .name("New Product")
                .description("New Description")
                .price(new BigDecimal("200.00"))
                .build();

        MultipartFile file1 = mock(MultipartFile.class);
        List<MultipartFile> files = List.of(file1);

        byte[] image1Bytes = "image1 data".getBytes();

        when(file1.getBytes()).thenReturn(image1Bytes);
        when(cloudinaryService.uploadImage(image1Bytes)).thenReturn("key1");

        // Act
        List<ProductImage> result = productImageService.saveImages(files, newProduct);

        // Assert
        assertEquals(1, result.size());
        assertTrue(result.get(0).isThumbnail());
        verify(file1).getBytes();
        verify(cloudinaryService).uploadImage(image1Bytes);
    }

    @Test
    void deleteImage_CloudinaryThrowsException_StillDeletesFromDatabase() {
        // Arrange
        Long imageId = 1L;
        String imageUrl = "https://res.cloudinary.com/demo/image/upload/v1234/folder/image_key_123.jpg";

        when(productImageRepository.findById(imageId)).thenReturn(Optional.of(validImage));
        when(cloudinaryService.getImageUrl(anyString())).thenReturn(imageUrl);
        // Use doThrow().when() syntax and match with null argument
        doThrow(new RuntimeException("Cloudinary error")).when(cloudinaryService).deleteImage(any());

        // Act
        productImageService.deleteImage(imageId);

        // Assert
        verify(productImageRepository).findById(imageId);
        verify(cloudinaryService).getImageUrl(anyString());
        // Change to verify with any() to match null
        verify(cloudinaryService).deleteImage(any());
        verify(productImageRepository).delete(validImage);
    }

    @Test
    void deleteAllImagesForProduct_CloudinaryThrowsException_StillDeletesFromDatabase() {
        // Arrange
        Long productId = 1L;
        List<ProductImage> images = List.of(validImage);
        String imageUrl = "https://res.cloudinary.com/demo/image/upload/v1234/folder/image_key_123.jpg";

        when(productImageRepository.findByProductId(productId)).thenReturn(images);
        when(cloudinaryService.getImageUrl(anyString())).thenReturn(imageUrl);
        // Use doThrow().when() syntax and match with null argument
        doThrow(new RuntimeException("Cloudinary error")).when(cloudinaryService).deleteImage(any());

        // Act
        productImageService.deleteAllImagesForProduct(productId);

        // Assert
        verify(productImageRepository).findByProductId(productId);
        verify(cloudinaryService).getImageUrl(anyString());
        // Change to verify with any() to match null
        verify(cloudinaryService).deleteImage(any());
        verify(productImageRepository).deleteByProductId(productId);
    }

    @Test
    void extractPublicIdFromUrl_ValidUrl_ExtractsCorrectly() throws Exception {
        // We need to test the private method using reflection
        String imageUrl = "https://res.cloudinary.com/demo/image/upload/v1234/folder/image_key_123.jpg";
        String expectedPublicId = "folder/image_key_123";

        // Use reflection to access the private method
        Method extractPublicIdMethod = ProductImageServiceImpl.class.getDeclaredMethod("extractPublicIdFromUrl", String.class);
        extractPublicIdMethod.setAccessible(true);

        // Act
        String result = (String) extractPublicIdMethod.invoke(productImageService, imageUrl);

        // Assert
        assertEquals(expectedPublicId, result);
    }

    @Test
    void extractPublicIdFromUrl_NullOrEmptyUrl_ReturnsNull() throws Exception {
        // Use reflection to access the private method
        Method extractPublicIdMethod = ProductImageServiceImpl.class.getDeclaredMethod("extractPublicIdFromUrl", String.class);
        extractPublicIdMethod.setAccessible(true);

        // Act & Assert
        assertNull(extractPublicIdMethod.invoke(productImageService, (String)null));
        assertNull(extractPublicIdMethod.invoke(productImageService, ""));
    }

    @Test
    void extractPublicIdFromUrl_InvalidUrl_ReturnsNull() throws Exception {
        // Use reflection to access the private method
        Method extractPublicIdMethod = ProductImageServiceImpl.class.getDeclaredMethod("extractPublicIdFromUrl", String.class);
        extractPublicIdMethod.setAccessible(true);

        // Act & Assert
        assertNull(extractPublicIdMethod.invoke(productImageService, "invalid-url-without-upload"));
    }
}