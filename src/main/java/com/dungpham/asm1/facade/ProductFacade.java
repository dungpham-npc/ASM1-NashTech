package com.dungpham.asm1.facade;

import com.dungpham.asm1.entity.Product;
import com.dungpham.asm1.request.ProductRequest;
import com.dungpham.asm1.response.BaseResponse;
import com.dungpham.asm1.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductFacade {
    BaseResponse<List<ProductResponse>> getFeaturedProducts();

    BaseResponse<Page<ProductResponse>> getAllProducts(Specification<Product> spec, Pageable pageable);

    BaseResponse<ProductResponse> getProductDetails(Long id);

    BaseResponse<ProductResponse> createProduct(ProductRequest request, List<MultipartFile> productImages);

    BaseResponse<ProductResponse> updateProduct(ProductRequest request, Long id);

    BaseResponse<String> removeProduct(Long id);

    BaseResponse<String> rateProduct(Long productId, Integer rating);

    BaseResponse<String> uploadProductImages(Long productId, List<MultipartFile> files);

    BaseResponse<String> deleteProductImage(String imageUrl);
}
