package com.dungpham.asm1.service;

import com.dungpham.asm1.entity.Category;

import java.util.List;

public interface CategoryService {
    List<Category> getAllCategories();

    Category getCategory(Long id);

    Category createCategory(Category category);

    Category updateCategory(Category category, Long id);

    void removeCategory(Long id);
}
