package com.loopers.application.order;

import com.loopers.domain.order.OrderItemService;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.order.OrderStatus;
import com.loopers.domain.order.OrderStatusHistoryService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderFacade {

    private final ProductService productService;
    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final OrderStatusHistoryService orderStatusHistoryService;

    public OrderInfo order(OrderCommand command) {
        List<Long> productIds = new ArrayList<>(command.productQuantities().keySet());
        List<Product> products = productService.getByIds(productIds);

        products.forEach(product ->
            productService.decreaseStock(product.getId(), command.productQuantities().get(product.getId())));

        var totalPrice = orderItemService.calculateTotalPrice(products, command.productQuantities());

        var order = orderService.createOrder(command.userId(), totalPrice.value());

        orderItemService.createOrderItems(order.getId(), products, command.productQuantities());

        orderStatusHistoryService.recordHistory(order.getId(), OrderStatus.ORDERED);

        return OrderInfo.from(order);
    }
}
