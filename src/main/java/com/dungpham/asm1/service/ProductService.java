package com.dungpham.asm1.service;

import com.dungpham.asm1.entity.Product;
import com.dungpham.asm1.response.ProductResponse;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface ProductService {
    void addProduct(String name, double price, int quantity);
    void updateProduct(int id, String name, double price, int quantity);
    void deleteProduct(int id);
    void getProductById(int id);
    List<Product> getFeaturedProducts();
    Page<Product> getAllProducts(Specification<Product> spec, Pageable pageable);
}
