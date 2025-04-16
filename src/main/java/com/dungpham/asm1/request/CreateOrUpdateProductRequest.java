package com.dungpham.asm1.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
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
    @NotNull(message = "Name is required")
    @NotBlank(message = "Name cannot be blank")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    @Schema(description = "product name", example = "Premium Coffee Bean")
    private String name;

    @Schema(description = "product description", example = "Premium coffee beans sourced from Colombia")
    private String description;

    @Schema(description = "featured status", example = "true")
    private boolean isFeatured;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Price must have at most 8 digits in whole part and 2 decimal places")
    @Schema(description = "product price", example = "29.99")
    private BigDecimal price;

    @NotNull(message = "Category ID is required")
    @Schema(description = "category id", example = "1")
    private Long categoryId;
}
