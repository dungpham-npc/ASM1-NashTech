package com.dungpham.asm1.repository;

import com.dungpham.asm1.entity.Category;
import com.dungpham.asm1.response.CategoryResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    @Query("""
            SELECT new com.dungpham.asm1.response.CategoryResponse(c.name, COUNT(p))
            FROM Category c
            LEFT JOIN c.products p
            GROUP BY c.name
           """)
    List<CategoryResponse> findCategoryWithProductCount();
}
