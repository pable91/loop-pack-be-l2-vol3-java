package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.PgClient;
import com.loopers.domain.payment.PgPaymentRequest;
import com.loopers.domain.payment.PgPaymentResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
public class PgClientImpl implements PgClient {

    private final RestClient restClient;

    public PgClientImpl(@Value("${pg.url:http://localhost:8082}") String pgUrl) {
        this.restClient = RestClient.builder()
            .baseUrl(pgUrl)
            .build();
    }

    @Override
    public PgPaymentResponse requestPayment(PgPaymentRequest request) {
        try {
            PgApiResponse response = restClient.post()
                .uri("/api/v1/payments")
                .header("X-USER-ID", request.userId())
                .contentType(MediaType.APPLICATION_JSON)
                .body(new PgRequestBody(
                    request.orderId(),
                    request.cardType(),
                    request.cardNo(),
                    request.amount(),
                    request.callbackUrl()
                ))
                .retrieve()
                .body(PgApiResponse.class);

            if (response == null || response.data() == null) {
                throw new CoreException(ErrorType.INTERNAL_ERROR, "PG 응답이 비어있습니다");
            }

            return new PgPaymentResponse(
                response.data().transactionKey(),
                response.data().status(),
                response.data().reason()
            );
        } catch (RestClientException e) {
            log.error("PG 호출 실패. request={}", request, e);
            throw new CoreException(ErrorType.INTERNAL_ERROR, "PG 호출에 실패했습니다");
        }
    }

    private record PgRequestBody(
        String orderId,
        String cardType,
        String cardNo,
        Long amount,
        String callbackUrl
    ) {}

    private record PgApiResponse(
        PgMeta meta,
        PgData data
    ) {}

    private record PgMeta(
        String result,
        String errorCode,
        String message
    ) {}

    private record PgData(
        String transactionKey,
        String status,
        String reason
    ) {}
}
