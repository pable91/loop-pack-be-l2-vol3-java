package com.loopers.domain.brand;

import com.loopers.infrastructure.brand.BrandEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.time.ZonedDateTime;

public record Brand(Long id, String name, String description, ZonedDateTime createdAt) {

    public static Brand create(Long id, String name, String description) {
        validateName(name);

        return new Brand(id, name, description, null);
    }

    public static Brand toDomain(BrandEntity brandEntity) {
        return new Brand(brandEntity.getId(), brandEntity.getName(), brandEntity.getDescription(), brandEntity.getCreatedAt());
    }

    private static void validateName(String name) {
        if(name == null || name.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "브랜드 이름은 필수 입니다");
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
