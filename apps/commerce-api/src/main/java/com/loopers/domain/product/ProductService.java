package com.loopers.domain.product;

import com.loopers.domain.brand.BrandValidator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    private final BrandValidator brandValidator;

    public List<Product> createProducts(Map<Long, CreateProductRequest> createProductsCommand) {
        createProductsCommand.keySet().forEach(brandValidator::validateExists);

        List<Product> createdProducts = new ArrayList<>();

        createProductsCommand.forEach((brandId, request) -> {
            Product product = Product.create(
                null,
                request.name(),
                brandId,
                request.price(),
                request.stock()
            );
            Product savedProduct = productRepository.save(product);
            createdProducts.add(savedProduct);
        });

        return createdProducts;
    }
}
