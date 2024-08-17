package com.wootecam.festivals.domain.festival.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.beneathPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wootecam.festivals.docs.utils.RestDocsSupport;
import com.wootecam.festivals.domain.festival.dto.Cursor;
import com.wootecam.festivals.domain.festival.dto.FestivalAdminResponse;
import com.wootecam.festivals.domain.festival.dto.FestivalCreateRequest;
import com.wootecam.festivals.domain.festival.dto.FestivalIdResponse;
import com.wootecam.festivals.domain.festival.dto.FestivalListResponse;
import com.wootecam.festivals.domain.festival.dto.FestivalResponse;
import com.wootecam.festivals.domain.festival.dto.KeySetPageResponse;
import com.wootecam.festivals.domain.festival.entity.FestivalProgressStatus;
import com.wootecam.festivals.domain.festival.entity.FestivalPublicationStatus;
import com.wootecam.festivals.domain.festival.exception.FestivalErrorCode;
import com.wootecam.festivals.domain.festival.service.FestivalService;
import com.wootecam.festivals.global.exception.type.ApiException;
import java.time.LocalDateTime;
import java.util.List;
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

@WebMvcTest(FestivalController.class)
@ActiveProfiles("test")
class FestivalControllerTest extends RestDocsSupport {

    @MockBean
    private FestivalService festivalService;

    static Stream<Arguments> provideInvalidFestivalRequests() {
        return Stream.of(
                Arguments.of(new FestivalCreateRequest(null, "", "Description", LocalDateTime.now().minusDays(1),
                        LocalDateTime.now().plusDays(1)))
        );
    }

    @Override
    protected Object initController() {
        return new FestivalController(festivalService);
    }

