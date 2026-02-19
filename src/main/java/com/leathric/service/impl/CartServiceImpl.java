package com.leathric.service.impl;

import com.leathric.dto.CartDtos;
import com.leathric.entity.*;
import com.leathric.exception.BadRequestException;
import com.leathric.exception.ResourceNotFoundException;
import com.leathric.repository.CartRepository;
import com.leathric.repository.ProductRepository;
import com.leathric.repository.UserRepository;
import com.leathric.service.CartService;
import com.leathric.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public CartDtos.CartResponse getCurrentUserCart() {
        return toResponse(getOrCreateUserCart());
    }

    @Override
    @Transactional
    public CartDtos.CartResponse addItem(CartDtos.AddCartItemRequest request) {
        Cart cart = getOrCreateUserCart();
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (product.getStockQuantity() < request.getQuantity()) {
            throw new BadRequestException("Insufficient stock");
        }

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElse(CartItem.builder().cart(cart).product(product).quantity(0).build());

        if (item.getId() == null) {
            cart.getItems().add(item);
        }
        item.setQuantity(item.getQuantity() + request.getQuantity());

        return toResponse(cartRepository.save(cart));
    }

    @Override
    @Transactional
    public CartDtos.CartResponse updateItem(Long itemId, CartDtos.UpdateCartItemRequest request) {
        Cart cart = getOrCreateUserCart();
        CartItem item = cart.getItems().stream().filter(i -> i.getId().equals(itemId)).findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        if (item.getProduct().getStockQuantity() < request.getQuantity()) {
            throw new BadRequestException("Insufficient stock");
        }

        item.setQuantity(request.getQuantity());
        return toResponse(cartRepository.save(cart));
    }

    @Override
    @Transactional
    public CartDtos.CartResponse removeItem(Long itemId) {
        Cart cart = getOrCreateUserCart();
        boolean removed = cart.getItems().removeIf(item -> item.getId().equals(itemId));
        if (!removed) {
            throw new ResourceNotFoundException("Cart item not found");
        }
        return toResponse(cartRepository.save(cart));
    }

    private Cart getOrCreateUserCart() {
        User user = userRepository.findByEmail(SecurityUtils.currentUserEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return cartRepository.findByUserId(user.getId()).orElseGet(() -> cartRepository.save(Cart.builder().user(user).build()));
    }

    private CartDtos.CartResponse toResponse(Cart cart) {
        var items = cart.getItems().stream().map(item -> {
            BigDecimal lineTotal = item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            return CartDtos.CartItemResponse.builder()
                    .cartItemId(item.getId())
                    .productId(item.getProduct().getId())
                    .productName(item.getProduct().getName())
                    .unitPrice(item.getProduct().getPrice())
                    .quantity(item.getQuantity())
                    .lineTotal(lineTotal)
                    .build();
        }).toList();

        BigDecimal total = items.stream().map(CartDtos.CartItemResponse::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartDtos.CartResponse.builder()
                .cartId(cart.getId())
                .items(items)
                .totalAmount(total)
                .build();
    }
}
