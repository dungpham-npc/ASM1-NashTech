package com.dungpham.asm1.service;

import com.dungpham.asm1.entity.Category;
import com.dungpham.asm1.response.CategoryListResponse;

import java.util.List;

public interface CategoryService {
    List<CategoryListResponse> getAllCategories();

    Category getCategory(Long id);
}
