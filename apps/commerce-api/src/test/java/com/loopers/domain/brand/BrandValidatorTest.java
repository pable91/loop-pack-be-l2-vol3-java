package com.loopers.domain.brand;

import static org.mockito.BDDMockito.given;

import com.loopers.support.error.CoreException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BrandValidatorTest {

    @InjectMocks
    private BrandValidator brandValidator;

    @Mock
    private BrandRepository brandRepository;

    @Test
    @DisplayName("브랜드가 존재하지 않으면 예외를 던진다")
    void fail_validateExists_not_found() {
        Long id = 10L;

        given(brandRepository.existsById(id)).willReturn(false);

        Assertions.assertThatThrownBy(() -> brandValidator.validateExists(id))
            .isInstanceOf(CoreException.class)
            .hasMessage("브랜드를 찾을 수 없습니다");
    }
}
