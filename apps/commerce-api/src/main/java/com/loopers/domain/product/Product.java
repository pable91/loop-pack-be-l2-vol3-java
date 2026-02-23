package com.loopers.domain.product;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

/**
 *  Product 도메인
 */
public class Product {

    private final Long id;
    private final Long refBrandId;

    private String name;
    private Integer price;
    private Integer stock;
    private Integer likeCount;

    private Product(Long id, String name, Long refBrandId, Integer price, Integer stock, Integer likeCount) {
        this.id = id;
        this.name = name;
        this.refBrandId = refBrandId;
        this.price = price;
        this.stock = stock;
        this.likeCount = likeCount;
    }

    public static Product create(Long id, String name, Long refBrandId, Integer price, Integer stock,  Integer likeCount) {
        validateName(name);
        validateBrandId(refBrandId);
        validatePrice(price);
        validateStock(stock);
        validateLike(likeCount);

        return new Product(id, name, refBrandId, price, stock, likeCount);
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "상품 이름은 필수 입니다");
        }
    }

    private static void validateBrandId(Long refBrand) {
        if (refBrand == null || refBrand <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "브랜드FK는 null이거나 0이하가 될 수 없습니다");
        }
    }

    private static void validatePrice(Integer price) {
        if (price == null || price < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "상품 가격은 null이거나 음수가 될 수 없습니다");
        }
    }

    private static void validateStock(Integer stock) {
        if (stock == null || stock < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "상품 재고는 null이거나 음수가 될 수 없습니다");
        }
    }

    private static void validateLike(Integer likeCount) {
        if (likeCount == null || likeCount < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "좋아요 수는 null이거나 음수가 될 수 없습니다");
        }
    }

    public boolean hasEnoughStock(Integer requiredQuantity) {
        return this.stock >= requiredQuantity;
    }

    public void decreaseStock(Integer quantity) {
        validateQuantity(quantity);
        if (!hasEnoughStock(quantity)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "재고가 부족합니다");
        }
        this.stock -= quantity;
    }

    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "수량은 양수여야 합니다");
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Long getRefBrandId() {
        return refBrandId;
    }

    public Integer getPrice() {
        return price;
    }

    public Integer getStock() {
        return stock;
    }

    public Integer getLikeCount() { return likeCount; }
}
