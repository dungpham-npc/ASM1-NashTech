package com.dungpham.asm1.response;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@Setter
public class CategoryDetailsResponse {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;
    private boolean isActive;
}
