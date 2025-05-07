package com.dungpham.asm1.response;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@Setter
public class CartResponse {
    private Long id;
    private BigDecimal totalPrice;
    private List<CartItemResponse> cartItems;
}
