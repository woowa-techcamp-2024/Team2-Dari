package com.wootecam.festivals.global.auth.purchase;

public record PurchaseSession(Long memberId, Long ticketStockId) {

    public static PurchaseSession of(String str) {
        try {
            String[] parts = str.split(":");

            if (parts.length != 4 || !parts[0].equals("members") || !parts[2].equals("ticketStocks")) {
                throw new IllegalArgumentException("Invalid format: " + str);
            }

            Long memberId = Long.valueOf(parts[1]);
            Long ticketStockId = Long.valueOf(parts[3]);

            return new PurchaseSession(memberId, ticketStockId);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid purchase session value: " + str, e);
        }
    }

    public boolean isMember(Long memberId) {
        return this.memberId.equals(memberId);
    }
}