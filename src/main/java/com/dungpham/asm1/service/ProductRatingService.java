package com.dungpham.asm1.service;

import com.dungpham.asm1.entity.Product;
import com.dungpham.asm1.entity.User;

public interface ProductRatingService {
    void rateProduct(Product product, User user, int rating);
    double getAverageRatingOfProduct(Product product);
}
