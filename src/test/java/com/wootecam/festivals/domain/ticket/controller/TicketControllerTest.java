package com.wootecam.festivals.domain.ticket.controller;

import static com.wootecam.festivals.domain.ticket.utils.TicketValidConstant.TICKET_DETAIL_VALID_MESSAGE;
import static com.wootecam.festivals.domain.ticket.utils.TicketValidConstant.TICKET_END_TIME_EMPTY_VALID_MESSAGE;
import static com.wootecam.festivals.domain.ticket.utils.TicketValidConstant.TICKET_END_TIME_VALID_MESSAGE;
import static com.wootecam.festivals.domain.ticket.utils.TicketValidConstant.TICKET_NAME_EMPTY_VALID_MESSAGE;
import static com.wootecam.festivals.domain.ticket.utils.TicketValidConstant.TICKET_NAME_VALID_MESSAGE;
import static com.wootecam.festivals.domain.ticket.utils.TicketValidConstant.TICKET_PRICE_EMPTY_VALID_MESSAGE;
import static com.wootecam.festivals.domain.ticket.utils.TicketValidConstant.TICKET_PRICE_VALID_MESSAGE;
import static com.wootecam.festivals.domain.ticket.utils.TicketValidConstant.TICKET_QUANTITY_EMPTY_VALID_MESSAGE;
import static com.wootecam.festivals.domain.ticket.utils.TicketValidConstant.TICKET_QUANTITY_VALID_MESSAGE;
import static com.wootecam.festivals.domain.ticket.utils.TicketValidConstant.TICKET_REFUND_TIME_EMPTY_VALID_MESSAGE;
import static com.wootecam.festivals.domain.ticket.utils.TicketValidConstant.TICKET_REFUND_TIME_VALID_MESSAGE;
import static com.wootecam.festivals.domain.ticket.utils.TicketValidConstant.TICKET_START_TIME_EMPTY_VALID_MESSAGE;
import static com.wootecam.festivals.domain.ticket.utils.TicketValidConstant.TICKET_START_TIME_VALID_MESSAGE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.beneathPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wootecam.festivals.docs.utils.RestDocsSupport;
import com.wootecam.festivals.domain.ticket.dto.TicketCreateRequest;
import com.wootecam.festivals.domain.ticket.dto.TicketIdResponse;
import com.wootecam.festivals.domain.ticket.service.TicketService;
import java.time.LocalDateTime;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.ActiveProfiles;

@WebMvcTest(TicketController.class)
@ActiveProfiles("test")
class TicketControllerTest extends RestDocsSupport {

    @MockBean
    private TicketService ticketService;

    private static Stream<Arguments> provideInvalidTicketCreateRequests() {
        LocalDateTime now = LocalDateTime.now();
        return Stream.of(
                Arguments.of("a".repeat(101), "티켓 상세", 10000L, 100, now, now.plusDays(1), now.plusDays(1),
                        TICKET_NAME_VALID_MESSAGE),
                Arguments.of("티켓 이름", "a".repeat(1001), 10000L, 100, now, now.plusDays(1), now.plusDays(1),
                        TICKET_DETAIL_VALID_MESSAGE),
                Arguments.of("티켓 이름", "티켓 상세", -1L, 100, now, now.plusDays(1), now.plusDays(1),
                        TICKET_PRICE_VALID_MESSAGE),
                Arguments.of("티켓 이름", "티켓 상세", 10000000000L, 100, now, now.plusDays(1), now.plusDays(1),
                        TICKET_PRICE_VALID_MESSAGE),
                Arguments.of("티켓 이름", "티켓 상세", 10000L, 0, now, now.plusDays(1), now.plusDays(1),
                        TICKET_QUANTITY_VALID_MESSAGE),
                Arguments.of("티켓 이름", "티켓 상세", 10000L, 100001, now, now.plusDays(1), now.plusDays(1),
                        TICKET_QUANTITY_VALID_MESSAGE),
                Arguments.of("티켓 이름", "티켓 상세", 10000L, 100, now, now.minusDays(1), now.plusDays(1),
                        TICKET_END_TIME_VALID_MESSAGE),
                Arguments.of("티켓 이름", "티켓 상세", 10000L, 100, now, now.plusDays(1), now.minusDays(1),
                        TICKET_REFUND_TIME_VALID_MESSAGE)
        );
    }

