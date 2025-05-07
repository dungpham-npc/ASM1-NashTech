package com.dungpham.asm1.service;

import com.dungpham.asm1.entity.Cart;
import com.dungpham.asm1.entity.CartItem;
import com.dungpham.asm1.request.AddToCartRequest;

import java.math.BigDecimal;
import java.util.List;

public interface CartService {
    Cart getOrCreateCart();

    List<CartItem> getCartItems();

    Cart addToCart(AddToCartRequest request);

    Cart updateCartItemQuantity(Long cartItemId, int quantity);

    Cart removeCartItem(Long cartItemId);

    Cart clearCart();

    BigDecimal calculateCartTotal();

    BigDecimal calculateItemTotal(CartItem item);
}
