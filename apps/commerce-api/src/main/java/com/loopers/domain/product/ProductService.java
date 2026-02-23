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
        if (createProductsCommand == null || createProductsCommand.isEmpty()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "상품 생성 요청은 필수입니다");
        }

        createProductsCommand.keySet().forEach(brandValidator::validateExists);

        List<Product> createdProducts = new ArrayList<>();

        createProductsCommand.forEach((brandId, request) -> {
            Product product = Product.create(
                null,
                request.name(),
                brandId,
                request.price(),
                request.stock(),
                0
            );
            Product savedProduct = productRepository.save(product);
            createdProducts.add(savedProduct);
        });

        return createdProducts;
    }

    public void increaseLikeCount(Long productId) {
        Product product = findById(productId);
        product.increaseLikeCount();
        productRepository.update(product);
    }

    public void decreaseLikeCount(Long productId) {
        Product product = findById(productId);
        product.decreaseLikeCount();
        productRepository.update(product);
    }

    public void decreaseStock(Long productId, Integer decreaseStock) {
        Product product = findById(productId);

        product.decreaseStock(decreaseStock);

        productRepository.update(product);
    }

    public Product findById(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new CoreException(ErrorType.BAD_REQUEST, "상품을 찾을 수 없습니다"));
    }

    public List<Product> getProducts(ProductSearchCondition condition) {
        if (condition == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "검색 조건은 필수입니다");
        }
        if (condition.hasBrandId()) {
            brandValidator.validateExists(condition.brandId());
        }
        return productRepository.findAll(condition);
    }
}
