package com.dungpham.asm1.common.mapper;

import com.dungpham.asm1.entity.Cart;
import com.dungpham.asm1.entity.CartItem;
import com.dungpham.asm1.response.CartItemResponse;
import com.dungpham.asm1.response.CartResponse;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ProductMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CartMapper {

    @Mapping(source = "cartItems", target = "cartItems")
    CartResponse toCartResponse(Cart cart);

    @Mapping(source = "product", target = "product", qualifiedByName = "toProductResponse")
    @Mapping(target = "total", expression = "java(item.getPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity())))")
    CartItemResponse toCartItemResponse(CartItem item);

    List<CartItemResponse> toCartItemResponseList(List<CartItem> items);
}
