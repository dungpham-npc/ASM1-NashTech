package com.dungpham.asm1.repository;

import com.dungpham.asm1.entity.ProductRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface ProductRatingRepository extends JpaRepository<ProductRating, Long> {
    boolean existsByUserIdAndProductId(Long userId, Long productId);

    @Query("SELECT AVG(r.rating) FROM ProductRating r WHERE r.product.id = :productId")
    BigDecimal findAverageRatingByProductId(@Param("productId") Long productId);

}
