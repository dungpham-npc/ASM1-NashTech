package com.dungpham.asm1.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@Setter
public class ProductDetailsResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private List<String> imageUrls;
    private Double averageRating;
    private CategoryResponse category;
}
