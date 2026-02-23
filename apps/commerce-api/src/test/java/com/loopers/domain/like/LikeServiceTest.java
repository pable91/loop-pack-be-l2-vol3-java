package com.loopers.domain.like;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.loopers.support.error.CoreException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @InjectMocks
    private LikeService likeService;

    @Mock
    private LikeRepository likeRepository;

    @DisplayName("이미 좋아요를 누른 상품이면, 예외를 던진다")
    @Test
    void fail_like_when_already_liked() {
        Long productId = 1L;
        Long userId = 2L;

        given(likeRepository.existByUniqueId(productId, userId)).willReturn(true);

        assertThatThrownBy(() -> likeService.like(productId, userId))
            .isInstanceOf(CoreException.class)
            .hasMessage("이미 좋아요를 누른 상품입니다");
    }
}
