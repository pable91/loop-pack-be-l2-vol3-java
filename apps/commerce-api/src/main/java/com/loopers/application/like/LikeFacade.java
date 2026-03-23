package com.loopers.application.like;

import com.loopers.domain.like.LikedEvent;
import com.loopers.domain.like.LikeService;
import com.loopers.domain.like.UnlikedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LikeFacade {

    private final LikeService likeService;
    private final ApplicationEventPublisher eventPublisher;

    public void toggleLike(Long productId, Long userId) {
        if (likeService.isLiked(productId, userId)) {
            likeService.unlike(productId, userId);
            eventPublisher.publishEvent(new UnlikedEvent(productId));
        } else {
            likeService.like(productId, userId);
            eventPublisher.publishEvent(new LikedEvent(productId));
        }
    }
}
