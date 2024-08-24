package com.wootecam.festivals.global.auth.purchase;

import java.time.LocalDateTime;

public record PurchaseSession(Long memberId, Long ticketId, LocalDateTime expirationTime) {

    public static PurchaseSession of(String value) {
        try {
            String[] parts = value.split(",");

            String parsedLoginMemberId = parts[0];
            String parsedTicketId = parts[1];
            String parsedExpirationTime = parts[2];

            LocalDateTime expirationTime = LocalDateTime.parse(parsedExpirationTime);

            return new PurchaseSession(Long.parseLong(parsedLoginMemberId), Long.parseLong(parsedTicketId),
                    expirationTime);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid purchase session value: " + value, e);
        }
    }

    public boolean isExpired(LocalDateTime currentTime) {
        return expirationTime.isBefore(currentTime);
    }

    public boolean isMember(Long memberId) {
        return this.memberId.equals(memberId);
    }

    public boolean isTicket(Long ticketId) {
        return this.ticketId.equals(ticketId);
    }
}