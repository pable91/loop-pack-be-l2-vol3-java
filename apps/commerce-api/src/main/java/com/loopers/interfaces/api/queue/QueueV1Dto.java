package com.loopers.interfaces.api.queue;

import com.loopers.application.queue.QueueInfo;

public class QueueV1Dto {

    public record EnterResponse(long position, long totalWaiting, long estimatedWaitSeconds) {
        public static EnterResponse from(QueueInfo info) {
            return new EnterResponse(
                info.position(),
                info.totalWaiting(),
                info.estimatedWaitSeconds()
            );
        }
    }
}
