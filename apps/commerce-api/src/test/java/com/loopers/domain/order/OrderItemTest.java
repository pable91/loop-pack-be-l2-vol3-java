package com.loopers.domain.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.loopers.support.error.CoreException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class OrderItemTest {

    private static final Long DEFAULT_ORDER_ID = 1L;
    private static final Long DEFAULT_PRODUCT_ID = 1L;
    private static final Integer DEFAULT_QUANTITY = 2;
    private static final Integer DEFAULT_PRICE = 10000;

    private static void assertCoreException(Runnable runnable, String message) {
        assertThatThrownBy(runnable::run)
            .isInstanceOf(CoreException.class)
            .hasMessage(message);
    }

    @Nested
    @DisplayName("주문 항목 생성")
    class Create {

        @Test
        @DisplayName("주문 항목 생성에 성공한다")
        void success_create_order_item() {
            OrderItem orderItem = OrderItem.create(null, DEFAULT_ORDER_ID, DEFAULT_PRODUCT_ID, DEFAULT_QUANTITY, DEFAULT_PRICE);

            assertThat(orderItem.refOrderId()).isEqualTo(DEFAULT_ORDER_ID);
            assertThat(orderItem.refProductId()).isEqualTo(DEFAULT_PRODUCT_ID);
            assertThat(orderItem.quantity()).isEqualTo(DEFAULT_QUANTITY);
            assertThat(orderItem.price()).isEqualTo(DEFAULT_PRICE);
        }

        @DisplayName("주문FK가 유효하지 않으면, 생성시 예외를 던진다")
        @ParameterizedTest
        @NullSource
        @ValueSource(longs = {-1L, 0L})
        void fail_when_invalid_ref_order_id(Long refOrderId) {
            assertCoreException(
                () -> OrderItem.create(null, refOrderId, DEFAULT_PRODUCT_ID, DEFAULT_QUANTITY, DEFAULT_PRICE),
                "주문FK는 null이거나 0이하가 될 수 없습니다"
            );
        }

        @DisplayName("상품FK가 유효하지 않으면, 생성시 예외를 던진다")
        @ParameterizedTest
        @NullSource
        @ValueSource(longs = {-1L, 0L})
        void fail_when_invalid_ref_product_id(Long refProductId) {
            assertCoreException(
                () -> OrderItem.create(null, DEFAULT_ORDER_ID, refProductId, DEFAULT_QUANTITY, DEFAULT_PRICE),
                "상품FK는 null이거나 0이하가 될 수 없습니다"
            );
        }

        @DisplayName("수량이 null이거나 양수가 아니면, 생성시 예외를 던진다")
        @ParameterizedTest
        @NullSource
        @ValueSource(ints = {-1, 0})
        void fail_when_invalid_quantity(Integer quantity) {
            assertCoreException(
                () -> OrderItem.create(null, DEFAULT_ORDER_ID, DEFAULT_PRODUCT_ID, quantity, DEFAULT_PRICE),
                "수량은 양수여야 합니다"
            );
        }

        @DisplayName("주문 금액이 null이거나 음수이면, 생성시 예외를 던진다")
        @ParameterizedTest
        @NullSource
        @ValueSource(ints = {-1, -1000})
        void fail_when_invalid_price(Integer price) {
            assertCoreException(
                () -> OrderItem.create(null, DEFAULT_ORDER_ID, DEFAULT_PRODUCT_ID, DEFAULT_QUANTITY, price),
                "주문 금액은 null이거나 음수가 될 수 없습니다"
            );
        }
    }
}
