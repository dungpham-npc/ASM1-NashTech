package com.dungpham.asm1.controller;

import com.dungpham.asm1.entity.Product;
import com.dungpham.asm1.facade.ProductFacade;
import com.dungpham.asm1.infrastructure.aspect.Logged;
import com.dungpham.asm1.request.ProductRequest;
import com.dungpham.asm1.response.BaseResponse;
import com.dungpham.asm1.response.ProductResponse;
import com.dungpham.asm1.specification.ProductSpecification;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/${api.version}/products")
@RequiredArgsConstructor
public class ProductController {
    private final String TAG = "Product APIs";

    private final ProductFacade productFacade;

    @GetMapping("/featured")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get featured products",
            tags = {TAG})
    @Logged
    public BaseResponse<List<ProductResponse>> getFeaturedProducts() {
        return productFacade.getFeaturedProducts();
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get all products",
            tags = {TAG})
    @Logged
    public BaseResponse<Page<ProductResponse>> getAllProducts(
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort) {

        Sort sortObj = Sort.by(Sort.Direction.fromString(sort[1]), sort[0]);
        Pageable pageable = PageRequest.of(page, size, sortObj);

        Specification<Product> spec = Specification
                .where(ProductSpecification.hasName(productName))
                .and(ProductSpecification.hasPriceInRange(minPrice, maxPrice))
                .and(ProductSpecification.hasCategoryId(categoryId));

        return productFacade.getAllProducts(spec, pageable);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get a product's details by id",
            tags = {TAG})
    @Logged
    public BaseResponse<ProductResponse> getProductById(@PathVariable Long id) {
        return productFacade.getProductDetails(id);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create a new product",
            tags = {TAG})
    @Logged
    @SecurityRequirement(name = "Bearer Authentication")
    public BaseResponse<ProductResponse> createProduct(
            @RequestPart(value = "productImages") List<MultipartFile> productImages,
            @RequestPart(value = "request") ProductRequest request) {
        return productFacade.createProduct(request, productImages);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Update a product",
            tags = {TAG})
    @Logged
    @SecurityRequirement(name = "Bearer Authentication")
    public BaseResponse<ProductResponse> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductRequest request) {
        return productFacade.updateProduct(request, id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Delete a product",
            tags = {TAG})
    @Logged
    @SecurityRequirement(name = "Bearer Authentication")
    public BaseResponse<String> removeProduct(@PathVariable Long id) {
        productFacade.removeProduct(id);
        return BaseResponse.build("Product deleted successfully", true);
    }

    @PostMapping("/{id}/rate")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Rate a product",
            tags = {TAG})
    @Logged
    @SecurityRequirement(name = "Bearer Authentication")
    public BaseResponse<String> rateProduct(
            @PathVariable Long id,
            @RequestParam Integer rating) {
        return productFacade.rateProduct(id, rating);
    }

}
