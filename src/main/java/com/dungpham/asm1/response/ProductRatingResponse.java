package com.dungpham.asm1.response;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@Setter
public class ProductRatingResponse {
    private Long id;
    private Integer rating;
    private LocalDateTime updatedAt;
}
