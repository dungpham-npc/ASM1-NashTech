package com.dungpham.asm1.service.impl;

import com.dungpham.asm1.common.enums.ErrorCode;
import com.dungpham.asm1.common.exception.ProductRatingException;
import com.dungpham.asm1.entity.Product;
import com.dungpham.asm1.entity.ProductRating;
import com.dungpham.asm1.entity.User;
import com.dungpham.asm1.repository.ProductRatingRepository;
import com.dungpham.asm1.service.ProductRatingService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class ProductRatingServiceImpl implements ProductRatingService {
    private final ProductRatingRepository productRatingRepository;
    @Override
    public void rateProduct(Product product, User user, int rating) {
        validateRating(rating, product, user);
        ProductRating productRating = new ProductRating();
        productRating.setProduct(product);
        productRating.setUser(user);
        productRating.setRating(rating);

        productRatingRepository.save(productRating);
    }

    @Override
    public double getAverageRatingOfProduct(Product product) {
        if (product == null) {
            throw new ProductRatingException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        Double averageRating = productRatingRepository.findAverageRatingByProductId(product.getId());
        return Optional.ofNullable(averageRating).orElse(0.0);
    }

    private void validateRating(int rating, Product product, User user) {
        if (rating < 1 || rating > 5) {
            throw new ProductRatingException(ErrorCode.PRODUCT_RATING_OUT_OF_BOUNDS);
        }

        if (product == null) {
            throw new ProductRatingException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        if (user == null) {
            throw new ProductRatingException(ErrorCode.USER_NOT_FOUND);
        }

        if (productRatingRepository.existsByUserId(user.getId())) {
            throw new ProductRatingException(ErrorCode.PRODUCT_ALREADY_RATED);
        }

    }
}
