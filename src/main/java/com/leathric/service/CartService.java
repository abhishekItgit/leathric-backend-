package com.leathric.service;

import com.leathric.dto.CartDtos;

public interface CartService {
    CartDtos.CartResponse getCurrentUserCart();

    CartDtos.CartResponse addItem(CartDtos.AddCartItemRequest request);

    CartDtos.CartResponse updateItem(Long itemId, CartDtos.UpdateCartItemRequest request);

    CartDtos.CartResponse removeItem(Long itemId);
}
