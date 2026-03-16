package com.loopers.domain.payment;

public interface PgClient {

    PgPaymentResponse requestPayment(PgPaymentRequest request);
}
