package com.dungpham.asm1.repository;

import com.dungpham.asm1.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    List<Product> findTop5ByUpdatedAtNotNullAndIsFeaturedTrueAndIsActiveTrueOrderByUpdatedAtDesc();

    @EntityGraph(attributePaths = {"images"})
    Page<Product> findAll(Specification<Product> spec, Pageable pageable);

    Optional<Product> findByIdAndIsActiveTrue(Long id);
}
