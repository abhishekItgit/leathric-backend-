package com.leathric.controller;

import com.leathric.dto.ApiResponse;
import com.leathric.dto.WishlistDtos;
import com.leathric.service.WishlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    /**
     * Get user's wishlist
     * GET /api/wishlist
     */
    @GetMapping
    public ApiResponse<WishlistDtos.WishlistResponse> getWishlist() {
        return ApiResponse.<WishlistDtos.WishlistResponse>builder()
                .success(true)
                .message("Wishlist retrieved successfully")
                .data(wishlistService.getMyWishlist())
                .build();
    }

    /**
     * Add product to wishlist
     * POST /api/wishlist/items
     */
    @PostMapping("/items")
    public ApiResponse<WishlistDtos.WishlistResponse> addToWishlist(
            @Valid @RequestBody WishlistDtos.AddToWishlistRequest request) {
        return ApiResponse.<WishlistDtos.WishlistResponse>builder()
                .success(true)
                .message("Product added to wishlist")
                .data(wishlistService.addToWishlist(request))
                .build();
    }

    /**
     * Remove product from wishlist
     * DELETE /api/wishlist/items/{productId}
     */
    @DeleteMapping("/items/{productId}")
    public ApiResponse<WishlistDtos.WishlistResponse> removeFromWishlist(@PathVariable Long productId) {
        return ApiResponse.<WishlistDtos.WishlistResponse>builder()
                .success(true)
                .message("Product removed from wishlist")
                .data(wishlistService.removeFromWishlist(productId))
                .build();
    }

    /**
     * Clear entire wishlist
     * DELETE /api/wishlist
     */
    @DeleteMapping
    public ApiResponse<Void> clearWishlist() {
        wishlistService.clearWishlist();
        return ApiResponse.<Void>builder()
                .success(true)
                .message("Wishlist cleared successfully")
                .build();
    }

    /**
     * Check if product is in wishlist
     * GET /api/wishlist/check/{productId}
     */
    @GetMapping("/check/{productId}")
    public ApiResponse<Boolean> isInWishlist(@PathVariable Long productId) {
        return ApiResponse.<Boolean>builder()
                .success(true)
                .message("Wishlist check completed")
                .data(wishlistService.isInWishlist(productId))
                .build();
    }
}
