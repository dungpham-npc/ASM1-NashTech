package com.dungpham.asm1.service.impl;

import com.dungpham.asm1.infrastructure.aspect.Logged;
import com.dungpham.asm1.repository.CategoryRepository;
import com.dungpham.asm1.response.CategoryResponse;
import com.dungpham.asm1.service.CategoryService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    @Override
    @Logged
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findCategoryWithProductCount();
    }
}
