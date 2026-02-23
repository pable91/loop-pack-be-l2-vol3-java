package com.loopers.domain.product;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.loopers.support.error.CoreException;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    @Nested
    @DisplayName("상품 생성")
    class CreateProducts {

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("상품 생성 요청이 null이거나 비어있으면, 예외를 던진다")
        void fail_when_command_is_null_or_empty(Map<Long, CreateProductRequest> command) {
            assertThatThrownBy(() -> productService.createProducts(command))
                .isInstanceOf(CoreException.class)
                .hasMessage("상품 생성 요청은 필수입니다");
        }
    }

    @Nested
    @DisplayName("재고 차감")
    class DecreaseStock {

        @Test
        @DisplayName("상품이 존재하지 않으면, 예외를 던진다")
        void fail_when_product_not_found() {
            Long productId = 10101L;
            Integer decreaseStock = 100;

            given(productRepository.findById(productId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> productService.decreaseStock(productId, decreaseStock))
                .isInstanceOf(CoreException.class)
                .hasMessage("상품을 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("상품 목록 조회")
    class GetProducts {

        @Test
        @DisplayName("검색 조건이 null이면, 예외를 던진다")
        void fail_when_condition_is_null() {
            assertThatThrownBy(() -> productService.getProducts(null))
                .isInstanceOf(CoreException.class)
                .hasMessage("검색 조건은 필수입니다");
        }
    }
}
