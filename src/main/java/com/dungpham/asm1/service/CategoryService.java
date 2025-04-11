package com.dungpham.asm1.service;

import com.dungpham.asm1.response.CategoryListResponse;

import java.util.List;

public interface CategoryService {
    List<CategoryListResponse> getAllCategories();
}
