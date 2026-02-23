package com.loopers.application.product;

import com.loopers.domain.brand.BrandService;
import com.loopers.domain.product.CreateProductRequest;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductSearchCondition;
import com.loopers.domain.product.ProductService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductFacade {

    private final BrandService brandService;
    private final ProductService productService;

    public List<Product> createProducts(Map<Long, CreateProductRequest> command) {
        command.keySet().forEach(brandService::findById);
        return productService.createProducts(command);
    }

    @Transactional(readOnly = true)
    public List<Product> getProducts(ProductSearchCondition condition) {
        if (condition.hasBrandId()) {
            brandService.findById(condition.brandId());
        }
        return productService.getProducts(condition);
    }
}
