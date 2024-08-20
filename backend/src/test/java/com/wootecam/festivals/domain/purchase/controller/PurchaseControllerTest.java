package com.wootecam.festivals.domain.purchase.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.beneathPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wootecam.festivals.docs.utils.RestDocsSupport;
import com.wootecam.festivals.domain.member.exception.MemberErrorCode;
import com.wootecam.festivals.domain.purchase.dto.PurchasePreviewInfoResponse;
import com.wootecam.festivals.domain.purchase.dto.PurchaseTicketResponse;
import com.wootecam.festivals.domain.purchase.exception.PurchaseErrorCode;
import com.wootecam.festivals.domain.purchase.service.PurchaseFacadeService;
import com.wootecam.festivals.domain.purchase.service.PurchaseService;
import com.wootecam.festivals.domain.ticket.exception.TicketErrorCode;
import com.wootecam.festivals.global.exception.type.ApiException;
import java.time.LocalDateTime;
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
    private PurchaseFacadeService purchaseFacadeService;

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
        return new PurchaseController(purchaseFacadeService, purchaseService);
    }

    @Test
    @DisplayName("티켓 구매 성공 API")
    void createTicket() throws Exception {
        //given
        given(purchaseFacadeService.purchaseTicket(any(), any(), any()))
                .willReturn(new PurchaseTicketResponse(1L, 1L));

        //when then
        this.mockMvc.perform(post("/api/v1/festivals/{festivalId}/tickets/{ticketId}/purchase", 1L, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.purchaseId").value(1L))
                .andDo(restDocs.document(
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("purchaseId").type(JsonFieldType.NUMBER).description("생성된 티켓 구매 내역 ID"),
                                fieldWithPath("checkinId").type(JsonFieldType.NUMBER).description("생성된 체크인 내역 ID")
                        )
                ));
    }

    @MethodSource("provideException")
    @ParameterizedTest
    @DisplayName("티켓 구매 실패 API")
    void fail_createTicket(ApiException exception) throws Exception {
        //given
        given(purchaseFacadeService.purchaseTicket(any(), any(), any())).willThrow(exception);

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

    static Stream<Arguments> provideGetPurchasePreviewInfoException() {
        return Stream.of(
                Arguments.of(new ApiException(TicketErrorCode.TICKET_NOT_FOUND)),
                Arguments.of(new ApiException(MemberErrorCode.USER_NOT_FOUND)),
                Arguments.of(new ApiException(TicketErrorCode.TICKET_STOCK_NOT_FOUND)),
                Arguments.of(new ApiException(TicketErrorCode.TICKET_STOCK_EMPTY)),
                Arguments.of(new ApiException(PurchaseErrorCode.INVALID_TICKET_PURCHASE_TIME)),
                Arguments.of(new ApiException(PurchaseErrorCode.ALREADY_PURCHASED_TICKET))
        );
    }

    @Test
    @DisplayName("티켓 구매 미리보기 정보 조회 API")
    void getPurchasePreviewInfo() throws Exception {
        //given
        given(purchaseService.getPurchasePreviewInfo(any(), any(), any()))
                .willReturn(new PurchasePreviewInfoResponse(1L, "title", "img",
                        1L, "name", "detail", 1000L, 1, 1,
                        LocalDateTime.now()));

        //when then
        this.mockMvc.perform(get("/api/v1/festivals/{festivalId}/tickets/{ticketId}/purchase", 1L, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.festivalId").value(1L))
                .andExpect(jsonPath("$.data.festivalTitle").value("title"))
                .andExpect(jsonPath("$.data.festivalImg").value("img"))
                .andExpect(jsonPath("$.data.ticketId").value(1L))
                .andExpect(jsonPath("$.data.ticketName").value("name"))
                .andExpect(jsonPath("$.data.ticketDetail").value("detail"))
                .andExpect(jsonPath("$.data.ticketPrice").value(1000L))
                .andExpect(jsonPath("$.data.ticketQuantity").value(1))
                .andExpect(jsonPath("$.data.remainTicketQuantity").value(1))
                .andDo(restDocs.document(
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("festivalId").type(JsonFieldType.NUMBER).description("축제 ID"),
                                fieldWithPath("festivalTitle").type(JsonFieldType.STRING).description("축제 제목"),
                                fieldWithPath("festivalImg").type(JsonFieldType.STRING).description("축제 이미지"),
                                fieldWithPath("ticketId").type(JsonFieldType.NUMBER).description("티켓 ID"),
                                fieldWithPath("ticketName").type(JsonFieldType.STRING).description("티켓 이름"),
                                fieldWithPath("ticketDetail").type(JsonFieldType.STRING).description("티켓 상세"),
                                fieldWithPath("ticketPrice").type(JsonFieldType.NUMBER).description("티켓 가격"),
                                fieldWithPath("ticketQuantity").type(JsonFieldType.NUMBER).description("티켓 수량"),
                                fieldWithPath("remainTicketQuantity").type(JsonFieldType.NUMBER)
                                        .description("남은 티켓 수량"),
                                fieldWithPath("endSaleTime").type(JsonFieldType.STRING).description("티켓 판매 종료 시간")
                        )
                ));
    }

    @ParameterizedTest
    @MethodSource("provideGetPurchasePreviewInfoException")
    @DisplayName("티켓 구매 미리보기 정보 조회 실패 API")
    void failGetPurchasePreviewInfo(ApiException exception) throws Exception {
        //given
        given(purchaseService.getPurchasePreviewInfo(any(), any(), any())).willThrow(exception);

        //when then
        this.mockMvc.perform(get("/api/v1/festivals/{festivalId}/tickets/{ticketId}/purchase", 1L, 1L))
                .andExpect(status().is(exception.getErrorCode().getHttpStatus().value()))
                .andDo(restDocs.document(
                        responseFields(
                                fieldWithPath("errorCode").type(JsonFieldType.STRING).description("에러 코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("에러 메시지")
                        )
                ));
    }
}
