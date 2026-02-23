package com.loopers.domain.like;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

public class Like {

    private Long id;
    private Long refProductId;
    private Long refUserId;

    private Like(Long id, Long refProductId, Long refUserId) {
        this.id = id;
        this.refProductId = refProductId;
        this.refUserId = refUserId;
    }

    public static Like create(Long id, Long refProductId, Long refUserId) {
        validateRefId(refProductId, refUserId);

        return new Like(id, refProductId, refUserId);
    }

    private static void validateRefId(Long refProductId, Long refUserId) {
        if (refProductId == null || refProductId < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "상품FK는 null이거나 음수가 될 수 없습니다");
        }

        if (refUserId == null || refUserId < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "유저FK는 null이거나 음수가 될 수 없습니다");
        }
    }

    public Long getRefProductId() {
        return refProductId;
    }

    public Long getRefUserId() {
        return refUserId;
    }

}
