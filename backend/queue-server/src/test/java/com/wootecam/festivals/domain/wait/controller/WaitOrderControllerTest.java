package com.wootecam.festivals.domain.wait.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wootecam.festivals.docs.utils.RestDocsSupport;
import com.wootecam.festivals.domain.wait.dto.WaitOrderCreateResponse;
import com.wootecam.festivals.domain.wait.dto.WaitOrderResponse;
import com.wootecam.festivals.domain.wait.service.WaitOrderService;
import com.wootecam.festivals.global.auth.Authentication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

@WebMvcTest(WaitOrderController.class)
@ActiveProfiles("test")
@DisplayName("WaitOrderController 클래스")
class WaitOrderControllerTest extends RestDocsSupport {

    private final Long festivalId = 1L;
    private final Long ticketId = 1L;
    private final Long memberId = 1L;
    @MockBean
    private WaitOrderService waitOrderService;

    @Override
    protected Object initController() {
        return new WaitOrderController(waitOrderService);
    }

    @Nested
    @DisplayName("joinQueue 메소드는")
    class Describe_joinQueue {

        @Test
        @DisplayName("대기열에 사용자를 추가하고, 대기열 블록 순서를 반환한다")
        void it_returns_wait_order_create_response() throws Exception {
            // Given
            WaitOrderCreateResponse response = new WaitOrderCreateResponse(1L);
            when(waitOrderService.createWaitOrder(any(), any())).thenReturn(response);
            Authentication authentication = new Authentication(memberId);

            // When & Then
            mockMvc.perform(
                            post("/api/v1/festivals/{festivalId}/tickets/{ticketId}/purchase/wait", festivalId, ticketId)
                                    .requestAttr("authentication", authentication)
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.waitOrderBlock").value(1L));
        }
    }

    @Nested
    @DisplayName("getQueuePosition 메소드는")
    class Describe_getQueuePosition {

        @Test
        @DisplayName("대기열 통과 가능 여부와 대기 순서를 반환한다")
        void it_returns_wait_order_response() throws Exception {
            // Given
            WaitOrderResponse response = new WaitOrderResponse(true, 0L);
            when(waitOrderService.getWaitOrder(any(), any(), any())).thenReturn(response);
            Authentication authentication = new Authentication(memberId);

            // When & Then
            mockMvc.perform(get("/api/v1/festivals/{festivalId}/tickets/{ticketId}/purchase/wait", festivalId, ticketId)
                            .param("waitOrderBlock", "1")
                            .requestAttr("authentication", authentication)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.purchasable").value(true))
                    .andExpect(jsonPath("$.data.waitOrder").value(0L));
        }
    }
}
