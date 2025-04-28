package com.dungpham.asm1.response;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    private String description;
    private BigDecimal price;
    private String thumbnailImgKey;
    private List<String> imageKeys;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.0")
    private BigDecimal averageRating;
    private CategoryResponse category;
}