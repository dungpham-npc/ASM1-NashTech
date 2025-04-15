package com.dungpham.asm1.service;

import com.dungpham.asm1.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Optional;

public interface UserService extends UserDetailsService {
    User getUserByEmail(String mail);

//    void validateSignUp(RegisterRequest request);

    Optional<User> getCurrentUser();

    User createUser(User user);

    User findById(Long id);

    void updateUser(User user);

//    Page<User> findByFilter(UserCriteria criteria, boolean admin);
}
