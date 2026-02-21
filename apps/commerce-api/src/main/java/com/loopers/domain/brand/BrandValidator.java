package com.loopers.domain.brand;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BrandValidator {

    private final BrandRepository brandRepository;

    public void validateExists(Long id) {
        if (!brandRepository.existsById(id)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "브랜드를 찾을 수 없습니다");
        }
    }
}
