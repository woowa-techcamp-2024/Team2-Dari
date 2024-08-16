package com.wootecam.festivals.domain.festival.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wootecam.festivals.docs.utils.RestDocsSupport;
import com.wootecam.festivals.domain.festival.dto.Cursor;
import com.wootecam.festivals.domain.festival.dto.FestivalAdminResponse;
import com.wootecam.festivals.domain.festival.dto.FestivalCreateRequest;
import com.wootecam.festivals.domain.festival.dto.FestivalCreateResponse;
import com.wootecam.festivals.domain.festival.dto.FestivalDetailResponse;
import com.wootecam.festivals.domain.festival.dto.FestivalListResponse;
import com.wootecam.festivals.domain.festival.dto.KeySetPageResponse;
import com.wootecam.festivals.domain.festival.entity.FestivalProgressStatus;
import com.wootecam.festivals.domain.festival.entity.FestivalPublicationStatus;
import com.wootecam.festivals.domain.festival.exception.FestivalErrorCode;
import com.wootecam.festivals.domain.festival.service.FestivalService;
import com.wootecam.festivals.global.exception.type.ApiException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(FestivalController.class)
@ActiveProfiles("test")
class FestivalControllerTest extends RestDocsSupport {

    @MockBean
    private FestivalService festivalService;

    private FestivalController festivalController;

    private FestivalCreateRequest validRequestDto;

    @Override
    protected Object initController() {
        festivalController = new FestivalController(festivalService);
        return festivalController;
    }

    @BeforeEach
    void setUp() {
        validRequestDto = new FestivalCreateRequest(
                1L,
                "Summer Music Festival",
                "A vibrant music festival featuring various artists",
                LocalDateTime.now().plusDays(30),
                LocalDateTime.now().plusDays(32)
        );
    }

    @Test
    @DisplayName("축제 생성 API")
    void createFestival() throws Exception {
        // Given
        Long expectedFestivalId = 1L;
        FestivalCreateResponse responseDto = new FestivalCreateResponse(expectedFestivalId);
        given(festivalService.createFestival(any(FestivalCreateRequest.class)))
                .willReturn(responseDto);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/festivals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.festivalId").value(expectedFestivalId))
                .andDo(restDocs.document(
                        requestFields(
                                fieldWithPath("adminId").type(JsonFieldType.NUMBER)
                                        .description("주최 멤버 ID"),
                                fieldWithPath("title").type(JsonFieldType.STRING)
                                        .description("축제 제목"),
                                fieldWithPath("description").type(JsonFieldType.STRING)
                                        .description("축제 설명"),
                                fieldWithPath("startTime").type(JsonFieldType.STRING)
                                        .description("축제 시작 시간 (ISO-8601 형식)"),
                                fieldWithPath("endTime").type(JsonFieldType.STRING)
                                        .description("축제 종료 시간 (ISO-8601 형식)")
                        ),
                        responseFields(
                                fieldWithPath("data.festivalId").type(JsonFieldType.NUMBER)
                                        .description("생성된 축제 ID")
                        )
                ));
    }

