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
import com.dungpham.asm1.service.*;
import com.dungpham.asm1.service.impl.UserDetailsServiceImpl;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductFacadeImpl implements ProductFacade {
    private static final String DEFAULT_THUMBNAIL = "null.jpg";
    private static final int RATING_SCALE = 1;

    private final ProductService productService;
    private final ProductMapper productMapper;
    private final CategoryService categoryService;
    private final ProductRatingService productRatingService;
    private final UserDetailsServiceImpl userService;
    private final ProductImageService productImageService;
    private final CloudinaryService cloudinaryService;

    @Override
    public BaseResponse<List<ProductResponse>> getFeaturedProducts() {
        List<ProductResponse> responses = productService.getFeaturedProducts().stream()
                .map(this::mapToProductResponseWithThumbnail)
                .toList();
        return BaseResponse.build(responses, true);
    }

    @Override
    public BaseResponse<Page<ProductResponse>> getAllProducts(Specification<Product> spec, Pageable pageable) {
        Page<ProductResponse> responsePage = productService.getAllProducts(spec, pageable)
                .map(this::mapToProductResponseWithThumbnail);
        return BaseResponse.build(responsePage, true);
    }

    @Override
    public BaseResponse<ProductResponse> getProductDetails(Long id) {
        Product product = productService.getProductById(id);
        ProductResponse response;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        boolean isAdmin = auth != null
                && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken)
                && auth.getAuthorities().stream()
                .anyMatch(grantedAuthority -> "ROLE_ADMIN".equals(grantedAuthority.getAuthority()));

        response = isAdmin
                ? productMapper.toProductManagementDetailsResponse(product)
                : productMapper.toProductDetailsResponse(product);


        response.setAverageRating(getScaledAverageRating(product));
        response.setImageKeys(product.getImages().stream()
                .map(ProductImage::getImageKey)
                .toList());
        response.setThumbnailImgKey(null);

        return BaseResponse.build(response, true);
    }

    @Override
    @Transactional
    public BaseResponse<ProductResponse> createProduct(ProductRequest request, List<MultipartFile> productImages) {
        Product product = productMapper.toEntity(request);
        product.setCategory(getCategoryOrThrow(request.getCategoryId()));
        if (productImages != null && !productImages.isEmpty()) {
            product.setImages(productImageService.saveImages(productImages, product));
        }
        product.setFeatured(request.isFeatured());
        Product savedProduct = productService.createProduct(product);
        ProductResponse response = productMapper.toProductDetailsResponse(savedProduct);

        response.setAverageRating(BigDecimal.ZERO);
        response.setImageKeys(getImageUrls(savedProduct.getImages()));
        return BaseResponse.build(response, true);
    }

    @Override
    public BaseResponse<ProductResponse> updateProduct(ProductRequest request, Long id) {
        Product product = productService.getProductById(id);
        productMapper.updateEntity(request, product);
        product.setCategory(getCategoryOrThrow(request.getCategoryId()));
        product.setFeatured(request.isFeatured());
        Product updatedProduct = productService.updateProduct(product);

        ProductResponse response = productMapper.toProductDetailsResponse(updatedProduct);
        response.setAverageRating(getScaledAverageRating(updatedProduct));
        response.setImageKeys(getImageUrls(updatedProduct.getImages()));
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

    @Override
    public BaseResponse<String> uploadProductImages(Long productId, List<MultipartFile> files) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public BaseResponse<String> deleteProductImage(String imageUrl) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private ProductResponse mapToProductResponseWithThumbnail(Product product) {
        ProductResponse response = productMapper.toProductResponse(product);
        ProductImage thumbnail = product.getImages().stream()
                .filter(ProductImage::isThumbnail)
                .findFirst()
                .orElse(null);
        response.setThumbnailImgKey(thumbnail != null
                ? productImageService.getProductThumbnail(product).getImageKey()
                : DEFAULT_THUMBNAIL);
        response.setImageKeys(null);
        response.setAverageRating(getScaledAverageRating(product));
        return response;
    }

    private Category getCategoryOrThrow(Long categoryId) {
        Category category = categoryService.getCategory(categoryId);
        if (category == null) {
            throw new NotFoundException("Category");
        }
        return category;
    }

    private BigDecimal getScaledAverageRating(Product product) {
        return productRatingService.getAverageRatingOfProduct(product)
                .setScale(RATING_SCALE, RoundingMode.HALF_UP);
    }

    private List<String> getImageUrls(List<ProductImage> images) {
        return images.stream()
                .map(image -> cloudinaryService.getImageUrl(image.getImageKey()))
                .map(url -> url != null ? url : "")
                .toList();
    }
}