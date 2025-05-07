package com.dungpham.asm1.service.impl;

import com.dungpham.asm1.common.exception.NotFoundException;
import com.dungpham.asm1.entity.Cart;
import com.dungpham.asm1.entity.CartItem;
import com.dungpham.asm1.entity.Product;
import com.dungpham.asm1.entity.User;
import com.dungpham.asm1.repository.CartItemRepository;
import com.dungpham.asm1.repository.CartRepository;
import com.dungpham.asm1.repository.ProductRepository;
import com.dungpham.asm1.request.AddToCartRequest;
import com.dungpham.asm1.service.CartService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserDetailsServiceImpl userService;

    @Override
    public Cart getOrCreateCart() {
        User currentUser = userService.getCurrentUser()
                .orElseThrow(() -> new SecurityException("User must be logged in to access the cart"));

        return cartRepository.findByUserId(currentUser.getId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(currentUser);
                    newCart.setTotalPrice(BigDecimal.ZERO);
                    return cartRepository.save(newCart);
                });
    }

    @Override
    public List<CartItem> getCartItems() {
        Cart cart = getOrCreateCart();
        return cart.getCartItems();
    }

    @Override
    @Transactional
    public Cart addToCart(AddToCartRequest request) {
        Cart cart = getOrCreateCart();
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new NotFoundException("Product"));

        CartItem existingItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity((int) (existingItem.getQuantity() + request.getQuantity()));
            cartItemRepository.save(existingItem);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setPrice(product.getPrice());
            newItem.setQuantity(Math.toIntExact(request.getQuantity()));
            cart.getCartItems().add(newItem);
            cartItemRepository.save(newItem);
        }

        return updateCartTotal(cart);
    }

    @Override
    @Transactional
    public Cart updateCartItemQuantity(Long cartItemId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new NotFoundException("Cart item"));

        item.setQuantity(quantity);
        cartItemRepository.save(item);
        return updateCartTotal(item.getCart());
    }

    @Override
    @Transactional
    public Cart removeCartItem(Long cartItemId) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new NotFoundException("Cart item"));

        Cart cart = item.getCart();
        cart.getCartItems().remove(item);
        cartItemRepository.delete(item);

        return updateCartTotal(cart);
    }

    @Override
    @Transactional
    public Cart clearCart() {
        Cart cart = getOrCreateCart();
        cart.getCartItems().clear();
        cartItemRepository.deleteByCartId(cart.getId());
        return updateCartTotal(cart);
    }

    @Override
    public BigDecimal calculateCartTotal() {
        Cart cart = getOrCreateCart();
        return cart.getCartItems().stream()
                .map(this::calculateItemTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal calculateItemTotal(CartItem item) {
        return item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
    }

    private Cart updateCartTotal(Cart cart) {
        BigDecimal total = calculateCartTotal();
        cart.setTotalPrice(total);
        return cartRepository.save(cart);
    }
}

