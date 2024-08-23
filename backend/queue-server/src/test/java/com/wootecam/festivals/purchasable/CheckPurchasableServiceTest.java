package com.wootecam.festivals.purchasable;

import static org.assertj.core.api.Assertions.assertThat;

import com.wootecam.festivals.purchasable.dto.PurchasableResponse;
import com.wootecam.festivals.purchasable.service.CheckPurchasableService;
import com.wootecam.festivals.utils.SpringBootTestConfig;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CheckPurchasableServiceTest extends SpringBootTestConfig {

    @Autowired
    private CheckPurchasableService checkPurchasableService;

    @Nested
    @DisplayName("checkPurchasable 메소드는")
    class Describe_checkPurchasable {

        @Nested
        @DisplayName("티켓이 구매 불가능한 상태일 때")
        class Context_unpurchasable {

            @Test
            @DisplayName("티켓 구매 여부를 false로 반환한다")
            void it_returns_PurchasableResponse() {
                // Given
                Long ticketId = 1L;
                Long loginMemberId = 1L;
                LocalDateTime now = LocalDateTime.now();

                // When
                PurchasableResponse response = checkPurchasableService.checkPurchasable(ticketId, loginMemberId, now);

                // Then
                assertThat(response.purchasable()).isFalse();
            }
        }
    }
}