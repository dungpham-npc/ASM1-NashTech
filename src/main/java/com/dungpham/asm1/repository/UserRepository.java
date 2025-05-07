package com.dungpham.asm1.repository;

import com.dungpham.asm1.entity.Product;
import com.dungpham.asm1.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @EntityGraph(attributePaths = {"role", "recipientInformation"})
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<User> findAll(Specification<User> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"role", "recipientInformation"})
    @Override
    User getReferenceById(Long id);
}
