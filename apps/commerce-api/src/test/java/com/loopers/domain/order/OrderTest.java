package com.loopers.domain.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.loopers.support.error.CoreException;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class OrderTest {

    private static final Long DEFAULT_USER_ID = 1L;
    private static final Integer DEFAULT_TOTAL_PRICE = 10000;
    private static final ZonedDateTime DEFAULT_ORDER_DT = ZonedDateTime.now();

    private static Order createOrder(Long refUserId, Integer totalPrice, ZonedDateTime orderDt) {
        return Order.create(null, refUserId, OrderStatus.ORDERED, totalPrice, orderDt);
    }

    private static void assertCoreException(Runnable runnable, String message) {
        assertThatThrownBy(runnable::run)
            .isInstanceOf(CoreException.class)
            .hasMessage(message);
    }

    @Nested
    @DisplayName("주문 생성")
    class Create {

        @Test
        @DisplayName("주문 생성에 성공한다")
        void success_create_order() {
            Order order = createOrder(DEFAULT_USER_ID, DEFAULT_TOTAL_PRICE, DEFAULT_ORDER_DT);

            assertThat(order.getRefUserId()).isEqualTo(DEFAULT_USER_ID);
            assertThat(order.getTotalPrice()).isEqualTo(DEFAULT_TOTAL_PRICE);
            assertThat(order.getStatus()).isEqualTo(OrderStatus.ORDERED);
            assertThat(order.getOrderDt()).isEqualTo(DEFAULT_ORDER_DT);
        }

        @DisplayName("유저 정보가 유효하지 않으면, 생성시 예외를 던진다")
        @ParameterizedTest
        @NullSource
        @ValueSource(longs = {-1L, 0L})
        void fail_when_invalid_ref_user_id(Long refUserId) {
            assertCoreException(
                () -> createOrder(refUserId, DEFAULT_TOTAL_PRICE, DEFAULT_ORDER_DT),
                "유저FK는 null이거나 0이하가 될 수 없습니다"
            );
        }

        @DisplayName("총 주문 금액이 null이거나 음수이면, 생성시 예외를 던진다")
        @ParameterizedTest
        @NullSource
        @ValueSource(ints = {-1, -1000})
        void fail_when_invalid_total_price(Integer totalPrice) {
            assertCoreException(
                () -> createOrder(DEFAULT_USER_ID, totalPrice, DEFAULT_ORDER_DT),
                "총 주문 금액은 null이거나 음수가 될 수 없습니다"
            );
        }

        @Test
        @DisplayName("주문 일시가 null이면, 생성시 예외를 던진다")
        void fail_when_order_dt_is_null() {
            assertCoreException(
                () -> createOrder(DEFAULT_USER_ID, DEFAULT_TOTAL_PRICE, null),
                "주문 일시는 필수입니다"
            );
        }
    }

    @Nested
    @DisplayName("주문 취소")
    class Cancel {

        @Test
        @DisplayName("주문 완료 상태에서 취소하면, 상태가 취소로 변경된다")
        void success_cancel_order() {
            Order order = createOrder(DEFAULT_USER_ID, DEFAULT_TOTAL_PRICE, DEFAULT_ORDER_DT);

            order.cancel();

            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        }

        @Test
        @DisplayName("이미 취소된 주문을 취소하면, 예외를 던진다")
        void fail_when_already_cancelled() {
            Order order = createOrder(DEFAULT_USER_ID, DEFAULT_TOTAL_PRICE, DEFAULT_ORDER_DT);
            order.cancel();

            assertCoreException(() -> order.cancel(), "주문 완료 상태에서만 취소할 수 있습니다");
        }
    }
}
