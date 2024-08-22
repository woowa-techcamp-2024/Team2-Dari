package com.wootecam.festivals.domain.festival.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.beneathPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wootecam.festivals.docs.utils.RestDocsSupport;
import com.wootecam.festivals.domain.festival.dto.ParticipantResponse;
import com.wootecam.festivals.domain.festival.dto.ParticipantsPaginationResponse;
import com.wootecam.festivals.domain.festival.service.FestivalParticipantService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.ActiveProfiles;

@WebMvcTest(FestivalParticipantController.class)
@ActiveProfiles("test")
class FestivalParticipantControllerTest extends RestDocsSupport {

    @MockBean
    private FestivalParticipantService festivalParticipantService;

    @Override
    protected Object initController() {
        return new FestivalParticipantController(festivalParticipantService);
    }

    @Test
    @DisplayName("참가자 목록 페이지네이션 조회 API")
    void getParticipants() throws Exception {
        // given
        List<ParticipantResponse> participantResponses = new ArrayList<>();
        for (long i = 1; i <= 10; i++) {
            ParticipantResponse participantResponse = new ParticipantResponse(i, "name" + i, "email" + i,
                    i, "ticket" + i, i, LocalDateTime.now(), i, false);

            participantResponses.add(participantResponse);
        }

        given(festivalParticipantService.getParticipantListWithPagination(any(), any(), any()))
                .willReturn(new ParticipantsPaginationResponse(participantResponses,
                        10, 10,
                        1000, 1000 / 10,
                        true, true));

        // when, then
        mockMvc.perform(get("/api/v1/festivals/{festivalId}/participants", 1L)
                        .queryParam("page", "10")
                        .queryParam("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.participants").isArray())
                .andExpect(jsonPath("$.data.participants.length()").value(10))
                .andExpect(jsonPath("$.data.currentPage").value(10))
                .andExpect(jsonPath("$.data.totalPages").value(100))
                .andExpect(jsonPath("$.data.totalItems").value(1000))
                .andExpect(jsonPath("$.data.hasNext").value(true))
                .andExpect(jsonPath("$.data.hasPrevious").value(true))
                .andDo(restDocs.document(
                        queryParameters(
                                parameterWithName("page").description("페이지 번호"),
                                parameterWithName("size").description("페이지 크기")
                        ),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("participants").type(JsonFieldType.ARRAY)
                                        .description("참가자 목록"),
                                fieldWithPath("participants[].participantId").type(JsonFieldType.NUMBER)
                                        .description("참가자 식별자"),
                                fieldWithPath("participants[].participantName").type(JsonFieldType.STRING)
                                        .description("참가자 이름"),
                                fieldWithPath("participants[].participantEmail").type(JsonFieldType.STRING)
                                        .description("참가자 이메일"),
                                fieldWithPath("participants[].ticketId").type(JsonFieldType.NUMBER)
                                        .description("티켓 식별자"),
                                fieldWithPath("participants[].ticketName").type(JsonFieldType.STRING)
                                        .description("티켓 이름"),
                                fieldWithPath("participants[].purchaseId").type(JsonFieldType.NUMBER)
                                        .description("구매 식별자"),
                                fieldWithPath("participants[].purchaseTime").type(JsonFieldType.STRING)
                                        .description("구매 시간"),
                                fieldWithPath("participants[].checkinId").type(JsonFieldType.NUMBER)
                                        .description("체크인 식별자"),
                                fieldWithPath("participants[].isCheckin").type(JsonFieldType.BOOLEAN)
                                        .description("체크인 여부"),
                                fieldWithPath("currentPage").type(JsonFieldType.NUMBER)
                                        .description("현재 페이지"),
                                fieldWithPath("itemsPerPage").type(JsonFieldType.NUMBER)
                                        .description("페이지 크기"),
                                fieldWithPath("totalPages").type(JsonFieldType.NUMBER)
                                        .description("전체 페이지 수"),
                                fieldWithPath("totalItems").type(JsonFieldType.NUMBER)
                                        .description("전체 참가자 수"),
                                fieldWithPath("hasNext").type(JsonFieldType.BOOLEAN)
                                        .description("다음 페이지 존재 여부"),
                                fieldWithPath("hasPrevious").type(JsonFieldType.BOOLEAN)
                                        .description("이전 페이지 존재 여부")
                        )
                ));
    }
}
