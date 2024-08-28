package com.wootecam.festivals.domain.wait.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.beneathPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wootecam.festivals.docs.utils.RestDocsSupport;
import com.wootecam.festivals.domain.wait.dto.WaitOrderResponse;
import com.wootecam.festivals.domain.wait.service.WaitOrderService;
import com.wootecam.festivals.global.auth.Authentication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
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
    @DisplayName("getQueuePosition 메소드는")
    class Describe_getQueuePosition {

        @Test
        @DisplayName("대기열 통과 가능 여부와 대기 순서를 반환한다")
        void it_returns_wait_order_response() throws Exception {
            // Given
            WaitOrderResponse response = new WaitOrderResponse(true, 15L, 30L);
            when(waitOrderService.getWaitOrder(any(), any(), any())).thenReturn(response);
            Authentication authentication = new Authentication(memberId);

            // When & Then
            mockMvc.perform(get("/api/v1/festivals/{festivalId}/tickets/{ticketId}/purchase/wait", festivalId, ticketId)
                            .param("waitOrder", "30")
                            .requestAttr("authentication", authentication)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.purchasable").value(true))
                    .andExpect(jsonPath("$.data.relativeWaitOrder").value(15L))
                    .andExpect(jsonPath("$.data.absoluteWaitOrder").value(30L))
                    .andDo(restDocs.document(
                            queryParameters(
                                    parameterWithName("waitOrder").description("해당 사용자의 대기 번호, 대기열 통과 여부 판단 시 사용됨")
                            ),
                            responseFields(
                                    beneathPath("data").withSubsectionId("data"),
                                    fieldWithPath("purchasable").type(JsonFieldType.BOOLEAN).description("티켓 구매 가능 여부"),
                                    fieldWithPath("relativeWaitOrder").type(JsonFieldType.NUMBER)
                                            .description("사용자가 대기열 페이지에서 확인할 대기 번호"),
                                    fieldWithPath("absoluteWaitOrder").type(JsonFieldType.NUMBER)
                                            .description("대기열 통과 여부 판단 시 사용되는 대기 번호"))
                    ));
        }
    }
}
