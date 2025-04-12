package com.dungpham.asm1.controller;

import com.dungpham.asm1.entity.Product;
import com.dungpham.asm1.facade.ProductFacade;
import com.dungpham.asm1.infrastructure.aspect.Logged;
import com.dungpham.asm1.response.BaseResponse;
import com.dungpham.asm1.response.ProductResponse;
import com.dungpham.asm1.specification.ProductSpecification;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/${api.version}/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductFacade productFacade;

    @GetMapping("/featured")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get featured products",
            tags = {"Product APIs"})
    @Logged
    public BaseResponse<List<ProductResponse>> getFeaturedProducts() {
        return productFacade.getFeaturedProducts();
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get all products",
            tags = {"Product APIs"})
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

}
