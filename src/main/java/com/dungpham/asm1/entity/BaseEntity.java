package com.dungpham.asm1.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.Instant;

@MappedSuperclass
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class BaseEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Builder.Default
    @Setter
    private boolean isActive = true;

    @Column(nullable = false)
    @Builder.Default
    private Long createdAt = Instant.now().toEpochMilli();

    @Column(nullable = false)
    @Builder.Default
    private Long updatedAt = Instant.now().toEpochMilli();

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now().toEpochMilli();
    }
}
