package com.loopers.domain.product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    Product save(Product product);

    Optional<Product> findById(Long id);

    Product update(Product product);

    List<Product> findAll(ProductSearchCondition condition);
}
