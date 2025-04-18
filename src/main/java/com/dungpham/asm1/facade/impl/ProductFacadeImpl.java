package com.dungpham.asm1.facade.impl;

import com.dungpham.asm1.common.exception.NotFoundException;
import com.dungpham.asm1.entity.Category;
import com.dungpham.asm1.entity.Product;
import com.dungpham.asm1.entity.ProductImage;
import com.dungpham.asm1.entity.User;
import com.dungpham.asm1.facade.ProductFacade;
import com.dungpham.asm1.request.ProductRequest;
import com.dungpham.asm1.response.BaseResponse;
import com.dungpham.asm1.response.CategoryResponse;
import com.dungpham.asm1.response.ProductDetailsResponse;
import com.dungpham.asm1.response.ProductResponse;
import com.dungpham.asm1.service.CategoryService;
import com.dungpham.asm1.service.ProductRatingService;
import com.dungpham.asm1.service.ProductService;
import com.dungpham.asm1.service.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductFacadeImpl implements ProductFacade {
    private final ProductService productService;
    private final ModelMapper modelMapper;
    private final CategoryService categoryService;
    private final ProductRatingService productRatingService;
    private final UserService userService;


    @Override
    public BaseResponse<List<ProductResponse>> getFeaturedProducts() {
        return BaseResponse.build(productService.getFeaturedProducts()
                .stream()
                .map(this::toProductResponse)
                .toList(), true);
    }

    @Override
    public BaseResponse<Page<ProductResponse>> getAllProducts(Specification<Product> spec, Pageable pageable) {
        Page<Product> productPage = productService.getAllProducts(spec, pageable);

        return BaseResponse.build(productPage.map(product -> {
            ProductResponse response = modelMapper.map(product, ProductResponse.class);
            response.setThumbnailUrl(
                    product.getImages().stream()
                            .filter(ProductImage::isThumbnail)
                            .findFirst()
                            .map(ProductImage::getImageKey)
                            .orElse(null)
            );
            return response;
        }), true);
    }

    @Override
    public BaseResponse<ProductDetailsResponse> getProductDetails(Long id) {
        Product product = productService.getProductById(id);

        return BaseResponse.build(toProductDetailsResponse(product), true);
    }

    @Override
    public BaseResponse<ProductDetailsResponse> createProduct(ProductRequest request, List<MultipartFile> productImages) {
        Product product = toProductEntity(request);

        // Add placeholder image(s) for testing
        //TODO: Validate single thumbnail image constraint here
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

        return BaseResponse.build(toProductDetailsResponse(productService.createProduct(product)), true);
    }

    @Override
    public BaseResponse<ProductDetailsResponse> updateProduct(ProductRequest request, Long id) {
        if (categoryService.getCategory(request.getCategoryId()) == null) {
            throw new NotFoundException("Category");
        }

        Product product = productService.getProductById(id);
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setFeatured(request.isFeatured());
        product.setCategory(categoryService.getCategory(request.getCategoryId()));

        Product updated = productService.updateProduct(product);
        ProductDetailsResponse response = toProductDetailsResponse(updated);
        return BaseResponse.build(response, true);

    }

    @Override
    public BaseResponse<Void> removeProduct(Long id) {
        productService.removeProduct(id);
        return BaseResponse.build(null, true);
    }

    @Override
    public BaseResponse<String> rateProduct(Long productId, Integer rating) {
        Product product = productService.getProductById(productId);
//        User user = userService.getCurrentUser()
//                .orElseThrow(() -> new UserException(ErrorCode.SECURITY_ERROR));
        User user = userService.getUserByEmail("test@example.com"); //TODO: Replace with actual user retrieval logic


        productRatingService.rateProduct(product, user, rating);
        return BaseResponse.build("Rating successful", true);
    }

    private Product toProductEntity(ProductRequest request) {
        Product product = modelMapper.map(request, Product.class);

        Category category = categoryService.getCategory(request.getCategoryId());
        product.setCategory(category);

        return product;
    }

    private ProductResponse toProductResponse(Product product) {
        ProductResponse response = modelMapper.map(product, ProductResponse.class);

        response.setThumbnailUrl(
                product.getImages().stream()
                        .filter(ProductImage::isThumbnail)
                        .findFirst()
                        .map(ProductImage::getImageKey)
                        .orElse(null)
        );

        return response;
    }

    private ProductDetailsResponse toProductDetailsResponse(Product product) {
        Category category = product.getCategory();
        CategoryResponse categoryResponse = modelMapper.map(category, CategoryResponse.class);

        ProductDetailsResponse response = modelMapper.map(product, ProductDetailsResponse.class);
        response.setImageUrls(product.getImages().stream()
                .map(ProductImage::getImageKey)
                .toList());
        response.setCategory(categoryResponse);
        response.setAverageRating(productRatingService.getAverageRatingOfProduct(product));

        return response;
    }
}
