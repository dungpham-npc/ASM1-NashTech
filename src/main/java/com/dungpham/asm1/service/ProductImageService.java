package com.dungpham.asm1.service;

import com.dungpham.asm1.entity.Product;
import com.dungpham.asm1.entity.ProductImage;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductImageService {
    ProductImage saveImage(MultipartFile file, Product product, boolean isThumbnail);

    List<ProductImage> saveImages(List<MultipartFile> files, Product product);

    List<ProductImage> getImagesOfProduct(Product product);

    ProductImage getProductThumbnail(Product product);

    void deleteImage(Long imageId);

    void deleteAllImagesForProduct(Long productId);

    ProductImage setAsThumbnail(Long imageId);
}