    @Test
    @DisplayName("축제 생성 API")
    void createFestival() throws Exception {
        FestivalCreateRequest validRequest = new FestivalCreateRequest(1L, "Summer Music Festival",
                "A vibrant music festival", LocalDateTime.now().plusDays(30), LocalDateTime.now().plusDays(32));
        given(festivalService.createFestival(any(FestivalCreateRequest.class))).willReturn(new FestivalIdResponse(1L));

        mockMvc.perform(post("/api/v1/festivals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.festivalId").value(1L))
                .andDo(restDocs.document(
                        requestFields(
                                fieldWithPath("adminId").type(JsonFieldType.NUMBER).description("주최 멤버 ID"),
                                fieldWithPath("title").type(JsonFieldType.STRING).description("축제 제목"),
                                fieldWithPath("description").type(JsonFieldType.STRING).description("축제 설명"),
                                fieldWithPath("startTime").type(JsonFieldType.STRING)
                                        .description("축제 시작 시간 (ISO-8601 형식)"),
                                fieldWithPath("endTime").type(JsonFieldType.STRING)
                                        .description("축제 종료 시간 (ISO-8601 형식)")
                        ),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("festivalId").type(JsonFieldType.NUMBER).description("생성된 축제 ID")
                        )
                ));
    }

    @ParameterizedTest
    @MethodSource("provideInvalidFestivalRequests")
    @DisplayName("축제 생성 API - 잘못된 입력")
    void createFestival_InvalidInput(FestivalCreateRequest invalidRequest) throws Exception {
        mockMvc.perform(post("/api/v1/festivals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andDo(restDocs.document(
                        requestFields(
                                fieldWithPath("adminId").type(JsonFieldType.NULL).description("주최 멤버 ID (필수)"),
                                fieldWithPath("title").type(JsonFieldType.STRING).description("축제 제목 (필수, 공백 불가)"),
                                fieldWithPath("description").type(JsonFieldType.STRING).description("축제 설명"),
                                fieldWithPath("startTime").type(JsonFieldType.STRING)
                                        .description("축제 시작 시간 (현재 시간 이후여야 함)"),
                                fieldWithPath("endTime").type(JsonFieldType.STRING)
                                        .description("축제 종료 시간 (시작 시간 이후여야 함)")
                        ),
                        responseFields(
                                fieldWithPath("errorCode").type(JsonFieldType.STRING).description("에러 코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("에러 메시지")
                        )
                ));
    }

    @Test
    @DisplayName("축제 상세 조회 API")
    void getFestivalDetail() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        FestivalResponse responseDto = new FestivalResponse(1L, 1L, "Summer Music Festival", "A vibrant music festival",
                "image",
                now.plusDays(30), now.plusDays(32), FestivalPublicationStatus.DRAFT);
        given(festivalService.getFestivalDetail(any())).willReturn(responseDto);

        mockMvc.perform(get("/api/v1/festivals/{festivalId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.festivalId").value(1L))
                .andDo(restDocs.document(
                        pathParameters(
                                parameterWithName("festivalId").description("조회할 축제의 ID")
                        ),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("festivalId").type(JsonFieldType.NUMBER).description("축제 ID"),
                                fieldWithPath("adminId").type(JsonFieldType.NUMBER).description("주최 멤버 ID"),
                                fieldWithPath("title").type(JsonFieldType.STRING).description("축제 제목"),
                                fieldWithPath("description").type(JsonFieldType.STRING).description("축제 설명"),
                                fieldWithPath("festivalImg").type(JsonFieldType.STRING).description("축제 이미지"),
                                fieldWithPath("startTime").type(JsonFieldType.STRING).description("축제 시작 시간"),
                                fieldWithPath("endTime").type(JsonFieldType.STRING).description("축제 종료 시간"),
                                fieldWithPath("festivalPublicationStatus").type(JsonFieldType.STRING)
                                        .description("축제 상태")
                        )
                ));
    }

    @Test
    @DisplayName("존재하지 않는 축제 ID로 상세 조회 시 400 에러 반환")
    void getFestivalDetail_NotFound() throws Exception {
        given(festivalService.getFestivalDetail(any())).willThrow(
                new ApiException(FestivalErrorCode.FESTIVAL_NOT_FOUND));

        mockMvc.perform(get("/api/v1/festivals/{festivalId}", 9999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(FestivalErrorCode.FESTIVAL_NOT_FOUND.getCode()))
                .andDo(restDocs.document(
                        pathParameters(
                                parameterWithName("festivalId").description("조회할 축제의 ID")
                        ),
                        responseFields(
                                fieldWithPath("errorCode").type(JsonFieldType.STRING).description("에러 코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("에러 메시지")
                        )
                ));
    }

    @Test
    @DisplayName("페이지네이션된 축제 목록 조회 API")
    void getFestivals() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        List<FestivalListResponse> festivalResponses = List.of(
                new FestivalListResponse(1L, "축제1", "image1", now, now.plusDays(7), FestivalPublicationStatus.PUBLISHED,
                        FestivalProgressStatus.ONGOING,
                        new FestivalAdminResponse(1L, "관리자1", "admin1@email.com", "img1")),
                new FestivalListResponse(2L, "축제2", "image2", now.plusDays(1), now.plusDays(8),
                        FestivalPublicationStatus.PUBLISHED, FestivalProgressStatus.ONGOING,
                        new FestivalAdminResponse(2L, "관리자2", "admin2@email.com", "img2"))
        );
        Cursor nextCursor = new Cursor(now.plusDays(1), 2L);
        KeySetPageResponse<FestivalListResponse> pageResponse = new KeySetPageResponse<>(festivalResponses, nextCursor,
                true);

        given(festivalService.getFestivals(any(LocalDateTime.class), any(Long.class), anyInt())).willReturn(
                pageResponse);

        mockMvc.perform(get("/api/v1/festivals")
                        .param("cursorTime", now.toString())
                        .param("cursorId", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].festivalId").value(1))
                .andDo(restDocs.document(
                        queryParameters(
                                parameterWithName("cursorTime").description("다음 페이지 조회를 위한 시간 커서").optional(),
                                parameterWithName("cursorId").description("다음 페이지 조회를 위한 ID 커서").optional(),
                                parameterWithName("pageSize").description("페이지 크기").optional()
                        ),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("content").type(JsonFieldType.ARRAY).description("축제 목록"),
                                fieldWithPath("content[].festivalId").type(JsonFieldType.NUMBER)
                                        .description("축제 ID"),
                                fieldWithPath("content[].title").type(JsonFieldType.STRING).description("축제 제목"),
                                fieldWithPath("content[].festivalImg").type(JsonFieldType.STRING).description("축제 이미지"),
                                fieldWithPath("content[].startTime").type(JsonFieldType.STRING)
                                        .description("축제 시작 시간"),
                                fieldWithPath("content[].endTime").type(JsonFieldType.STRING)
                                        .description("축제 종료 시간"),
                                fieldWithPath("content[].festivalPublicationStatus").type(JsonFieldType.STRING)
                                        .description("축제 공개 상태"),
                                fieldWithPath("content[].festivalProgressStatus").type(JsonFieldType.STRING)
                                        .description("축제 상태"),
                                fieldWithPath("content[].admin.adminId").type(JsonFieldType.NUMBER)
                                        .description("관리자 멤버 ID"),
                                fieldWithPath("content[].admin.name").type(JsonFieldType.STRING)
                                        .description("관리자 멤버 이름"),
                                fieldWithPath("content[].admin.email").type(JsonFieldType.STRING)
                                        .description("관리자 이메일"),
                                fieldWithPath("content[].admin.profileImg").type(JsonFieldType.STRING)
                                        .description("관리자 프로필 이미지"),
                                fieldWithPath("cursor.time").type(JsonFieldType.STRING)
                                        .description("다음 페이지 시간 커서"),
                                fieldWithPath("cursor.id").type(JsonFieldType.NUMBER).description("다음 페이지 ID 커서"),
                                fieldWithPath("hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 존재 여부")
                        )
                ));
    }
}