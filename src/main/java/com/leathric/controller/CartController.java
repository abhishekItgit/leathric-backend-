package com.leathric.controller;

import com.leathric.dto.ApiResponse;
import com.leathric.dto.CartDtos;
import com.leathric.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ApiResponse<CartDtos.CartResponse> getCart() {
        return ApiResponse.<CartDtos.CartResponse>builder().success(true).message("Cart fetched")
                .data(cartService.getCurrentUserCart()).build();
    }

    @PostMapping("/items")
    public ApiResponse<CartDtos.CartResponse> addItem(@Valid @RequestBody CartDtos.AddCartItemRequest request) {
        return ApiResponse.<CartDtos.CartResponse>builder().success(true).message("Item added")
                .data(cartService.addItem(request)).build();
    }

    @PutMapping("/items/{itemId}")
    public ApiResponse<CartDtos.CartResponse> updateItem(@PathVariable Long itemId,
                                                         @Valid @RequestBody CartDtos.UpdateCartItemRequest request) {
        return ApiResponse.<CartDtos.CartResponse>builder().success(true).message("Item updated")
                .data(cartService.updateItem(itemId, request)).build();
    }

    @DeleteMapping("/items/{itemId}")
    public ApiResponse<CartDtos.CartResponse> removeItem(@PathVariable Long itemId) {
        return ApiResponse.<CartDtos.CartResponse>builder().success(true).message("Item removed")
                .data(cartService.removeItem(itemId)).build();
    }
}
