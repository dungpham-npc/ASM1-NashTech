package com.dungpham.asm1.controller;

import com.dungpham.asm1.infrastructure.aspect.Logged;
import com.dungpham.asm1.response.BaseResponse;
import com.dungpham.asm1.response.CategoryResponse;
import com.dungpham.asm1.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/${api.version}/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get all categories with product count",
            tags = {"Category APIs"})
    @Logged
    public BaseResponse<List<CategoryResponse>> getAllCategories() {
        return BaseResponse.build(categoryService.getAllCategories(), true);
    }
}
