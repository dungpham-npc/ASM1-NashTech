package com.dungpham.asm1.controller;

import com.dungpham.asm1.common.util.Util;
import com.dungpham.asm1.entity.Category;
import com.dungpham.asm1.infrastructure.aspect.Logged;
import com.dungpham.asm1.request.CreateOrUpdateCategoryRequest;
import com.dungpham.asm1.response.BaseResponse;
import com.dungpham.asm1.response.CategoryDetailsResponse;
import com.dungpham.asm1.response.CategoryListResponse;
import com.dungpham.asm1.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/${api.version}/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final String tag = "Category APIs";

    private final CategoryService categoryService;
    private final ModelMapper modelMapper;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get all categories with product count",
            tags = {tag})
    @Logged
    public BaseResponse<List<CategoryListResponse>> getAllCategories() {
        return BaseResponse.build(categoryService.getAllCategories(), true);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get a category and details",
            tags = {tag})
    @Logged
    public BaseResponse<CategoryDetailsResponse> getCategory(@PathVariable Long id) {
        Category category = categoryService.getCategory(id);
        return BaseResponse.build(toCategoryDetailsResponse(category), true);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create a new category",
            tags = {tag})
    @Logged
    public BaseResponse<CategoryDetailsResponse> createCategory(@RequestBody CreateOrUpdateCategoryRequest request) {
        Category category = modelMapper.map(request, Category.class);
        Category createdCategory = categoryService.createCategory(category);
        return BaseResponse.build(toCategoryDetailsResponse(createdCategory), true);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Update a category",
            tags = {tag})
    @Logged
    public BaseResponse<CategoryDetailsResponse> updateCategory(
            @PathVariable Long id, @RequestBody CreateOrUpdateCategoryRequest request) {
        Category category = modelMapper.map(request, Category.class);
        Category updatedCategory = categoryService.updateCategory(category, id);
        return BaseResponse.build(toCategoryDetailsResponse(updatedCategory), true);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Delete a category",
            tags = {tag})
    @Logged
    public BaseResponse<String> deleteCategory(@PathVariable Long id) {
        categoryService.removeCategory(id);
        return BaseResponse.build("Category deleted successfully", true);
    }

    private CategoryDetailsResponse toCategoryDetailsResponse(Category category) {
        CategoryDetailsResponse response = modelMapper.map(category, CategoryDetailsResponse.class);
        response.setCreatedAt(Util.convertTimestampToLocalDateTime(category.getCreatedAt()));
        response.setUpdatedAt(Util.convertTimestampToLocalDateTime(category.getUpdatedAt()));
        return response;
    }
}
