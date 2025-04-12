package com.dungpham.asm1.facade;

import com.dungpham.asm1.entity.Product;
import com.dungpham.asm1.response.BaseResponse;
import com.dungpham.asm1.response.ProductDetailsResponse;
import com.dungpham.asm1.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface ProductFacade {
    BaseResponse<List<ProductResponse>> getFeaturedProducts();
    BaseResponse<Page<ProductResponse>> getAllProducts(Specification<Product> spec, Pageable pageable);
}
