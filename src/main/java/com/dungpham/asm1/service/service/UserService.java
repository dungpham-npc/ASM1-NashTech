package com.dungpham.asm1.service.service;

import com.dungpham.asm1.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Optional;

public interface UserService extends UserDetailsService {
    User findByEmail(String mail);

//    void validateSignUp(RegisterRequest request);

    Optional<User> getCurrentUser();

    User createUser(User user);

    User findById(Long id);

    void updateUser(User user);

//    Page<User> findByFilter(UserCriteria criteria, boolean admin);
}
