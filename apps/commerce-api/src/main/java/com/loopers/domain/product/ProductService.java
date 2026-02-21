package com.loopers.domain.product;

import com.loopers.domain.brand.BrandValidator;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    private final BrandValidator brandValidator;

    public void createProducts(Map<Long, CreateProductRequest> createProductsCommand) {
        createProductsCommand.keySet().forEach(brandValidator::validateExists);

        createProductsCommand.forEach((brandId, request) -> {
            Product product = Product.create(
                null,
                request.name(),
                brandId,
                request.price(),
                request.stock()
            );
            productRepository.save(product);
        });
    }
}
