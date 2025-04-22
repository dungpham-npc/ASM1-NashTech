package com.dungpham.asm1.service.impl;

import com.dungpham.asm1.common.exception.InvalidArgumentException;
import com.dungpham.asm1.common.exception.NotFoundException;
import com.dungpham.asm1.entity.Product;
import com.dungpham.asm1.infrastructure.aspect.Logged;
import com.dungpham.asm1.repository.ProductRepository;
import com.dungpham.asm1.service.ProductService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@AllArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;

    @Override
    @Transactional
    @Logged
    public Product createProduct(Product product) {
        validateProduct(product, false);
        return productRepository.save(product);
    }

    @Override
    @Transactional
    @Logged
    public Product updateProduct(Product product) {
        validateProduct(product, true);
        return productRepository.save(product);
    }

    private void validateProduct(Product product, boolean isUpdate) {
        if (isUpdate && product.getId() == null) {
            throw new NotFoundException("Product");
        }
        if (!isUpdate && product.getId() != null) {
            throw new InvalidArgumentException("id", "Product ID must be null for creation");
        }
        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidArgumentException("price", "Price must be greater than 0");
        }
        if (product.getName() == null || product.getName().isEmpty()) {
            throw new InvalidArgumentException("name", "Product name cannot be empty");
        }
        if (product.getDescription() == null || product.getDescription().isEmpty()) {
            throw new InvalidArgumentException("description", "Product description cannot be empty");
        }
        if (product.getCategory() == null || product.getCategory().getId() == null) {
            throw new NotFoundException("Category");
        }
    }

    @Override
    @Transactional
    @Logged
    public void removeProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product"));
        product.setActive(false);
        productRepository.save(product);
    }

    @Override
    @Logged
    public Product getProductById(Long id) {
        return productRepository
                .findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new NotFoundException("Product"));
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