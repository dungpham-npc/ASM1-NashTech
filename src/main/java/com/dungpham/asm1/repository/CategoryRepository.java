package com.dungpham.asm1.repository;

import com.dungpham.asm1.entity.Category;
import com.dungpham.asm1.response.CategoryDetailsResponse;
import com.dungpham.asm1.response.CategoryListResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    @Query("""
        SELECT new com.dungpham.asm1.response.CategoryListResponse(c.id, c.name, COUNT(p.id))
        FROM Category c
        LEFT JOIN c.products p
        WHERE c.isActive = true
        GROUP BY c.id, c.name
       """)
    List<CategoryListResponse> findCategoryWithProductCount();

    Optional<Category> findByIdAndIsActiveTrue(Long id);

    Optional<Category> findByNameAndIsActiveTrue(String name);
}
