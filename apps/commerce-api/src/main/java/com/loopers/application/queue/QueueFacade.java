package com.loopers.application.queue;

import com.loopers.domain.queue.EntryToken;
import com.loopers.domain.queue.EntryTokenRepository;
import com.loopers.domain.queue.WaitingQueue;
import com.loopers.domain.queue.WaitingQueueRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorMessage;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QueueFacade {

    // 임의 TPS
    // TODO 부하 테스트후 다시 설정 필요
    static final int TPS = 200;

    private final WaitingQueueRepository waitingQueueRepository;
    private final EntryTokenRepository entryTokenRepository;

    public QueueInfo enter(Long userId) {
        long position = waitingQueueRepository.enter(userId);
        long totalWaiting = waitingQueueRepository.getSize();
        WaitingQueue queue = WaitingQueue.of(userId, position, totalWaiting);
        return QueueInfo.from(queue, TPS);
    }

    public QueueInfo getPosition(Long userId) {
        long position = waitingQueueRepository.getPosition(userId);
        if (position == -1L) {
            throw new CoreException(ErrorType.NOT_FOUND, ErrorMessage.Queue.NOT_IN_QUEUE);
        }
        long totalWaiting = waitingQueueRepository.getSize();
        WaitingQueue queue = WaitingQueue.of(userId, position, totalWaiting);
        return QueueInfo.from(queue, TPS);
    }

    public void validateAndConsumeToken(Long userId, String token) {
        EntryToken entryToken = entryTokenRepository.findByUserId(userId)
            .orElseThrow(() -> new CoreException(ErrorType.FORBIDDEN, ErrorMessage.Queue.INVALID_ENTRY_TOKEN));
        if (!entryToken.getToken().equals(token)) {
            throw new CoreException(ErrorType.FORBIDDEN, ErrorMessage.Queue.INVALID_ENTRY_TOKEN);
        }
        entryTokenRepository.deleteByUserId(userId);
    }
}
