package com.dungpham.asm1.facade.impl;

import com.dungpham.asm1.entity.Product;
import com.dungpham.asm1.entity.ProductImage;
import com.dungpham.asm1.facade.ProductFacade;
import com.dungpham.asm1.response.BaseResponse;
import com.dungpham.asm1.response.ProductResponse;
import com.dungpham.asm1.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductFacadeImpl implements ProductFacade {
    private final ProductService productService;
    private final ModelMapper modelMapper;


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
}
