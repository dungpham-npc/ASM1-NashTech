package com.dungpham.asm1.service.impl;

import com.dungpham.asm1.common.enums.ErrorCode;
import com.dungpham.asm1.common.exception.UserException;
import com.dungpham.asm1.entity.User;
import com.dungpham.asm1.infrastructure.security.SecurityUserDetails;
import com.dungpham.asm1.repository.UserRepository;
import com.dungpham.asm1.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public User getUserByEmail(String mail) {
        return userRepository
                .findByEmail(mail)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));
    }

    @Override
    public Optional<User> getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return userRepository.findByEmail(userDetails.getUsername());
        }
        return Optional.empty();
    }

    @Override
    public User createUser(User user) {
        return null;
    }

    @Override
    public User findById(Long id) {
        return null;
    }

    @Override
    public void updateUser(User user) {

    }

    @Override
    public UserDetails loadUserByUsername(String mail) throws UsernameNotFoundException {
        User user =
                userRepository
                        .findByEmail(mail)
                        .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        List<GrantedAuthority> authorityList =
                List.of(new SimpleGrantedAuthority(user.getRole().toString()));

        return SecurityUserDetails.build(user, authorityList);
    }
}
