package com.dungpham.asm1.repository;

import com.dungpham.asm1.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByProductId(Long productId);

    Optional<ProductImage> findByProductIdAndIsThumbnail(Long productId, boolean isThumbnail);

    Optional<ProductImage> findFirstByProductId(Long productId);

    void deleteByProductId(Long productId);

    @Modifying
    @Query("UPDATE ProductImage pi SET pi.isThumbnail = false WHERE pi.product.id = :productId")
    void unsetAllProductThumbnails(@Param("productId") Long productId);
}