    @Test
    @DisplayName("축제 생성 API - 잘못된 입력")
    void createFestival_InvalidInput() throws Exception {
        // Given
        FestivalCreateRequest invalidRequestDto = new FestivalCreateRequest(
                null,
                "",
                "Description",
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1)
        );

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/festivals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequestDto)))
                .andExpect(status().isBadRequest())
                .andDo(restDocs.document(
                        requestFields(
                                fieldWithPath("adminId").type(JsonFieldType.NULL)
                                        .description("주최 멤버 ID (필수)"),
                                fieldWithPath("title").type(JsonFieldType.STRING)
                                        .description("축제 제목 (필수, 공백 불가)"),
                                fieldWithPath("description").type(JsonFieldType.STRING)
                                        .description("축제 설명"),
                                fieldWithPath("startTime").type(JsonFieldType.STRING)
                                        .description("축제 시작 시간 (현재 시간 이후여야 함)"),
                                fieldWithPath("endTime").type(JsonFieldType.STRING)
                                        .description("축제 종료 시간 (시작 시간 이후여야 함)")
                        ),
                        responseFields(
                                fieldWithPath("errorCode").type(JsonFieldType.STRING)
                                        .description("에러 코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING)
                                        .description("에러 메시지")
                        )
                ));
    }

    @Test
    @DisplayName("축제 상세 조회 API")
    void getFestivalDetail() throws Exception {
        // Given
        Long festivalId = 1L;
        LocalDateTime now = LocalDateTime.now();
        FestivalDetailResponse responseDto = new FestivalDetailResponse(
                festivalId,
                1L,
                "Summer Music Festival",
                "A vibrant music festival featuring various artists",
                now.plusDays(30),
                now.plusDays(32),
                FestivalPublicationStatus.DRAFT
        );
        given(festivalService.getFestivalDetail(anyLong())).willReturn(responseDto);

        // When & Then
        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/v1/festivals/{festivalId}", festivalId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.festivalId").value(festivalId))
                .andExpect(jsonPath("$.data.title").value("Summer Music Festival"))
                .andExpect(jsonPath("$.data.description").value("A vibrant music festival featuring various artists"))
                .andDo(restDocs.document(
                        pathParameters(
                                parameterWithName("festivalId").description("조회할 축제의 ID")
                        ),
                        responseFields(
                                fieldWithPath("data.festivalId").type(JsonFieldType.NUMBER).description("축제 ID"),
                                fieldWithPath("data.adminId").type(JsonFieldType.NUMBER).description("주최 멤버 ID"),
                                fieldWithPath("data.title").type(JsonFieldType.STRING).description("축제 제목"),
                                fieldWithPath("data.description").type(JsonFieldType.STRING).description("축제 설명"),
                                fieldWithPath("data.startTime").type(JsonFieldType.STRING).description("축제 시작 시간"),
                                fieldWithPath("data.endTime").type(JsonFieldType.STRING).description("축제 종료 시간"),
                                fieldWithPath("data.festivalPublicationStatus").type(JsonFieldType.STRING)
                                        .description("축제 상태")
                        )
                ));
    }

    @Test
    @DisplayName("존재하지 않는 축제 ID로 상세 조회 시 400 에러 반환")
    void getFestivalDetail_NotFound() throws Exception {
        // Given
        Long nonExistentId = 9999L;
        given(festivalService.getFestivalDetail(nonExistentId))
                .willThrow(new ApiException(FestivalErrorCode.FESTIVAL_NOT_FOUND));

        // When & Then
        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/v1/festivals/{festivalId}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(FestivalErrorCode.FESTIVAL_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value(FestivalErrorCode.FESTIVAL_NOT_FOUND.getMessage()))
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
        // given
        LocalDateTime now = LocalDateTime.now();
        List<FestivalListResponse> festivalResponses = List.of(
                new FestivalListResponse(1L, "축제1", now, now.plusDays(7), FestivalPublicationStatus.PUBLISHED,
                        FestivalProgressStatus.ONGOING,
                        new FestivalAdminResponse(1L, "관리자1", "detail1@email.com", "img1")),
                new FestivalListResponse(2L, "축제2", now.plusDays(1), now.plusDays(8),
                        FestivalPublicationStatus.PUBLISHED,
                        FestivalProgressStatus.ONGOING,
                        new FestivalAdminResponse(2L, "관리자2", "detail2@emil.com", "img2"))
        );
        Cursor nextCursor = new Cursor(now.plusDays(1), 2L);
        KeySetPageResponse<FestivalListResponse> pageResponse = new KeySetPageResponse<>(festivalResponses, nextCursor,
                true);

        given(festivalService.getFestivals(any(), any(), anyInt())).willReturn(pageResponse);

        // when & then
        this.mockMvc.perform(get("/api/v1/festivals")
                        .param("cursorTime", now.toString())
                        .param("cursorId", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].festivalId").value(1))
                .andExpect(jsonPath("$.data.content[1].festivalId").value(2))
                .andExpect(jsonPath("$.data.cursor.time").value(now.plusDays(1).toString()))
                .andExpect(jsonPath("$.data.cursor.id").value(2))
                .andExpect(jsonPath("$.data.hasNext").value(true))
                .andDo(restDocs.document(
                        queryParameters(
                                parameterWithName("cursorTime").description("다음 페이지 조회를 위한 시간 커서").optional(),
                                parameterWithName("cursorId").description("다음 페이지 조회를 위한 ID 커서").optional(),
                                parameterWithName("pageSize").description("페이지 크기").optional()
                        ),
                        responseFields(
                                fieldWithPath("data.content[]").type(JsonFieldType.ARRAY).description("축제 목록"),
                                fieldWithPath("data.content[].festivalId").type(JsonFieldType.NUMBER)
                                        .description("축제 ID"),
                                fieldWithPath("data.content[].title").type(JsonFieldType.STRING).description("축제 제목"),
                                fieldWithPath("data.content[].startTime").type(JsonFieldType.STRING)
                                        .description("축제 시작 시간"),
                                fieldWithPath("data.content[].endTime").type(JsonFieldType.STRING)
                                        .description("축제 종료 시간"),
                                fieldWithPath("data.content[].festivalPublicationStatus").type(JsonFieldType.STRING)
                                        .description("축제 공개 상태"),
                                fieldWithPath("data.content[].festivalProgressStatus").type(JsonFieldType.STRING)
                                        .description("축제 상태"),
                                fieldWithPath("data.content[].admin.adminId").type(JsonFieldType.NUMBER)
                                        .description("관리자 멤버 ID"),
                                fieldWithPath("data.content[].admin.name").type(JsonFieldType.STRING)
                                        .description("관리자 멤버 이름"),
                                fieldWithPath("data.content[].admin.email").type(JsonFieldType.STRING)
                                        .description("관리자 이메일"),
                                fieldWithPath("data.content[].admin.profileImg").type(JsonFieldType.STRING)
                                        .description("관리자 프로필 이미지"),
                                fieldWithPath("data.cursor.time").type(JsonFieldType.STRING)
                                        .description("다음 페이지 시간 커서"),
                                fieldWithPath("data.cursor.id").type(JsonFieldType.NUMBER).description("다음 페이지 ID 커서"),
                                fieldWithPath("data.hasNext").type(JsonFieldType.BOOLEAN).description("다음 페이지 존재 여부")
                        )
                ));
    }

    @Test
    @DisplayName("마지막 페이지 조회 시 cursor가 null이고 hasNext가 false인 응답을 반환한다.")
    void getLastPage() throws Exception {
        // given
        LocalDateTime now = LocalDateTime.now();
        List<FestivalListResponse> festivalResponses = List.of(
                new FestivalListResponse(1L, "축제1", now, now.plusDays(7), FestivalPublicationStatus.PUBLISHED,
                        FestivalProgressStatus.ONGOING,
                        new FestivalAdminResponse(1L, "관리자1", "detail1@ed.com", "img1"))
        );
        KeySetPageResponse<FestivalListResponse> pageResponse = new KeySetPageResponse<>(festivalResponses, null,
                false);

        given(festivalService.getFestivals(any(), any(), anyInt())).willReturn(pageResponse);

        // when & then
        this.mockMvc.perform(get("/api/v1/festivals")
                        .param("cursorTime", now.toString())
                        .param("cursorId", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].festivalId").value(1))
                .andExpect(jsonPath("$.data.cursor").doesNotExist())
                .andExpect(jsonPath("$.data.hasNext").value(false));
    }

    @Test
    @DisplayName("빈 결과를 조회할 경우 빈 content와 null cursor, false hasNext를 반환한다.")
    void getEmptyResult() throws Exception {
        // given
        KeySetPageResponse<FestivalListResponse> pageResponse = new KeySetPageResponse<>(List.of(), null, false);

        given(festivalService.getFestivals(any(), any(), anyInt())).willReturn(pageResponse);

        // when & then
        this.mockMvc.perform(get("/api/v1/festivals")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content").isEmpty())
                .andExpect(jsonPath("$.data.cursor").doesNotExist())
                .andExpect(jsonPath("$.data.hasNext").value(false));
    }
}