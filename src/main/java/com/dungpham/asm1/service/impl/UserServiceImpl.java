package com.dungpham.asm1.service.impl;

import com.dungpham.asm1.common.exception.ConflictException;
import com.dungpham.asm1.common.exception.ForbiddenException;
import com.dungpham.asm1.common.exception.InvalidArgumentException;
import com.dungpham.asm1.common.exception.NotFoundException;
import com.dungpham.asm1.entity.RecipientInformation;
import com.dungpham.asm1.entity.User;
import com.dungpham.asm1.infrastructure.aspect.Logged;
import com.dungpham.asm1.infrastructure.security.SecurityUserDetails;
import com.dungpham.asm1.repository.UserRepository;
import com.dungpham.asm1.request.UpdateRecipientInfoRequest;
import com.dungpham.asm1.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Logged
    public User getUserByEmail(String mail) {
        User user = userRepository
                        .findByEmail(mail)
                        .orElseThrow(() -> new NotFoundException("User"));
        if (!user.isActive())
            throw new ForbiddenException("this user account");
        return user;
    }

    @Override
    @Logged
    public Page<User> getAllUsers(Specification<User> spec, Pageable pageable) {
        return userRepository.findAll(spec, pageable);
    }

    @Override
    @Transactional
    @Logged
    public User updateUserProfile(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new InvalidArgumentException("email", "Email cannot be empty");
        }

        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new InvalidArgumentException("password", "Password cannot be empty");
        }

        // Add null checks for recipient information
        if (user.getRecipientInformation() != null) {
            long count = user.getRecipientInformation()
                    .stream()
                    .filter(ri -> ri != null && Boolean.TRUE.equals(ri.getIsDefault()))
                    .count();

            if (count > 1) {
                throw new InvalidArgumentException("Recipient Information", "Only one recipient can be set as default.");
            }
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }

    @Override
    @Transactional
    @Logged
    public User createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ConflictException("User with this email");
        }
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new NotFoundException("Password");
        }
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new NotFoundException("Email");
        }
        if(user.getRole() == null) {
            throw new NotFoundException("Role");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    @Logged
    public User getUserById(Long id) {
        return userRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("User"));
    }

    @Override
    @Logged
    public void deactivateUser(Long id) {
        User user = userRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("User"));
        user.setActive(false);
        userRepository.save(user);
    }
}
