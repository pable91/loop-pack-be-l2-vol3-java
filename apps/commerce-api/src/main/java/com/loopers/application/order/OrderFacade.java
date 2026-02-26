package com.loopers.application.order;

import com.loopers.domain.order.OrderItem;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserService;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderFacade {

    private final ProductService productService;
    private final UserService userService;
    private final OrderService orderService;

    public void order(Long userId, Map<Long, Integer> productQuantities) {
        Long validatedUserId = userService.findById(userId).getId();

        List<Long> productIds = new ArrayList<>(productQuantities.keySet());
        List<Product> products = productService.findByIds(productIds);

        Map<Product, Integer> productWithQuantities = products.stream()
            .collect(Collectors.toMap(
                product -> product,
                product -> productQuantities.get(product.getId()),
                (a, b) -> a,
                LinkedHashMap::new
            ));

        productWithQuantities.forEach((product, quantity) ->
            productService.decreaseStock(product.getId(), quantity));

        int totalPrice = productWithQuantities.entrySet().stream()
            .mapToInt(e -> e.getKey().getPrice() * e.getValue())
            .sum();

        var order = orderService.createOrder(validatedUserId, totalPrice);

        List<OrderItem> orderItems = productWithQuantities.entrySet().stream()
            .map(e -> OrderItem.create(null, order.getId(), e.getKey().getId(), e.getValue(), e.getKey().getPrice()))
            .toList();
        orderService.createOrderItems(order.getId(), orderItems);
    }
}
