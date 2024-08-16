package com.wootecam.festivals.domain.purchase.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.beneathPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wootecam.festivals.docs.utils.RestDocsSupport;
import com.wootecam.festivals.domain.purchase.dto.PurchaseIdResponse;
import com.wootecam.festivals.domain.purchase.exception.PurchaseErrorCode;
import com.wootecam.festivals.domain.purchase.service.PurchaseService;
import com.wootecam.festivals.domain.ticket.exception.TicketErrorCode;
import com.wootecam.festivals.global.exception.type.ApiException;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.ActiveProfiles;

@WebMvcTest(PurchaseController.class)
@ActiveProfiles("test")
public class PurchaseControllerTest extends RestDocsSupport {

    @MockBean
    private PurchaseService purchaseService;

    static Stream<Arguments> provideException() {
        return Stream.of(
                Arguments.of(new ApiException(PurchaseErrorCode.INVALID_TICKET_PURCHASE_TIME)),
                Arguments.of(new ApiException(PurchaseErrorCode.ALREADY_PURCHASED_TICKET)),
                Arguments.of(new ApiException(TicketErrorCode.TICKET_STOCK_EMPTY))
        );
    }

    @Override
    protected Object initController() {
        return new PurchaseController(purchaseService);
    }

    @Test
    @DisplayName("티켓 구매 성공 API")
    void createTicket() throws Exception {
        //given
        given(purchaseService.createPurchase(any(), any(), any()))
                .willReturn(new PurchaseIdResponse(1L));

        //when then
        this.mockMvc.perform(post("/api/v1/festivals/{festivalId}/tickets/{ticketId}/purchase", 1L, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.purchaseId").value(1L))
                .andDo(restDocs.document(
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("purchaseId").type(JsonFieldType.NUMBER).description("생성된 티켓 구매 내역 ID")
                        )
                ));
    }

    @MethodSource("provideException")
    @ParameterizedTest
    @DisplayName("티켓 구매 실패 API")
    void fail_createTicket(ApiException exception) throws Exception {
        //given
        given(purchaseService.createPurchase(any(), any(), any())).willThrow(exception);

        //when then
        this.mockMvc.perform(post("/api/v1/festivals/{festivalId}/tickets/{ticketId}/purchase", 1L, 1L))
                .andExpect(status().is(exception.getErrorCode().getHttpStatus().value()))
                .andDo(restDocs.document(
                        responseFields(
                                fieldWithPath("errorCode").type(JsonFieldType.STRING).description("에러 코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("에러 메시지")
                        )
                ));
    }
}
