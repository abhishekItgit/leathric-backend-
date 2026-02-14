package com.leathric.config;

import com.leathric.entity.Category;
import com.leathric.entity.Product;
import com.leathric.repository.CategoryRepository;
import com.leathric.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductSeeder implements CommandLineRunner {

    private static final String BASE_URL = "http://localhost:8080/uploads/products/";

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (productRepository.count() > 0) {
            return;
        }

        Category leatherCategory = categoryRepository.findAll()
                .stream()
                .findFirst()
                .orElseGet(() -> categoryRepository.save(Category.builder()
                        .name("Leather Goods")
                        .description("Premium handcrafted leather essentials")
                        .build()));

        List<Product> sampleProducts = List.of(
                product("Leather Jacket", "Classic full-grain leather jacket with durable stitching.", "219.99", "sample-leather-jacket.png", leatherCategory),
                product("Leather Wallet", "Minimal bi-fold wallet made from premium vegetable tanned leather.", "49.99", "sample-leather-wallet.png", leatherCategory),
                product("Leather Boots", "Rugged leather boots designed for comfort and all-day wear.", "149.99", "sample-leather-boots.png", leatherCategory),
                product("Leather Belt", "Polished leather belt with brushed metal buckle.", "34.99", "sample-leather-belt.png", leatherCategory),
                product("Leather Bag", "Spacious leather carry bag for daily essentials.", "179.99", "sample-leather-bag.png", leatherCategory),
                product("Leather Gloves", "Soft leather gloves with warm inner lining.", "39.99", "sample-leather-gloves.png", leatherCategory)
        );

        productRepository.saveAll(sampleProducts);
        log.info("Seeded {} dummy leather products", sampleProducts.size());
    }

    private Product product(String name, String description, String price, String imageFile, Category category) {
        return Product.builder()
                .name(name)
                .description(description)
                .price(new BigDecimal(price))
                .imageUrl(BASE_URL + imageFile)
                .stockQuantity(25)
                .category(category)
                .build();
    }
}
