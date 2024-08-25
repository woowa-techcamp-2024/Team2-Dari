package com.wootecam.festivals.domain.purchase.controller;

import static com.wootecam.festivals.domain.purchase.controller.PurchaseController.PURCHASABLE_TICKET_STOCK_KEY;
import static com.wootecam.festivals.domain.purchase.controller.PurchaseController.PURCHASABLE_TICKET_TIMESTAMP_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
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
import com.wootecam.festivals.domain.purchase.dto.PurchasableResponse;
import com.wootecam.festivals.domain.purchase.dto.PurchasePreviewInfoResponse;
import com.wootecam.festivals.domain.purchase.dto.PurchaseTicketResponse;
import com.wootecam.festivals.domain.purchase.exception.PurchaseErrorCode;
import com.wootecam.festivals.domain.purchase.service.PurchaseFacadeService;
import com.wootecam.festivals.domain.purchase.service.PurchaseService;
import com.wootecam.festivals.domain.ticket.exception.TicketErrorCode;
import com.wootecam.festivals.global.auth.AuthErrorCode;
import com.wootecam.festivals.global.exception.type.ApiException;
import java.time.LocalDateTime;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.context.WebApplicationContext;

@WebMvcTest(PurchaseController.class)
@ActiveProfiles("test")
public class PurchaseControllerTest extends RestDocsSupport {

    @Autowired
    WebApplicationContext context;

    private MockHttpSession session;

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

    @AfterEach
    public void clean() {
        if (session != null) {
            session.clearAttributes();
        }
    }

    @Override
    protected Object initController() {
        return new PurchaseController(purchaseFacadeService, purchaseService);
    }

    @Test
    @DisplayName("티켓 결제 가능 여부 확인 API - 성공")
    void checkPurchasable() throws Exception {
        //given
        session = new MockHttpSession();
        given(purchaseService.checkPurchasable(any(), any(), any()))
                .willReturn(new PurchasableResponse(true, 1L));

        //when then
        this.mockMvc.perform(get("/api/v1/festivals/{festivalId}/tickets/{ticketId}/purchase/check", 1L, 1L)
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.purchasable").value(true))
                .andDo(restDocs.document(
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("purchasable").type(JsonFieldType.BOOLEAN)
                                        .description("티켓 결제 가능 여부\n 티켓 재고가 없는 경우 false입니다."),
                                fieldWithPath("ticketStockId").type(JsonFieldType.NUMBER)
                                        .description("티켓 재고 ID\n 티켓 재고가 없는 경우 null입니다.")
                        )
                ));
    }

    @MethodSource("provideCheckPurchasableException")
    @ParameterizedTest
    @DisplayName("티켓 결제 가능 여부 확인 API - 실패")
    void fail_checkPurchasable(ApiException exception) throws Exception {
        //given
        session = new MockHttpSession();
        given(purchaseService.checkPurchasable(any(), any(), any())).willThrow(exception);

        //when then
        this.mockMvc.perform(get("/api/v1/festivals/{festivalId}/tickets/{ticketId}/purchase/check", 1L, 1L)
                        .session(session))
                .andExpect(status().is(exception.getErrorCode().getHttpStatus().value()))
                .andDo(restDocs.document(
                        responseFields(
                                fieldWithPath("errorCode").type(JsonFieldType.STRING).description("에러 코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("에러 메시지")
                        )
                ));
    }

    static Stream<Arguments> provideCheckPurchasableException() {
        return Stream.of(
                Arguments.of(new ApiException(TicketErrorCode.TICKET_NOT_FOUND)),
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
        session = new MockHttpSession();
        session.setAttribute(PURCHASABLE_TICKET_STOCK_KEY, 1L);

        given(purchaseService.getPurchasePreviewInfo(any(), any(), any(), any()))
                .willReturn(new PurchasePreviewInfoResponse(1L, "title", "img",
                        1L, "name", "detail", 1000L, 1, 1L,
                        LocalDateTime.now()));

        //when then
        this.mockMvc.perform(get("/api/v1/festivals/{festivalId}/tickets/{ticketId}/purchase", 1L, 1L)
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.festivalId").value(1L))
                .andExpect(jsonPath("$.data.festivalTitle").value("title"))
                .andExpect(jsonPath("$.data.festivalImg").value("img"))
                .andExpect(jsonPath("$.data.ticketId").value(1L))
                .andExpect(jsonPath("$.data.ticketName").value("name"))
                .andExpect(jsonPath("$.data.ticketDetail").value("detail"))
                .andExpect(jsonPath("$.data.ticketPrice").value(1000L))
                .andExpect(jsonPath("$.data.ticketQuantity").value(1))
                .andExpect(jsonPath("$.data.ticketStockId").value(1L))
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
                                fieldWithPath("ticketStockId").type(JsonFieldType.NUMBER)
                                        .description("티켓 재고 ID"),
                                fieldWithPath("endSaleTime").type(JsonFieldType.STRING).description("티켓 판매 종료 시간")
                        )
                ));
    }

