package com.dungpham.asm1.service.impl;

import com.dungpham.asm1.common.exception.ConflictException;
import com.dungpham.asm1.common.exception.InvalidArgumentException;
import com.dungpham.asm1.common.exception.NotFoundException;
import com.dungpham.asm1.entity.Category;
import com.dungpham.asm1.infrastructure.aspect.Logged;
import com.dungpham.asm1.repository.CategoryRepository;
import com.dungpham.asm1.response.CategoryListResponse;
import com.dungpham.asm1.service.CategoryService;
import jakarta.transaction.Transactional;
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
        return categoryRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new NotFoundException("Category"));
    }

    @Override
    @Logged
    @Transactional
    public Category createCategory(Category category) {
        validateCategory(category, "create");
        return categoryRepository.save(category);
    }

    @Override
    @Logged
    @Transactional
    public Category updateCategory(Category categoryToUpdate, Long id) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category"));
        validateCategory(categoryToUpdate, "update");
        existingCategory.setName(categoryToUpdate.getName());
        existingCategory.setDescription(categoryToUpdate.getDescription());
        return categoryRepository.save(existingCategory);
    }

    @Override
    @Logged
    @Transactional
    public void removeCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category"));
        validateCategory(category, "delete");
        category.setActive(false);
        categoryRepository.save(category);
    }

    private void validateCategory(Category category, String operation) {
        switch (operation.toLowerCase()) {
            case "create", "update":
                if (category.getName() == null || category.getName().isEmpty()) {
                    throw new InvalidArgumentException("name", "Category name cannot be empty");
                }
                if (category.getDescription() == null || category.getDescription().isEmpty()) {
                    throw new InvalidArgumentException("description", "Category description cannot be empty");
                }
                break;

            case "delete":
                if (!category.getProducts().isEmpty()) {
                    throw new InvalidArgumentException("category", "Category has products, cannot be deleted");
                }
                break;

            default:
                throw new IllegalArgumentException("Unknown operation: " + operation);
        }
    }
}
