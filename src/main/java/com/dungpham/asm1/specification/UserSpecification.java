package com.dungpham.asm1.specification;

import com.dungpham.asm1.entity.Product;
import com.dungpham.asm1.entity.User;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {
    public static Specification<User> hasEmail(String email) {
        return (root, query, criteriaBuilder) -> {
            if (email == null || email.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(root.get("email"), "%" + email + "%");
        };
    }

    public static Specification<User> hasRole(Long roleId) {
        return (root, query, criteriaBuilder) -> {
            if (roleId == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("role").get("id"), roleId);
        };
    }
}
