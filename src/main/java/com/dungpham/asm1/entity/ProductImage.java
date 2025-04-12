package com.dungpham.asm1.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable //Create a table with no primary key, lifecycle is managed by the parent entity
public class ProductImage {
    @Column( nullable = false)
    private String imageKey;

    @Column(nullable = false)
    private boolean isThumbnail;
}
