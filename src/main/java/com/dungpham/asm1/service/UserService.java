package com.dungpham.asm1.service;

import com.dungpham.asm1.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User getUserByEmail(String mail);

//    void validateSignUp(RegisterRequest request);

    Page<User> getAllUsers(Specification<User> spec, Pageable pageable);

    User updateUserProfile(User user);

    User createUser(User user);

    User getUserById(Long id);

    void updatePassword(User user, String newPassword);

    void deactivateUser(Long id);
}
