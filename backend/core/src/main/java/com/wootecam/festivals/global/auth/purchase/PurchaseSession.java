package com.wootecam.festivals.global.auth.purchase;

public record PurchaseSession(Long ticketStockId) {

    public static PurchaseSession of(String str) {
        try {
            return new PurchaseSession(Long.parseLong(str));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid purchase session value: " + str, e);
        }
    }
}