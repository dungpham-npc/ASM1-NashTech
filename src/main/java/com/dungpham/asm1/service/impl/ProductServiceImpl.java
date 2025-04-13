package com.dungpham.asm1.service.impl;

import com.dungpham.asm1.common.enums.ErrorCode;
import com.dungpham.asm1.common.exception.ProductException;
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
        if (product.getId() != null) {
            throw new ProductException(ErrorCode.PRODUCT_ID_IS_NOT_NULL);
        }
        validateProduct(product);

        return productRepository.save(product);
    }

    @Override
    @Transactional
    @Logged
    public Product updateProduct(Product product) {
        if (productRepository.findById(product.getId()).isEmpty()) {
            throw new ProductException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        validateProduct(product);

        return productRepository.save(product);
    }

    private void validateProduct(Product product) {
        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ProductException(ErrorCode.PRICE_SMALLER_THAN_ZERO);
        }

        if (product.getName() == null || product.getName().isEmpty()) {
            throw new ProductException(ErrorCode.PRODUCT_NAME_EMPTY);
        }

        if (product.getDescription() == null || product.getDescription().isEmpty()) {
            throw new ProductException(ErrorCode.PRODUCT_DESCRIPTION_EMPTY);
        }

        if (product.getCategory() == null || product.getCategory().getId() == null) {
            throw new ProductException(ErrorCode.CATEGORY_NOT_FOUND);
        }
    }

    @Override
    @Transactional
    @Logged
    public void removeProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductException(ErrorCode.PRODUCT_NOT_FOUND));
        product.setActive(false);

        productRepository.save(product);
    }

    @Override
    @Logged
    public Product getProductById(Long id) {
        return productRepository
                .findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ProductException(ErrorCode.PRODUCT_NOT_FOUND));
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
