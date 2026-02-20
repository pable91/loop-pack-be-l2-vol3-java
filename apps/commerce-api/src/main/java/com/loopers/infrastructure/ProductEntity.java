package com.loopers.infrastructure;

import jakarta.persistence.Column;
import com.loopers.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

/**
 *  Product DB 엔티티
 */
@Entity
@Table(name = "product")
@NoArgsConstructor
public class ProductEntity extends BaseEntity {

    @Comment("상품 이름")
    @Column(name = "name", nullable = false)
    private String name;

    @Comment("브랜드 id (ref)")
    @Column(name = "ref_brand_id", nullable = false)
    private Long refBrandId;

    @Comment("현재 판매가")
    @Column(name = "price", nullable = false)
    private Long price;

    @Comment("현재 재고")
    @Column(name = "stock", nullable = false)
    private Integer quantity;
}
