package com.loopers.infrastructure.product;

import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.infrastructure.brand.BrandEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductJpaRepository productJpaRepository;

    @Override
    public Product save(Product product) {
        ProductEntity productEntity = ProductEntity.create(product);

        return ProductEntity.toDomain(productJpaRepository.save(productEntity));
    }

    @Override
    public Optional<Product> findById(Long id) {
        return productJpaRepository.findById(id)
            .map(ProductEntity::toDomain);
    }

    @Override
    public Product update(Product product) {
        ProductEntity productEntity = productJpaRepository.findById(product.getId())
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "브랜드를 찾을 수 없습니다."));
        productEntity.update(product);

        return ProductEntity.toDomain(productJpaRepository.save(productEntity));
    }
}
