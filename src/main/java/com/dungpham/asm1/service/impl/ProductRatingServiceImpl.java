package com.dungpham.asm1.service.impl;

import com.dungpham.asm1.common.exception.ConflictException;
import com.dungpham.asm1.common.exception.InvalidArgumentException;
import com.dungpham.asm1.common.exception.NotFoundException;
import com.dungpham.asm1.entity.Product;
import com.dungpham.asm1.entity.ProductRating;
import com.dungpham.asm1.entity.User;
import com.dungpham.asm1.infrastructure.aspect.Logged;
import com.dungpham.asm1.repository.ProductRatingRepository;
import com.dungpham.asm1.service.ProductRatingService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ProductRatingServiceImpl implements ProductRatingService {
    private final ProductRatingRepository productRatingRepository;
    @Override
    @Transactional
    @Logged
    public void rateProduct(Product product, User user, int rating) {
        validateRating(rating, product, user);
        ProductRating productRating = new ProductRating();
        productRating.setProduct(product);
        productRating.setUser(user);
        productRating.setRating(rating);

        productRatingRepository.save(productRating);
    }

    @Override
    @Logged
    public BigDecimal getAverageRatingOfProduct(Product product) {
        if (product == null) {
            throw new NotFoundException("Product");
        }
        BigDecimal averageRating = productRatingRepository.findAverageRatingByProductId(product.getId());
        return averageRating != null ? averageRating.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    private void validateRating(int rating, Product product, User user) {
        if (rating < 1 || rating > 5) {
            throw new InvalidArgumentException("rating", "Rating must be between 1 and 5");
        }

        if (product == null) {
            throw new NotFoundException("Product");
        }

        if (user == null) {
            throw new NotFoundException("User");
        }

        if (productRatingRepository.existsByUserIdAndProductId(user.getId(), product.getId())) {
            throw new ConflictException("User rating");
        }

    }
}
