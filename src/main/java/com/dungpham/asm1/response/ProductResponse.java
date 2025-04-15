package com.dungpham.asm1.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@Setter
public class ProductResponse {
    private Long id;
    private String name;
    private BigDecimal price;
    private String thumbnailUrl;
}
