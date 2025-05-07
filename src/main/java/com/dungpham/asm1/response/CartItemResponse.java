package com.dungpham.asm1.response;

import lombok.*;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@Setter
public class CartItemResponse {
    private Long id;
    private ProductResponse product;
    private int quantity;
    private BigDecimal price;
    private BigDecimal total;
}
