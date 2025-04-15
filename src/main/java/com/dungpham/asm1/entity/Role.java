package com.dungpham.asm1.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "roles")
public class Role extends BaseEntity {
    @Column(nullable = false, unique = true, length = 50)
    private String name;
}
