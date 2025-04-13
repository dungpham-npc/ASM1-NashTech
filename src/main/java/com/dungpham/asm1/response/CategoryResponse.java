package com.dungpham.asm1.response;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@Setter
public class CategoryResponse {
    private Long id;
    private String name;
}
