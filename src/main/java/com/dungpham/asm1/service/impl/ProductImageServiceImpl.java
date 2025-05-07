package com.dungpham.asm1.service.impl;

import com.dungpham.asm1.common.exception.NotFoundException;
import com.dungpham.asm1.entity.Product;
import com.dungpham.asm1.entity.ProductImage;
import com.dungpham.asm1.infrastructure.aspect.Logged;
import com.dungpham.asm1.repository.ProductImageRepository;
import com.dungpham.asm1.service.CloudinaryService;
import com.dungpham.asm1.service.ProductImageService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class ProductImageServiceImpl implements ProductImageService {
    private final ProductImageRepository productImageRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional
    @Logged
    public ProductImage saveImage(MultipartFile file, Product product, boolean isThumbnail) {
        try {
            String imageKey = cloudinaryService.uploadImage(file.getBytes());
            return ProductImage.builder()
                    .imageKey(imageKey)
                    .isThumbnail(isThumbnail)
                    .product(product)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to save image: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    @Logged
    public List<ProductImage> saveImages(List<MultipartFile> files, Product product) {
        if (files == null || files.isEmpty()) {
            return List.of();
        }
        List<ProductImage> result = new ArrayList<>();
        if (product.getId() == null) {
            result.add(saveImage(files.getFirst(), product, true));
            if (files.size() > 1) {
                result.addAll(files.subList(1, files.size()).stream()
                        .map(file -> saveImage(file, product, false))
                        .toList());
            }
        } else {
            boolean hasThumbnail = productImageRepository.findByProductIdAndIsThumbnail(product.getId(), true)
                    .isPresent();
            if (!hasThumbnail && !files.isEmpty()) {
                result.add(saveImage(files.getFirst(), product, true));
                if (files.size() > 1) {
                    result.addAll(files.subList(1, files.size()).stream()
                            .map(file -> saveImage(file, product, false))
                            .toList());
                }
            } else {
                result.addAll(files.stream()
                        .map(file -> saveImage(file, product, false))
                        .toList());
            }
        }
        return result;
    }

    @Override
    @Logged
    public List<ProductImage> getImagesOfProduct(Product product) {
        return productImageRepository.findByProductId(product.getId());
    }

    @Override
    @Logged
    public ProductImage getProductThumbnail(Product product) {
        return productImageRepository.findByProductIdAndIsThumbnail(product.getId(), true)
                .orElse(productImageRepository.findFirstByProductId(product.getId())
                        .orElse(null));
    }

    @Override
    @Transactional
    @Logged
    public void deleteImage(Long imageId) {
        productImageRepository.findById(imageId).ifPresent(image -> {
            try {
                String url = cloudinaryService.getImageUrl(image.getImageKey());
                String publicId = extractPublicIdFromUrl(url);
                cloudinaryService.deleteImage(publicId);
            } catch (Exception e) {
                log.error("Failed to delete image from Cloudinary: {}", e.getMessage());
            }
            productImageRepository.delete(image);
        });
    }

    @Override
    @Transactional
    @Logged
    public void deleteAllImagesForProduct(Long productId) {
        List<ProductImage> images = productImageRepository.findByProductId(productId);
        for (ProductImage image : images) {
            try {
                String url = cloudinaryService.getImageUrl(image.getImageKey());
                String publicId = extractPublicIdFromUrl(url);
                cloudinaryService.deleteImage(publicId);
            } catch (Exception e) {
                log.error("Failed to delete images from Cloudinary: {}", e.getMessage());
            }
        }
        productImageRepository.deleteByProductId(productId);
    }

    @Override
    @Transactional
    @Logged
    public ProductImage setAsThumbnail(Long imageId) {
        return productImageRepository.findById(imageId).map(image -> {
            productImageRepository.unsetAllProductThumbnails(image.getProduct().getId());
            image.setThumbnail(true);
            return productImageRepository.save(image);
        }).orElseThrow(() -> new NotFoundException("Product image not found"));
    }

    private String extractPublicIdFromUrl(String imageUrl) {
        try {
            if (imageUrl == null || imageUrl.isEmpty()) {
                return null;
            }
            String[] parts = imageUrl.split("/upload/");
            if (parts.length < 2) {
                return null;
            }
            String path = parts[1].replaceAll("^v\\d+/", "");
            return path.replaceAll("\\.[^\\.]+$", "");
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract public ID from URL: " + e.getMessage(), e);
        }
    }
}