    @Override
    protected Object initController() {
        return new TicketController(ticketService);
    }

    @Test
    @DisplayName("티켓 생성 API")
    void createTicket() throws Exception {
        Long expectedTicketId = 1L;
        LocalDateTime now = LocalDateTime.now();
        TicketCreateRequest ticketCreateRequest = new TicketCreateRequest("티켓 이름", "티켓 설명", 1000L, 100,
                now.plusDays(1), now.plusDays(2), now.plusDays(2));

        given(ticketService.createTicket(anyLong(), any(TicketCreateRequest.class)))
                .willReturn(new TicketIdResponse(expectedTicketId));

        mockMvc.perform(post("/api/v1/festivals/{festivalId}/tickets", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ticketCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.ticketId").value(expectedTicketId))
                .andDo(restDocs.document(
                        pathParameters(
                                parameterWithName("festivalId").description("축제 ID")
                        ),
                        requestFields(
                                fieldWithPath("name").type(JsonFieldType.STRING)
                                        .description("티켓 이름")
                                        .attributes(field("constraints",
                                                TICKET_NAME_EMPTY_VALID_MESSAGE + ", " + TICKET_NAME_VALID_MESSAGE)),
                                fieldWithPath("detail").type(JsonFieldType.STRING)
                                        .optional()
                                        .description("티켓 설명")
                                        .attributes(field("constraints", TICKET_DETAIL_VALID_MESSAGE)),
                                fieldWithPath("price").type(JsonFieldType.NUMBER)
                                        .description("티켓 가격")
                                        .attributes(field("constraints",
                                                TICKET_PRICE_EMPTY_VALID_MESSAGE + ", " + TICKET_PRICE_VALID_MESSAGE)),
                                fieldWithPath("quantity").type(JsonFieldType.NUMBER)
                                        .description("티켓 수량")
                                        .attributes(field("constraints", TICKET_QUANTITY_EMPTY_VALID_MESSAGE + ", "
                                                + TICKET_QUANTITY_VALID_MESSAGE)),
                                fieldWithPath("startSaleTime").type(JsonFieldType.STRING)
                                        .description("티켓 판매 시작 시간")
                                        .attributes(field("constraints", TICKET_START_TIME_EMPTY_VALID_MESSAGE + ", "
                                                + TICKET_START_TIME_VALID_MESSAGE)),
                                fieldWithPath("endSaleTime").type(JsonFieldType.STRING)
                                        .description("티켓 판매 종료 시간")
                                        .attributes(field("constraints", TICKET_END_TIME_EMPTY_VALID_MESSAGE + ", "
                                                + TICKET_END_TIME_VALID_MESSAGE)),
                                fieldWithPath("refundEndTime").type(JsonFieldType.STRING)
                                        .description("티켓 환불 종료 시간")
                                        .attributes(field("constraints", TICKET_REFUND_TIME_EMPTY_VALID_MESSAGE + ", "
                                                + TICKET_REFUND_TIME_VALID_MESSAGE))
                        ),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("ticketId").type(JsonFieldType.NUMBER).description("생성된 티켓 ID")
                        )
                ));
    }

    @ParameterizedTest
    @MethodSource("provideInvalidTicketCreateRequests")
    @DisplayName("티켓 생성 API - 유효성 검사 실패")
    void createTicketValidationFail(String name, String detail, Long price, Integer quantity,
                                    LocalDateTime startSaleTime,
                                    LocalDateTime endSaleTime, LocalDateTime refundEndTime, String errorMessage)
            throws Exception {
        TicketCreateRequest ticketCreateRequest = new TicketCreateRequest(name, detail, price, quantity, startSaleTime,
                endSaleTime, refundEndTime);

        mockMvc.perform(post("/api/v1/festivals/{festivalId}/tickets", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ticketCreateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(errorMessage));
    }
}