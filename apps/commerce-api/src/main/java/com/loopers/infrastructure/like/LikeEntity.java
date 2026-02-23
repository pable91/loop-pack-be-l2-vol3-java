package com.loopers.infrastructure.like;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "like")
public class LikeEntity extends BaseEntity {

    @Comment("상품 id (ref)")
    @Column(name = "ref_product_id", nullable = false)
    private Long refProductId;

    @Comment("유저 id (ref)")
    @Column(name = "ref_user_id", nullable = false)
    private Long refUserId;

    public Long getRefProductId() {
        return refProductId;
    }

    public Long getRefUserId() {
        return refUserId;
    }
}
