package com.loopers.domain.brand;

import static org.mockito.BDDMockito.given;

import com.loopers.support.error.CoreException;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BrandServiceTest {

    @InjectMocks
    private BrandService brandService;

    @Mock
    private BrandRepository brandRepository;

    @Test
    @DisplayName("브랜드 정보를 수정할 때 해당 브랜드가 존재하지않으면 예외를 던진다")
    void fail_modify_not_found() {
        Long id = 10L;
        String name = "나이키";
        String description = "나이키설명";

        given(brandRepository.findById(id)).willReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> brandService.update(id, name, description))
            .isInstanceOf(CoreException.class)
            .hasMessage("브랜드를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("id로 브랜드를 조회할 때 브랜드가 존재하지않으면 예외를 던진다")
    void fail_findById_not_found_brand() {
        Long id = 10L;

        given(brandRepository.findById(id)).willReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> brandService.findById(id))
            .isInstanceOf(CoreException.class)
            .hasMessage("브랜드를 찾을 수 없습니다");
    }

//        @Test
//    void check_default_return() {
//        Long id = 10L;
//
//        Optional<Brand> result = brandRepository.findById(id);
//
//        System.out.println("result: " + result);
//        System.out.println("isPresent: " + result.isPresent());
//    }

//    @Test
//    @DisplayName("BrandEntity를 생성할 때 ID 초기값을 확인한다")
//    void check_entity_initial_id() {
//        Brand brand = Brand.create(null, "테스트", "설명");
//
//        BrandEntity entity = BrandEntity.create(brand);
//
//        System.out.println("생성된 엔티티의 ID: " + entity.getId());
//
//        Assertions.assertThat(entity.getId()).isEqualTo(null);
//    }
}
