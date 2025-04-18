package com.dungpham.asm1.service.impl;

import com.dungpham.asm1.common.enums.ErrorCode;
import com.dungpham.asm1.common.exception.NotFoundException;
import com.dungpham.asm1.entity.Category;
import com.dungpham.asm1.infrastructure.aspect.Logged;
import com.dungpham.asm1.repository.CategoryRepository;
import com.dungpham.asm1.response.CategoryListResponse;
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
    public List<CategoryListResponse> getAllCategories() {
        return categoryRepository.findCategoryWithProductCount();
    }

    @Override
    @Logged
    public Category getCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category"));
    }
}