    @ParameterizedTest
    @MethodSource("provideGetPurchasePreviewInfoException")
    @DisplayName("티켓 구매 미리보기 정보 조회 실패 API")
    void failGetPurchasePreviewInfo(ApiException exception) throws Exception {
        //given
        session = new MockHttpSession();
        session.setAttribute(PURCHASABLE_TICKET_STOCK_KEY, 1L);

        given(purchaseService.getPurchasePreviewInfo(any(), any(), any(), any())).willThrow(exception);

        //when then
        this.mockMvc.perform(get("/api/v1/festivals/{festivalId}/tickets/{ticketId}/purchase", 1L, 1L)
                        .session(session))
                .andExpect(status().is(exception.getErrorCode().getHttpStatus().value()))
                .andDo(restDocs.document(
                        responseFields(
                                fieldWithPath("errorCode").type(JsonFieldType.STRING).description("에러 코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("에러 메시지")
                        )
                ));
    }

    @Test
    @DisplayName("티켓 구매 미리보기 정보 조회 실패 API - 403")
    void failGetPurchasePreviewInfo_forbidden() throws Exception {
        //given
        session = new MockHttpSession();
        ApiException apiException = new ApiException(AuthErrorCode.FORBIDDEN);
        given(purchaseService.getPurchasePreviewInfo(any(), any(), any(), any())).willThrow(
                new ApiException(AuthErrorCode.FORBIDDEN));

        //when then
        this.mockMvc.perform(get("/api/v1/festivals/{festivalId}/tickets/{ticketId}/purchase", 1L, 1L)
                        .session(session))
                .andExpect(status().is(apiException.getErrorCode().getHttpStatus().value()))
                .andDo(restDocs.document(
                        responseFields(
                                fieldWithPath("errorCode").type(JsonFieldType.STRING).description("에러 코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("에러 메시지")
                        )
                ));
    }

    @Test
    @DisplayName("티켓 구매 성공 API")
    void createTicket() throws Exception {
        session = new MockHttpSession();
        session.setAttribute(PURCHASABLE_TICKET_STOCK_KEY, 1L);

        //given
        given(purchaseFacadeService.purchaseTicket(any(), any(), any(), any()))
                .willReturn(new PurchaseTicketResponse(1L, 1L));

        //when then
        this.mockMvc.perform(post("/api/v1/festivals/{festivalId}/tickets/{ticketId}/purchase", 1L, 1L)
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.purchaseId").value(1L))
                .andDo(restDocs.document(
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("purchaseId").type(JsonFieldType.NUMBER).description("생성된 티켓 구매 내역 ID"),
                                fieldWithPath("checkinId").type(JsonFieldType.NUMBER).description("생성된 체크인 내역 ID")
                        )
                ));

        assertAll(() -> assertThat(session.getAttribute(PURCHASABLE_TICKET_STOCK_KEY)).isNull(),
                () -> assertThat(session.getAttribute(PURCHASABLE_TICKET_TIMESTAMP_KEY)).isNull());
    }

    @MethodSource("provideException")
    @ParameterizedTest
    @DisplayName("티켓 구매 실패 API")
    void fail_createTicket(ApiException exception) throws Exception {
        //given
        session = new MockHttpSession();
        session.setAttribute(PURCHASABLE_TICKET_STOCK_KEY, 1L);

        given(purchaseFacadeService.purchaseTicket(any(), any(), any(), any())).willThrow(exception);

        //when then
        this.mockMvc.perform(post("/api/v1/festivals/{festivalId}/tickets/{ticketId}/purchase", 1L, 1L)
                        .session(session))
                .andExpect(status().is(exception.getErrorCode().getHttpStatus().value()))
                .andDo(restDocs.document(
                        responseFields(
                                fieldWithPath("errorCode").type(JsonFieldType.STRING).description("에러 코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("에러 메시지")
                        )
                ));
    }

    @Test
    @DisplayName("티켓 구매 실패 API - 403")
    void fail_createTicket_forbidden() throws Exception {
        //given
        session = new MockHttpSession();
        ApiException apiException = new ApiException(AuthErrorCode.FORBIDDEN);
        given(purchaseFacadeService.purchaseTicket(any(), any(), any(), any())).willThrow(apiException);

        //when then
        this.mockMvc.perform(post("/api/v1/festivals/{festivalId}/tickets/{ticketId}/purchase", 1L, 1L)
                        .session(session))
                .andExpect(status().is(apiException.getErrorCode().getHttpStatus().value()))
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
                Arguments.of(new ApiException(TicketErrorCode.TICKET_STOCK_NOT_FOUND)),
                Arguments.of(new ApiException(TicketErrorCode.TICKET_STOCK_EMPTY)),
                Arguments.of(new ApiException(PurchaseErrorCode.INVALID_TICKET_PURCHASE_TIME)),
                Arguments.of(new ApiException(PurchaseErrorCode.ALREADY_PURCHASED_TICKET))
        );
    }
}
