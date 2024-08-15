package com.wootecam.festivals.domain.purchase.entity;

import com.wootecam.festivals.global.docs.EnumType;


public enum PurchaseStatus implements EnumType {

    // 결제 로직 추가 시 수정 필요
    PURCHASED("구매 완료"),
    REFUNDED("환불 완료"),
    ;

    private final String description;

    PurchaseStatus(String description) {
        this.description = description;
    }

    public boolean isPurchased() {
        return this == PURCHASED;
    }

    public boolean isRefunded() {
        return this == REFUNDED;
    }

    @Override
    public String getName() {
        return name();
    }

    @Override
    public String getDescription() {
        return description;
    }
}
