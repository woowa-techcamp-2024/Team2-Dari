package com.wootecam.festivals.domain.my.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.beneathPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wootecam.festivals.docs.utils.RestDocsSupport;
import com.wootecam.festivals.domain.festival.dto.FestivalAdminResponse;
import com.wootecam.festivals.domain.festival.dto.FestivalResponse;
import com.wootecam.festivals.domain.festival.entity.FestivalProgressStatus;
import com.wootecam.festivals.domain.festival.entity.FestivalPublicationStatus;
import com.wootecam.festivals.domain.my.dto.MyFestivalCursor;
import com.wootecam.festivals.domain.my.dto.MyFestivalResponse;
import com.wootecam.festivals.domain.my.dto.MyPurchasedFestivalResponse;
import com.wootecam.festivals.domain.my.dto.MyPurchasedTicketResponse;
import com.wootecam.festivals.domain.my.service.MyService;
import com.wootecam.festivals.domain.purchase.entity.PurchaseStatus;
import com.wootecam.festivals.domain.ticket.dto.TicketWithoutStockResponse;
import com.wootecam.festivals.global.constants.GlobalConstants;
import com.wootecam.festivals.global.page.CursorBasedPage;
import com.wootecam.festivals.global.utils.DateTimeUtils;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.ActiveProfiles;

@WebMvcTest(MyController.class)
@ActiveProfiles("test")
class MyControllerTest extends RestDocsSupport {

    @MockBean
    private MyService myService;

    @Override
    protected Object initController() {
        return new MyController(myService);
    }

    @Test
    @DisplayName("내가 개최한 축제 목록 조회 API")
    void findHostedFestival() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        List<MyFestivalResponse> festivalResponses = createFestivalResponses(now, GlobalConstants.MIN_PAGE_SIZE + 1);
        MyFestivalCursor nextCursor = new MyFestivalCursor(now.plusDays(1), 2L);
        CursorBasedPage<MyFestivalResponse, MyFestivalCursor> result = new CursorBasedPage<>(festivalResponses,
                nextCursor, GlobalConstants.MIN_PAGE_SIZE);

        given(myService.findHostedFestival(any(), any(), anyInt())).willReturn(result);

