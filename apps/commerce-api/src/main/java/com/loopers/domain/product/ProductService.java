package com.loopers.domain.product;

import com.loopers.domain.brand.BrandValidator;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
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

    public void decreaseStock(Long productId, Integer decreaseStock) {
        // 상품이 존재해?
        Product product = findById(productId);

        // 상품의 수량이 충분해? + 수량 감소
        product.decreaseStock(decreaseStock);

        productRepository.update(product);
    }

    public Product findById(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new CoreException(ErrorType.BAD_REQUEST, "상품을 찾을 수 없습니다"));
    }
}
