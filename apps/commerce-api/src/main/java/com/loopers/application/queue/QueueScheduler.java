package com.loopers.application.queue;

import com.loopers.domain.queue.EntryToken;
import com.loopers.domain.queue.EntryTokenRepository;
import com.loopers.domain.queue.WaitingQueueRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class QueueScheduler {

    // 100ms마다 20명씩 발급 → 초당 200명(TPS) 처리
    static final int BATCH_SIZE = 20;

    private final WaitingQueueRepository waitingQueueRepository;
    private final EntryTokenRepository entryTokenRepository;

    @Scheduled(fixedDelay = 100)
    public void issueTokens() {
        try {
            List<Long> userIds = waitingQueueRepository.popFront(BATCH_SIZE);
            for (Long userId : userIds) {
                EntryToken token = EntryToken.issue(userId);
                entryTokenRepository.save(token);
                log.debug("Entry token issued. userId={}", userId);
            }
        } catch (Exception e) {
            log.error("Failed to issue entry tokens. Skipping batch.", e);
        }
    }
}