        mockMvc.perform(get("/api/v1/member/festivals")
                        .param("time", DateTimeUtils.normalizeDateTime(now.plusDays(11)).toString())
                        .param("id", "12")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].festivalId").value(11))
                .andDo(restDocs.document(
                        queryParameters(
                                parameterWithName("time").description("다음 페이지 조회를 위한 시간 커서").optional(),
                                parameterWithName("id").description("다음 페이지 조회를 위한 ID 커서").optional(),
                                parameterWithName("pageSize").description("다음 페이지 사이즈").optional()
                        ),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("content").type(JsonFieldType.ARRAY).description("축제 목록"),
                                fieldWithPath("content[].festivalId").type(JsonFieldType.NUMBER)
                                        .description("축제 ID"),
                                fieldWithPath("content[].title").type(JsonFieldType.STRING).description("축제 제목"),
                                fieldWithPath("content[].festivalImg").type(JsonFieldType.STRING).description("축제 이미지"),
                                fieldWithPath("content[].startTime").type(JsonFieldType.STRING).description("축제 시작 시각"),
                                fieldWithPath("cursor").type(JsonFieldType.OBJECT).description("다음 페이지 커서 정보"),
                                fieldWithPath("cursor.startTime").type(JsonFieldType.STRING)
                                        .description("다음 페이지의 시작 시각 커서"),
                                fieldWithPath("cursor.id").type(JsonFieldType.NUMBER).description("다음 페이지의 ID 커서"),
                                fieldWithPath("hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 존재 여부")
                        )
                ));
    }

    @Test
    @DisplayName("내가 구매한 티켓 단건 조회 API")
    void findMyPurchasedTicket() throws Exception {
        // Given
        MyPurchasedTicketResponse response = createMockPurchasedTicketResponse();

        given(myService.findMyPurchasedTicket(any(), any())).willReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/member/tickets/{ticketId}", 1))
                .andExpect(status().isOk())
                .andDo(restDocs.document(
                        pathParameters(
                                parameterWithName("ticketId").description("조회할 티켓 ID")
                        ),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("purchaseId").type(JsonFieldType.NUMBER).description("구매 ID"),
                                fieldWithPath("purchaseTime").type(JsonFieldType.STRING).description("구매 시간"),
                                fieldWithPath("purchaseStatus").type(JsonFieldType.STRING).description("구매 상태"),
                                fieldWithPath("ticket").type(JsonFieldType.OBJECT).description("티켓 정보"),
                                fieldWithPath("ticket.id").type(JsonFieldType.NUMBER).description("티켓 ID"),
                                fieldWithPath("ticket.name").type(JsonFieldType.STRING).description("티켓 이름"),
                                fieldWithPath("ticket.detail").type(JsonFieldType.STRING).description("티켓 상세 정보"),
                                fieldWithPath("ticket.price").type(JsonFieldType.NUMBER).description("티켓 가격"),
                                fieldWithPath("ticket.quantity").type(JsonFieldType.NUMBER).description("티켓 총 수량"),
                                fieldWithPath("ticket.startSaleTime").type(JsonFieldType.STRING)
                                        .description("판매 시작 시간"),
                                fieldWithPath("ticket.endSaleTime").type(JsonFieldType.STRING).description("판매 종료 시간"),
                                fieldWithPath("ticket.refundEndTime").type(JsonFieldType.STRING)
                                        .description("환불 가능 종료 시간"),
                                fieldWithPath("ticket.createdAt").type(JsonFieldType.STRING).description("생성 시간"),
                                fieldWithPath("ticket.updatedAt").type(JsonFieldType.STRING).description("수정 시간"),
                                fieldWithPath("festival").type(JsonFieldType.OBJECT).description("축제 정보"),
                                fieldWithPath("festival.festivalId").type(JsonFieldType.NUMBER).description("축제 ID"),
                                fieldWithPath("festival.adminId").type(JsonFieldType.NUMBER).description("관리자 ID"),
                                fieldWithPath("festival.title").type(JsonFieldType.STRING).description("축제 제목"),
                                fieldWithPath("festival.description").type(JsonFieldType.STRING).description("축제 설명"),
                                fieldWithPath("festival.festivalImg").type(JsonFieldType.STRING).description("축제 이미지"),
                                fieldWithPath("festival.startTime").type(JsonFieldType.STRING).description("축제 시작 시간"),
                                fieldWithPath("festival.endTime").type(JsonFieldType.STRING).description("축제 종료 시간"),
                                fieldWithPath("festival.festivalPublicationStatus").type(JsonFieldType.STRING)
                                        .description("축제 공개 상태"),
                                fieldWithPath("festival.festivalProgressStatus").type(JsonFieldType.STRING)
                                        .description("축제 진행 상태"),
                                fieldWithPath("checkinId").type(JsonFieldType.NUMBER).description("체크인 id"),
                                fieldWithPath("isCheckedIn").type(JsonFieldType.BOOLEAN).description("체크인 여부"),
                                fieldWithPath("checkinTime").type(JsonFieldType.STRING).description("체크인 시간")
                        )
                ));
    }

    @Test
    @DisplayName("내가 구매한 축제 목록 조회 API")
    void findMyPurchasedFestivals() throws Exception {
        // Given
        LocalDateTime now = LocalDateTime.now();
        List<MyPurchasedFestivalResponse> festivalResponses = createMockPurchasedFestivalResponses(now,
                GlobalConstants.MIN_PAGE_SIZE + 1);
        MyFestivalCursor nextCursor = new MyFestivalCursor(now.plusDays(1), 2L);
        CursorBasedPage<MyPurchasedFestivalResponse, MyFestivalCursor> result = new CursorBasedPage<>(festivalResponses,
                nextCursor, GlobalConstants.MIN_PAGE_SIZE);

        given(myService.findMyPurchasedFestivals(any(), any(), anyInt())).willReturn(result);

        // When & Then
        mockMvc.perform(get("/api/v1/member/tickets")
                        .param("time", DateTimeUtils.normalizeDateTime(now.plusDays(11)).toString())
                        .param("id", "12")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].title").value("축제11"))
                .andDo(restDocs.document(
                        queryParameters(
                                parameterWithName("time").description("다음 페이지 조회를 위한 시간 커서").optional(),
                                parameterWithName("id").description("다음 페이지 조회를 위한 ID 커서").optional(),
                                parameterWithName("pageSize").description("페이지 사이즈").optional()
                        ),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("content").type(JsonFieldType.ARRAY).description("구매한 축제 목록"),
                                fieldWithPath("content[].title").type(JsonFieldType.STRING).description("축제 제목"),
                                fieldWithPath("content[].festivalImg").type(JsonFieldType.STRING).description("축제 이미지"),
                                fieldWithPath("content[].startTime").type(JsonFieldType.STRING).description("축제 시작 시각"),
                                fieldWithPath("content[].endTime").type(JsonFieldType.STRING).description("축제 종료 시각"),
                                fieldWithPath("content[].festivalPublicationStatus").type(JsonFieldType.STRING)
                                        .description("축제 공개 상태"),
                                fieldWithPath("content[].festivalProgressStatus").type(JsonFieldType.STRING)
                                        .description("축제 진행 상태"),
                                fieldWithPath("content[].admin").type(JsonFieldType.OBJECT).description("축제 관리자 정보"),
                                fieldWithPath("content[].admin.adminId").type(JsonFieldType.NUMBER)
                                        .description("관리자 ID"),
                                fieldWithPath("content[].admin.name").type(JsonFieldType.STRING).description("관리자 이름"),
                                fieldWithPath("content[].admin.email").type(JsonFieldType.STRING)
                                        .description("관리자 이메일"),
                                fieldWithPath("content[].admin.profileImg").type(JsonFieldType.STRING)
                                        .description("관리자 프로필 이미지"),
                                fieldWithPath("content[].purchaseId").type(JsonFieldType.NUMBER).description("구매 ID"),
                                fieldWithPath("content[].purchaseTime").type(JsonFieldType.STRING).description("구매 시간"),
                                fieldWithPath("content[].ticketId").type(JsonFieldType.NUMBER).description("티켓 ID"),
                                fieldWithPath("cursor").type(JsonFieldType.OBJECT).description("다음 페이지 커서 정보"),
                                fieldWithPath("cursor.startTime").type(JsonFieldType.STRING)
                                        .description("다음 페이지의 시작 시각 커서"),
                                fieldWithPath("cursor.id").type(JsonFieldType.NUMBER).description("다음 페이지의 ID 커서"),
                                fieldWithPath("hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 존재 여부")
                        )
                ));
    }

    private List<MyFestivalResponse> createFestivalResponses(LocalDateTime now, int count) {
        return IntStream.rangeClosed(1, count)
                .mapToObj(i -> new MyFestivalResponse(
                        (long) (count + 1 - i),
                        "축제" + (count + 1 - i),
                        "image" + (count + 1 - i),
                        now.plusDays(count + 1 - i)
                ))
                .collect(Collectors.toList());
    }

    private MyPurchasedTicketResponse createMockPurchasedTicketResponse() {
        return new MyPurchasedTicketResponse(
                1L, // purchaseId
                LocalDateTime.now(), // purchaseTime
                PurchaseStatus.PURCHASED, // purchaseStatus
                new TicketWithoutStockResponse(
                        100L, "VIP 티켓", "VIP 좌석", 50000L, 1,
                        LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(30),
                        LocalDateTime.now().plusDays(29), LocalDateTime.now(), LocalDateTime.now()
                ),
                new FestivalResponse(
                        1000L, 1L, "록 페스티벌", "최고의 록 밴드들과 함께하는 축제",
                        "festival_image.jpg", LocalDateTime.now().plusDays(60),
                        LocalDateTime.now().plusDays(62), FestivalPublicationStatus.PUBLISHED,
                        FestivalProgressStatus.UPCOMING
                ), 1L,
                true,
                LocalDateTime.now()
        );
    }

    private List<MyPurchasedFestivalResponse> createMockPurchasedFestivalResponses(LocalDateTime now, int count) {
        return IntStream.rangeClosed(1, count)
                .mapToObj(i -> new MyPurchasedFestivalResponse(
                        "축제" + (count + 1 - i),
                        "image" + (count + 1 - i),
                        now.plusDays(count + 1 - i),
                        now.plusDays(count + 2 - i),
                        FestivalPublicationStatus.PUBLISHED,
                        FestivalProgressStatus.UPCOMING,
                        new FestivalAdminResponse(1L, "관리자" + i, "admin" + i + "@example.com", "profile" + i + ".jpg"),
                        (long) (count + 1 - i),
                        now.minusDays(i),
                        100L
                ))
                .collect(Collectors.toList());
    }
}