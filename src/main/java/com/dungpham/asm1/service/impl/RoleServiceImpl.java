package com.dungpham.asm1.service.impl;

import com.dungpham.asm1.common.exception.NotFoundException;
import com.dungpham.asm1.entity.Role;
import com.dungpham.asm1.infrastructure.aspect.Logged;
import com.dungpham.asm1.repository.RoleRepository;
import com.dungpham.asm1.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;
    @Override
    @Logged
    public Role getRoleById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Role"));
    }
}
