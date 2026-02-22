package com.loopers.domain.product;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

import com.loopers.domain.brand.BrandValidator;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
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
    private BrandValidator brandValidator;

    @Mock
    private ProductRepository productRepository;

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("상품 생성 요청이 null이거나 비어있으면, 예외를 던진다")
    void fail_createProducts_when_command_is_null_or_empty(Map<Long, CreateProductRequest> command) {
        assertThatThrownBy(() -> productService.createProducts(command))
            .isInstanceOf(CoreException.class)
            .hasMessage("상품 생성 요청은 필수입니다");
    }

    @Test
    @DisplayName("상품 생성시 브랜드가 존재하지 않으면 예외를 던진다")
    void fail_createProducts_when_brand_not_found() {
        Long brandId = 999L;
        CreateProductRequest request = new CreateProductRequest("product1", 100000, 10);
        Map<Long, CreateProductRequest> command = Map.of(brandId, request);

        willThrow(new CoreException(ErrorType.BAD_REQUEST, "브랜드를 찾을 수 없습니다"))
            .given(brandValidator).validateExists(brandId);

        assertThatThrownBy(() -> productService.createProducts(command))
            .isInstanceOf(CoreException.class)
            .hasMessage("브랜드를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("상품 수량 감소시 상품이 존재하지 않으면, 예외를 던진다")
    void fail_decreaseStock_when_product_not_found() {
        Long productId = 10101L;
        Integer decreaseStock = 100;

        given(productRepository.findById(productId)).willReturn(Optional.empty());

        assertThatThrownBy(() ->  productService.decreaseStock(productId, decreaseStock))
            .isInstanceOf(CoreException.class)
            .hasMessage("상품을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("상품 목록 페이징 조회시 브랜드가 존재하지 않으면, 예외를 던진다")
    void fail_getProducts_when_brand_not_found() {
        Long brandId = 999L;
        ProductSearchCondition condition = ProductSearchCondition.of(brandId, ProductSortType.LATEST, 0, 10);

        willThrow(new CoreException(ErrorType.BAD_REQUEST, "브랜드를 찾을 수 없습니다"))
            .given(brandValidator).validateExists(brandId);

        assertThatThrownBy(() -> productService.getProducts(condition))
            .isInstanceOf(CoreException.class)
            .hasMessage("브랜드를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("상품 목록 조회시 검색 조건이 null이면, 예외를 던진다")
    void fail_getProducts_when_condition_is_null() {
        assertThatThrownBy(() -> productService.getProducts(null))
            .isInstanceOf(CoreException.class)
            .hasMessage("검색 조건은 필수입니다");
    }
}
