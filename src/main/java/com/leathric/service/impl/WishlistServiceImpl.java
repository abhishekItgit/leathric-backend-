package com.leathric.service.impl;

import com.leathric.dto.WishlistDtos;
import com.leathric.entity.*;
import com.leathric.exception.BadRequestException;
import com.leathric.exception.ResourceNotFoundException;
import com.leathric.repository.ProductRepository;
import com.leathric.repository.UserRepository;
import com.leathric.repository.WishlistRepository;
import com.leathric.service.WishlistService;
import com.leathric.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public WishlistDtos.WishlistResponse getMyWishlist() {
        log.debug("Fetching wishlist for user: {}", SecurityUtils.currentUserEmail());
        Wishlist wishlist = getOrCreateWishlist();
        return toWishlistResponse(wishlist);
    }

    @Override
    @Transactional
    public WishlistDtos.WishlistResponse addToWishlist(WishlistDtos.AddToWishlistRequest request) {
        log.info("Adding product {} to wishlist for user: {}", request.getProductId(), SecurityUtils.currentUserEmail());
        
        Wishlist wishlist = getOrCreateWishlist();
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        // Check if product already in wishlist
        boolean alreadyExists = wishlist.getItems().stream()
                .anyMatch(item -> item.getProduct().getId().equals(product.getId()));

        if (alreadyExists) {
            throw new BadRequestException("Product already in wishlist");
        }

        // Add to wishlist
        WishlistItem item = WishlistItem.builder()
                .wishlist(wishlist)
                .product(product)
                .build();
        
        wishlist.getItems().add(item);
        Wishlist saved = wishlistRepository.save(wishlist);
        
        log.info("Product {} added to wishlist successfully", product.getId());
        return toWishlistResponse(saved);
    }

    @Override
    @Transactional
    public WishlistDtos.WishlistResponse removeFromWishlist(Long productId) {
        log.info("Removing product {} from wishlist for user: {}", productId, SecurityUtils.currentUserEmail());
        
        Wishlist wishlist = getOrCreateWishlist();
        
        boolean removed = wishlist.getItems().removeIf(item -> 
            item.getProduct().getId().equals(productId)
        );

        if (!removed) {
            throw new ResourceNotFoundException("Product not found in wishlist");
        }

        Wishlist saved = wishlistRepository.save(wishlist);
        log.info("Product {} removed from wishlist successfully", productId);
        
        return toWishlistResponse(saved);
    }

    @Override
    @Transactional
    public void clearWishlist() {
        log.info("Clearing wishlist for user: {}", SecurityUtils.currentUserEmail());
        
        Wishlist wishlist = getOrCreateWishlist();
        wishlist.getItems().clear();
        wishlistRepository.save(wishlist);
        
        log.info("Wishlist cleared successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isInWishlist(Long productId) {
        User user = getCurrentUser();
        return wishlistRepository.findByUserIdWithItems(user.getId())
                .map(wishlist -> wishlist.getItems().stream()
                        .anyMatch(item -> item.getProduct().getId().equals(productId)))
                .orElse(false);
    }

    // ==================== Private Helper Methods ====================

    private User getCurrentUser() {
        return userRepository.findByEmail(SecurityUtils.currentUserEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Wishlist getOrCreateWishlist() {
        User user = getCurrentUser();
        
        // Try to find existing wishlist
        Optional<Wishlist> existing = wishlistRepository.findByUserIdWithItems(user.getId());
        if (existing.isPresent()) {
            return existing.get();
        }
        
        // Try to create new wishlist, handle race condition
        try {
            log.debug("Creating new wishlist for user: {}", user.getEmail());
            return wishlistRepository.save(Wishlist.builder()
                    .user(user)
                    .build());
        } catch (DataIntegrityViolationException e) {
            // Another thread created it, fetch again
            log.debug("Wishlist already created by another thread, fetching...");
            return wishlistRepository.findByUserIdWithItems(user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Wishlist not found after creation"));
        }
    }

    private WishlistDtos.WishlistResponse toWishlistResponse(Wishlist wishlist) {
        List<WishlistDtos.WishlistItemResponse> items = wishlist.getItems().stream()
                .map(item -> {
                    Product product = item.getProduct();
                    return WishlistDtos.WishlistItemResponse.builder()
                            .wishlistItemId(item.getId())
                            .productId(product.getId())
                            .productName(product.getName())
                            .productDescription(product.getDescription())
                            .price(product.getPrice())
                            .imageUrl(product.getImageUrl())
                            .stockQuantity(product.getStockQuantity())
                            .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                            .inStock(product.getStockQuantity() > 0)
                            .build();
                })
                .toList();

        return WishlistDtos.WishlistResponse.builder()
                .wishlistId(wishlist.getId())
                .itemCount(items.size())
                .items(items)
                .build();
    }
}
