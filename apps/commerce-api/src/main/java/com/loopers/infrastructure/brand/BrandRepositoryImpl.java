package com.loopers.infrastructure.brand;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class BrandRepositoryImpl implements BrandRepository {

    private final BrandJpaRepository brandJpaRepository;

    @Override
    public Brand create(Brand brand) {
        BrandEntity brandEntity = BrandEntity.create(brand);

        return BrandEntity.toDomain(brandJpaRepository.save(brandEntity));
    }

    @Override
    public Brand update(Long id, Brand brand) {

        BrandEntity brandEntity = brandJpaRepository.findById(id)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "브랜드를 찾을 수 없습니다."));
        brandEntity.update(brand);

        return BrandEntity.toDomain(brandJpaRepository.save(brandEntity));
    }

    @Override
    public Optional<Brand> findById(Long id) {
        return brandJpaRepository.findById(id)
            .map(BrandEntity::toDomain);
    }

    @Override
    public boolean existsById(Long id) {
        return brandJpaRepository.existsById(id);
    }
}
