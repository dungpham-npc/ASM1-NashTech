package com.dungpham.asm1.controller;

import com.dungpham.asm1.common.mapper.CartMapper;
import com.dungpham.asm1.entity.Cart;
import com.dungpham.asm1.entity.Product;
import com.dungpham.asm1.entity.ProductImage;
import com.dungpham.asm1.request.AddToCartRequest;
import com.dungpham.asm1.response.BaseResponse;
import com.dungpham.asm1.response.CartResponse;
import com.dungpham.asm1.response.ProductResponse;
import com.dungpham.asm1.service.CartService;
import com.dungpham.asm1.service.ProductImageService;
import com.dungpham.asm1.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/${api.version}/cart")
@RequiredArgsConstructor
public class CartController {
    private final String TAG = "Cart APIs";

    private final CartService cartService;
    private final ProductService productService;
    private final CartMapper cartMapper;
    private final ProductImageService productImageService;

    @GetMapping
    @SecurityRequirement(name = "Bearer Authentication")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get user cart", tags = {TAG})
    public BaseResponse<CartResponse> getUserCart() {
        Cart cart = cartService.getOrCreateCart();
        CartResponse cartResponse = cartMapper.toCartResponse(cart);

        populateProductThumbnails(cartResponse);

        return BaseResponse.build(cartResponse, true);
    }

    @PostMapping
    @SecurityRequirement(name = "Bearer Authentication")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Add item to cart", tags = {TAG})
    public BaseResponse<CartResponse> addItemToCart(@Valid @RequestBody AddToCartRequest request) {
        Cart updatedCart = cartService.addToCart(request);
        CartResponse cartResponse = cartMapper.toCartResponse(updatedCart);

        populateProductThumbnails(cartResponse);

        return BaseResponse.build(cartResponse, true);
    }

    @PutMapping("/{cartItemId}")
    @SecurityRequirement(name = "Bearer Authentication")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update item quantity in cart", tags = {TAG})
    public BaseResponse<CartResponse> updateCartItemQuantity(
            @PathVariable @NotNull Long cartItemId,
            @RequestParam @Min(1) int quantity) {
        Cart updatedCart = cartService.updateCartItemQuantity(cartItemId, quantity);
        CartResponse cartResponse = cartMapper.toCartResponse(updatedCart);

        populateProductThumbnails(cartResponse);
        return BaseResponse.build(cartResponse, true);
    }

    @DeleteMapping("/{cartItemId}")
    @SecurityRequirement(name = "Bearer Authentication")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Remove item from cart", tags = {TAG})
    public BaseResponse<CartResponse> removeCartItem(@PathVariable @NotNull Long cartItemId) {
        Cart updatedCart = cartService.removeCartItem(cartItemId);
        CartResponse cartResponse = cartMapper.toCartResponse(updatedCart);

        populateProductThumbnails(cartResponse);
        return BaseResponse.build(cartResponse, true);
    }

    @DeleteMapping
    @SecurityRequirement(name = "Bearer Authentication")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Clear cart", tags = {TAG})
    public BaseResponse<CartResponse> clearCart() {
        Cart clearedCart = cartService.clearCart();
        CartResponse cartResponse = cartMapper.toCartResponse(clearedCart);

        populateProductThumbnails(cartResponse);
        return BaseResponse.build(cartResponse, true);
    }

    private void populateProductThumbnails(CartResponse cartResponse) {
        cartResponse.getCartItems().forEach(cartItemResponse -> {
            Long productId = cartItemResponse.getProduct().getId();
            Product product = productService.getProductById(productId);
            ProductImage thumbnail = productImageService.getProductThumbnail(product);
            cartItemResponse.getProduct().setThumbnailImgKey(
                    thumbnail != null ? thumbnail.getImageKey() : "null.jpg");
        });
    }
}

