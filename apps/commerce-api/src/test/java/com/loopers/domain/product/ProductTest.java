package com.loopers.domain.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.loopers.support.error.CoreException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

public class ProductTest {

    @DisplayName("상품 도메인 객체 생성 테스트")
    @Test
    void success_create_product() {
        String name = "product1";
        Long refBrandId = 105L;
        Integer price = 1000;
        Integer stock = 100;

        Product product = Product.create(null, name, refBrandId, price, stock);

        assertThat(product).isNotNull();
        assertThat(product.getId()).isEqualTo(null);
        assertThat(product.getName()).isEqualTo(name);
        assertThat(product.getRefBrandId()).isEqualTo(refBrandId);
        assertThat(product.getPrice()).isEqualTo(price);
        assertThat(product.getStock()).isEqualTo(stock);
    }

    @DisplayName("상품 이름이 유효하지 않다면, 생성시 예외를 던진다")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void fail_create_product_with_invalid_name(String name) {
        Long refBrandId = 105L;
        Integer price = 1000;
        Integer stock = 100;

        assertThatThrownBy(() -> Product.create(null, name, refBrandId, price, stock))
            .isInstanceOf(CoreException.class)
            .hasMessage("상품 이름은 필수 입니다");
    }

    @DisplayName("상품의 브랜드 정보가 유효하지 않다면, 생성시 예외를 던진다")
    @ParameterizedTest
    @NullSource
    @ValueSource(longs = {-10000L, -1L, 0L})
    void fail_create_product_with_invalid_brand_id(Long refBrandId) {
        String name = "product1";
        Integer price = 1000;
        Integer stock = 100;

        assertThatThrownBy(() -> Product.create(null, name, refBrandId, price, stock))
            .isInstanceOf(CoreException.class)
            .hasMessage("브랜드FK는 null이거나 0이하가 될 수 없습니다");
    }

    @DisplayName("상품 가격이 null이거나 음수라면, 생성시 예외를 던진다")
    @ParameterizedTest
    @NullSource
    @ValueSource(ints = {-10000, -1})
    void fail_create_product_with_invalid_price(Integer price) {
        String name = "product1";
        Long refBrandId = 105L;
        Integer stock = 100;

        assertThatThrownBy(() -> Product.create(null, name, refBrandId, price, stock))
            .isInstanceOf(CoreException.class)
            .hasMessage("상품 가격은 null이거나 음수가 될 수 없습니다");
    }

    @DisplayName("상품 재고가 null이거나 0 이하라면, 생성시 예외를 던진다")
    @ParameterizedTest
    @NullSource
    @ValueSource(ints = {-100, -1})
    void fail_create_product_with_invalid_stock(Integer stock) {
        String name = "product1";
        Long refBrandId = 105L;
        Integer price = 1000;

        assertThatThrownBy(() -> Product.create(null, name, refBrandId, price, stock))
            .isInstanceOf(CoreException.class)
            .hasMessage("상품 재고는 null이거나 음수가 될 수 없습니다");
    }

    @DisplayName("요청 수량보다 재고가 많으면 true, 재고가 적으면 false")
    @ParameterizedTest(name = "재고={0}, 요청={1} → {2}")
    @CsvSource({
        "1000, 1, true",
        "1, 1000, false"
    })
    void validate_stock_by_required_quantity(int stock, int requiredQuantity, boolean expected) {
        Product product = Product.create(null, "product1", 105L, 1000, stock);

        assertThat(product.hasEnoughStock(requiredQuantity)).isEqualTo(expected);
    }

    @DisplayName("재고 차감에 성공하면, 재고가 요청 수량만큼 줄어든다")
    @Test
    void success_decrease_stock() {
        Product product = Product.create(null, "product1", 105L, 1000, 100);

        product.decreaseStock(30);

        assertThat(product.getStock()).isEqualTo(70);
    }

    @DisplayName("재고보다 많은 수량을 차감하면, 예외를 던진다")
    @Test
    void fail_decrease_stock_when_not_enough() {
        Product product = Product.create(null, "product1", 105L, 1000, 10);

        assertThatThrownBy(() -> product.decreaseStock(100))
            .isInstanceOf(CoreException.class)
            .hasMessage("재고가 부족합니다");
    }
}
