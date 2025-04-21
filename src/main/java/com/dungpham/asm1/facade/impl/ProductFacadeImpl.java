package com.dungpham.asm1.facade.impl;

import com.dungpham.asm1.common.exception.NotFoundException;
import com.dungpham.asm1.common.exception.UnauthorizedException;
import com.dungpham.asm1.common.mapper.ProductMapper;
import com.dungpham.asm1.entity.Category;
import com.dungpham.asm1.entity.Product;
import com.dungpham.asm1.entity.ProductImage;
import com.dungpham.asm1.entity.User;
import com.dungpham.asm1.facade.ProductFacade;
import com.dungpham.asm1.request.ProductRequest;
import com.dungpham.asm1.response.BaseResponse;
import com.dungpham.asm1.response.ProductResponse;
import com.dungpham.asm1.service.CategoryService;
import com.dungpham.asm1.service.ProductRatingService;
import com.dungpham.asm1.service.ProductService;
import com.dungpham.asm1.service.impl.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductFacadeImpl implements ProductFacade {
    private final ProductService productService;
    private final ProductMapper productMapper;
    private final CategoryService categoryService;
    private final ProductRatingService productRatingService;
    private final UserDetailsServiceImpl userService;


    @Override
    public BaseResponse<List<ProductResponse>> getFeaturedProducts() {
        return BaseResponse.build(productService.getFeaturedProducts()
                .stream()
                .map(productMapper::toProductResponse)
                .toList(), true);
    }

    @Override
    public BaseResponse<Page<ProductResponse>> getAllProducts(Specification<Product> spec, Pageable pageable) {
        Page<Product> productPage = productService.getAllProducts(spec, pageable);

        return BaseResponse.build(productPage.map(productMapper::toProductResponse), true);
    }

    @Override
    public BaseResponse<ProductResponse> getProductDetails(Long id) {
        Product product = productService.getProductById(id);
        ProductResponse response = productMapper.toProductDetailsResponse(product);
        BigDecimal averageRating = productRatingService.getAverageRatingOfProduct(product);

        response.setAverageRating(averageRating.setScale(2, RoundingMode.HALF_UP));

        return BaseResponse.build(response, true);
    }

    @Override
    public BaseResponse<ProductResponse> createProduct(ProductRequest request, List<MultipartFile> productImages) {
        Product product = productMapper.toEntity(request);
        Category category = categoryService.getCategory(request.getCategoryId());

        if (category == null) {
            throw new NotFoundException("Category");
        }
        product.setCategory(category);

        if (productImages != null && !productImages.isEmpty()) {
            List<ProductImage> images = new ArrayList<>();
            boolean isFirst = true;

            for (MultipartFile file : productImages) {
                String tempImageKey = "temp_" + (file.getOriginalFilename() != null ?
                        file.getOriginalFilename() :
                        "image_" + System.currentTimeMillis());

                ProductImage image = ProductImage.builder()
                        .imageKey(tempImageKey)
                        .isThumbnail(isFirst)
                        .product(product)
                        .build();

                images.add(image);
                isFirst = false;
            }

            product.setImages(images);
        } else {
            ProductImage placeholderImage = ProductImage.builder()
                    .imageKey("placeholder_image")
                    .isThumbnail(true)
                    .product(product)
                    .build();

            product.setImages(Collections.singletonList(placeholderImage));
        }

        return BaseResponse.build(productMapper.toProductDetailsResponse(productService.createProduct(product)), true);
    }

    @Override
    public BaseResponse<ProductResponse> updateProduct(ProductRequest request, Long id) {
        Category category = categoryService.getCategory(request.getCategoryId());

        if (category == null) {
            throw new NotFoundException("Category");
        }

        Product product = productService.getProductById(id);

        productMapper.updateEntity(request, product);
        product.setCategory(category);

        Product updated = productService.updateProduct(product);

        ProductResponse response = productMapper.toProductDetailsResponse(updated);
        BigDecimal averageRating = productRatingService.getAverageRatingOfProduct(updated);
        response.setAverageRating(averageRating.setScale(2, RoundingMode.HALF_UP));

        return BaseResponse.build(response, true);

    }

    @Override
    public BaseResponse<String> removeProduct(Long id) {
        productService.removeProduct(id);
        return BaseResponse.build("Product removed successfully", true);
    }

    @Override
    public BaseResponse<String> rateProduct(Long productId, Integer rating) {
        Product product = productService.getProductById(productId);
        User user = userService.getCurrentUser()
                .orElseThrow(() -> new UnauthorizedException("this feature"));


        productRatingService.rateProduct(product, user, rating);
        return BaseResponse.build("Rating successful", true);
    }
}
