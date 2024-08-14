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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wootecam.festivals.docs.utils.RestDocsSupport;
import com.wootecam.festivals.domain.ticket.dto.TicketCreateRequest;
import com.wootecam.festivals.domain.ticket.service.TicketService;
import java.time.LocalDateTime;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentMatchers;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

@WebMvcTest(TicketController.class)
class TicketControllerTest extends RestDocsSupport {

    @MockBean
    private TicketService ticketService;

    private TicketController ticketController;

    @Override
    protected Object initController() {
        ticketController = new TicketController(ticketService);
        return ticketController;
    }

    @Test
    @DisplayName("티켓 생성 API")
    void createTicket() throws Exception {
        // given
        Long expectedTicketId = 1L;
        LocalDateTime now = LocalDateTime.now();
        TicketCreateRequest ticketCreateRequest = new TicketCreateRequest("티켓 이름", "티켓 설명", 1000L, 100,
                now.plusDays(1), now.plusDays(2), now.plusDays(2));

        // when
        given(ticketService.createTicket(anyLong(), ArgumentMatchers.any(TicketCreateRequest.class)))
                .willReturn(expectedTicketId);

        // then
        this.mockMvc.perform(post("/api/v1/festivals/{festivalId}/tickets", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ticketCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.ticketId").value(expectedTicketId))
                .andDo(restDocs.document(
                        requestFields(
                                fieldWithPath("name").type(JsonFieldType.STRING)
                                        .attributes(field("constraints", TICKET_NAME_EMPTY_VALID_MESSAGE),
                                                field("constraints", TICKET_NAME_VALID_MESSAGE))
                                        .description("티켓 이름"),
                                fieldWithPath("detail").type(JsonFieldType.STRING)
                                        .optional()
                                        .attributes(field("constraints", TICKET_DETAIL_VALID_MESSAGE))
                                        .description("티켓 설명"),
                                fieldWithPath("price").type(JsonFieldType.NUMBER)
                                        .attributes(field("constraints", TICKET_PRICE_EMPTY_VALID_MESSAGE),
                                                field("constraints", TICKET_PRICE_VALID_MESSAGE))
                                        .description("티켓 가격"),
                                fieldWithPath("quantity").type(JsonFieldType.NUMBER)
                                        .attributes(field("constraints", TICKET_QUANTITY_EMPTY_VALID_MESSAGE),
                                                field("constraints", TICKET_QUANTITY_VALID_MESSAGE))
                                        .description("티켓 수량"),
                                fieldWithPath("startSaleTime").type(JsonFieldType.STRING)
                                        .attributes(field("constraints", TICKET_START_TIME_EMPTY_VALID_MESSAGE),
                                                field("constraints", TICKET_START_TIME_VALID_MESSAGE))
                                        .description("티켓 판매 시작 시간"),
                                fieldWithPath("endSaleTime").type(JsonFieldType.STRING)
                                        .attributes(field("constraints", TICKET_END_TIME_EMPTY_VALID_MESSAGE),
                                                field("constraints", TICKET_END_TIME_VALID_MESSAGE))
                                        .description("티켓 판매 종료 시간"),
                                fieldWithPath("refundEndTime").type(JsonFieldType.STRING)
                                        .attributes(field("constraints", TICKET_REFUND_TIME_EMPTY_VALID_MESSAGE),
                                                field("constraints", TICKET_REFUND_TIME_VALID_MESSAGE))
                                        .description("티켓 환불 종료 시간")
                        ),
                        responseFields(
                                fieldWithPath("data.ticketId").type(JsonFieldType.NUMBER).description("생성된 티켓 ID")
                        )
                ));
    }

    @ParameterizedTest
    @MethodSource("invalidTicketCreateDtos")
    @DisplayName("티켓 생성 API - Validation 실패")
    void createTicketValidationFail(String name, String detail, Long price, Integer quantity,
                                    LocalDateTime startSaleTime,
                                    LocalDateTime endSaleTime, LocalDateTime refundEndTime, String errorMessage)
            throws Exception {
        // given
        TicketCreateRequest ticketCreateRequest = new TicketCreateRequest(name, detail, price, quantity, startSaleTime,
                endSaleTime, refundEndTime);

        // then
        this.mockMvc.perform(post("/api/v1/festivals/{festivalId}/tickets", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ticketCreateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(errorMessage));
    }

    private static Stream<Arguments> invalidTicketCreateDtos() {
        LocalDateTime now = LocalDateTime.now();
        return Stream.of(
                Arguments.of("", "티켓 상세", 10000L, 100, now, now.plusDays(1), now.plusDays(1),
                        TICKET_NAME_EMPTY_VALID_MESSAGE),
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
}
