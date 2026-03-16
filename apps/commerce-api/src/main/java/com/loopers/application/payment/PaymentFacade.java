package com.loopers.application.payment;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.payment.Payment;
import com.loopers.domain.payment.PaymentRepository;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.payment.PgClient;
import com.loopers.domain.payment.PgPaymentRequest;
import com.loopers.domain.payment.PgPaymentResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorMessage;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentFacade {

    private final OrderRepository orderRepository;
    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;
    private final PgClient pgClient;

    @Value("${app.callback-url:http://localhost:8080/api/v1/payments/callback}")
    private String callbackUrl;

    /**
     * 결제 요청
     * 1. 주문 조회
     * 2. Payment 생성 (PENDING)
     * 3. PG 호출 (트랜잭션 밖)
     * 4. PG 응답 반환 (비동기 처리이므로 PENDING 상태로 반환)
     */
    public PaymentInfo requestPayment(PaymentCommand command) {
        // 1. 주문 조회 및 검증
        Order order = getOrder(command.orderId());

        // 2. Payment 생성
        Payment payment = paymentService.createPayment(order.getId());

        // 3. PG 호출 (트랜잭션 밖에서 실행)
        PgPaymentRequest pgRequest = PgPaymentRequest.of(
            String.valueOf(command.userId()),
            order.getId(),
            command.cardType(),
            command.cardNo(),
            order.getTotalPrice().value().longValue(),
            callbackUrl
        );

        try {
            PgPaymentResponse pgResponse = pgClient.requestPayment(pgRequest);
            log.info("PG 결제 요청 완료. transactionKey={}, status={}",
                pgResponse.transactionKey(), pgResponse.status());
        } catch (Exception e) {
            log.error("PG 결제 요청 실패. orderId={}", order.getId(), e);
            paymentService.markFailed(payment.getId(), e.getMessage());
            throw e;
        }

        return PaymentInfo.from(payment, order);
    }


    private Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, ErrorMessage.Order.ORDER_NOT_FOUND));
    }
}
