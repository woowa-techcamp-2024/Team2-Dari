package com.wootecam.festivals.domain.wait.dto;

public record WaitOrderResponse(boolean purchasable, Long relativeWaitOrder, Long absoluteWaitOrder) {
}
