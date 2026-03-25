package com.loopers.application.metrics;

import com.loopers.domain.event.EventHandled;
import com.loopers.domain.event.EventHandledRepository;
import com.loopers.domain.metrics.ProductMetrics;
import com.loopers.domain.metrics.ProductMetricsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductMetricsProcessor {

    private final ProductMetricsRepository productMetricsRepository;
    private final EventHandledRepository eventHandledRepository;

    @Transactional
    public void process(String eventId, String eventType, Long productId) {
        if (eventHandledRepository.existsByEventIdAndEventType(eventId, eventType)) {
            log.info("중복 이벤트 건너뜀. eventId={}, eventType={}", eventId, eventType);
            return;
        }

        ensureMetricsExists(productId);

        switch (eventType) {
            case "PRODUCT_VIEWED" -> productMetricsRepository.incrementViewCount(productId);
            case "LIKED" -> productMetricsRepository.incrementLikeCount(productId);
            case "UNLIKED" -> productMetricsRepository.decrementLikeCount(productId);
            case "ORDER_CONFIRMED" -> productMetricsRepository.incrementSalesCount(productId);
            default -> log.warn("알 수 없는 이벤트 타입. eventType={}", eventType);
        }

        eventHandledRepository.save(EventHandled.create(eventId, eventType));
    }

    private void ensureMetricsExists(Long productId) {
        productMetricsRepository.findByProductId(productId)
            .orElseGet(() -> productMetricsRepository.save(ProductMetrics.create(productId)));
    }
}
