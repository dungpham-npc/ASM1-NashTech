package com.dungpham.asm1.service.impl;

import com.dungpham.asm1.common.exception.NotFoundException;
import com.dungpham.asm1.entity.User;
import com.dungpham.asm1.infrastructure.aspect.Logged;
import com.dungpham.asm1.infrastructure.security.SecurityUserDetails;
import com.dungpham.asm1.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private static final Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);
    private final UserRepository userRepository;
    @Override
    @Logged
    @Transactional
    public UserDetails loadUserByUsername(String mail) throws UsernameNotFoundException {
        User user = userRepository
                .findByEmail(mail)
                .orElseThrow(() -> new NotFoundException("User"));

        List<GrantedAuthority> authorityList =
                List.of(new SimpleGrantedAuthority(user.getRole().toString()));

        return SecurityUserDetails.build(user, authorityList);
    }

    @Logged
    public Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return Optional.empty();

        Object principal = authentication.getPrincipal();
        log.info("Principal: {}", principal);

        if (principal instanceof SecurityUserDetails securityUserDetails) {
            // Return the already fetched user from the authentication context
            return Optional.of(securityUserDetails.getUser());
        }

        return Optional.empty();
    }
}
