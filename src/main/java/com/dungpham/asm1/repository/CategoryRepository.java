package com.dungpham.asm1.repository;

import com.dungpham.asm1.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findAllByIsActiveTrue();
    Optional<Category> findByIdAndIsActiveTrue(Long id);

    Optional<Category> findByNameAndIsActiveTrue(String name);
}
