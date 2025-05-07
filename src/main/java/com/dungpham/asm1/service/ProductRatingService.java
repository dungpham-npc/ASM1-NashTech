package com.dungpham.asm1.service;

import com.dungpham.asm1.entity.Product;
import com.dungpham.asm1.entity.User;

import java.math.BigDecimal;

public interface ProductRatingService {
    void rateProduct(Product product, User user, int rating);
    BigDecimal getAverageRatingOfProduct(Product product);
}
