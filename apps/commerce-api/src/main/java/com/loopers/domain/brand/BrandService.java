package com.loopers.domain.brand;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository brandRepository;

    public Brand create(String name, String description) {
        Brand brand = Brand.create(null, name, description);
        return  brandRepository.create(brand);
    }

    public Brand update(Long id, String name, String description) {
        Brand existing = findById(id);
        Brand updated = existing.update(name, description);
        return brandRepository.update(id, updated);
    }

    public Brand findById(Long id) {
        return brandRepository.findById(id)
            .orElseThrow(() -> new CoreException(ErrorType.BAD_REQUEST, "브랜드를 찾을 수 없습니다"));
    }
}
