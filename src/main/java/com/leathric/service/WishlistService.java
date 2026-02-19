package com.leathric.service;

import com.leathric.dto.WishlistDtos;

public interface WishlistService {
    
    /**
     * Get current user's wishlist
     */
    WishlistDtos.WishlistResponse getMyWishlist();

    /**
     * Add product to wishlist
     */
    WishlistDtos.WishlistResponse addToWishlist(WishlistDtos.AddToWishlistRequest request);

    /**
     * Remove product from wishlist
     */
    WishlistDtos.WishlistResponse removeFromWishlist(Long productId);

    /**
     * Clear entire wishlist
     */
    void clearWishlist();

    /**
     * Check if product is in wishlist
     */
    boolean isInWishlist(Long productId);
}
