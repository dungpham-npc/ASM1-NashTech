package com.dungpham.asm1.controller;

import com.dungpham.asm1.common.mapper.CategoryMapper;
import com.dungpham.asm1.common.util.Util;
import com.dungpham.asm1.entity.Category;
import com.dungpham.asm1.infrastructure.aspect.Logged;
import com.dungpham.asm1.request.CategoryRequest;
import com.dungpham.asm1.response.BaseResponse;
import com.dungpham.asm1.response.CategoryResponse;
import com.dungpham.asm1.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/${api.version}/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final String tag = "Category APIs";

    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get all categories",
            tags = {tag})
    @Logged
    public BaseResponse<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> categoryResponses = categoryService.getAllCategories()
                .stream()
                .map(categoryMapper::toListResponse)
                .toList();
        return BaseResponse.build(categoryResponses, true);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get a category and details",
            tags = {tag})
    @Logged
    public BaseResponse<CategoryResponse> getCategory(@PathVariable Long id) {
        Category category = categoryService.getCategory(id);
        return BaseResponse.build(categoryMapper.toDetailedResponse(category), true);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new category", tags = {tag})
    @Logged
    @SecurityRequirement(name = "Bearer Authentication")
    public BaseResponse<CategoryResponse> createCategory(@RequestBody CategoryRequest request) {
        Category category = categoryMapper.toEntity(request);

        Category createdCategory = categoryService.createCategory(category);

        return BaseResponse.build(categoryMapper.toDetailedResponse(createdCategory), true);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update a category", tags = {tag})
    @Logged
    @SecurityRequirement(name = "Bearer Authentication")
    public BaseResponse<CategoryResponse> updateCategory(
            @PathVariable Long id, @RequestBody CategoryRequest request) {
        Category existingCategory = categoryService.getCategory(id);

        categoryMapper.updateEntityFromRequest(request, existingCategory);

        Category updatedCategory = categoryService.updateCategory(existingCategory, id);

        return BaseResponse.build(categoryMapper.toDetailedResponse(updatedCategory), true);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Delete a category",
            tags = {tag})
    @Logged
    @SecurityRequirement(name = "Bearer Authentication")
    public BaseResponse<String> deleteCategory(@PathVariable Long id) {
        categoryService.removeCategory(id);
        return BaseResponse.build("Category deleted successfully", true);
    }
}
