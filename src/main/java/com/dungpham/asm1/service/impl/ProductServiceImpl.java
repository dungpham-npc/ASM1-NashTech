package com.dungpham.asm1.service.impl;

import com.dungpham.asm1.entity.Product;
import com.dungpham.asm1.infrastructure.aspect.Logged;
import com.dungpham.asm1.repository.ProductRepository;
import com.dungpham.asm1.response.ProductResponse;
import com.dungpham.asm1.service.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    @Override
    public void addProduct(String name, double price, int quantity) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void updateProduct(int id, String name, double price, int quantity) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void deleteProduct(int id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void getProductById(int id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    @Logged
    public List<Product> getFeaturedProducts() {
        return productRepository.findTop5ByUpdatedAtNotNullAndIsFeaturedTrueAndIsActiveTrueOrderByUpdatedAtDesc();
    }

    @Override
    @Logged
    public Page<Product> getAllProducts(Specification<Product> spec, Pageable pageable) {
        return productRepository.findAll(spec, pageable);
    }
}
