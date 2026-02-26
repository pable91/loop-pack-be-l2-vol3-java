package com.loopers.application.order;

import com.loopers.domain.order.OrderService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderFacade {

    private final ProductService productService;
    private final OrderService orderService;

    public void order(Long userId, Map<Long, Integer> productQuantities) {
        List<Long> productIds = new ArrayList<>(productQuantities.keySet());
        List<Product> products = productService.getByIds(productIds);

        products.forEach(product ->
            productService.decreaseStock(product.getId(), productQuantities.get(product.getId())));

        orderService.createOrderWithItems(userId, products, productQuantities);
    }
}
