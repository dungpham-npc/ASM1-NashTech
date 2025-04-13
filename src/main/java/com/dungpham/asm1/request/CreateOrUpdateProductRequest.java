package com.dungpham.asm1.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class CreateOrUpdateProductRequest {
    private String name;
    private String description;
    private boolean isFeatured;
    private BigDecimal price;
    private Long categoryId;
}
