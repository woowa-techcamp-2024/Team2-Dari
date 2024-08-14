package com.wootecam.festivals.domain.festival.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wootecam.festivals.docs.utils.RestDocsSupport;
import com.wootecam.festivals.domain.festival.dto.FestivalCreateRequest;
import com.wootecam.festivals.domain.festival.dto.FestivalCreateResponse;
import com.wootecam.festivals.domain.festival.dto.FestivalDetailResponse;
import com.wootecam.festivals.domain.festival.entity.FestivalStatus;
import com.wootecam.festivals.domain.festival.exception.FestivalErrorCode;
import com.wootecam.festivals.domain.festival.service.FestivalService;
import com.wootecam.festivals.global.exception.type.ApiException;
import java.time.LocalDateTime;
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
                                fieldWithPath("organizationId").type(JsonFieldType.NUMBER)
                                        .description("주최 단체 ID"),
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
                                fieldWithPath("organizationId").type(JsonFieldType.NULL)
                                        .description("주최 단체 ID (필수)"),
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
                FestivalStatus.DRAFT
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
                                fieldWithPath("data.organizationId").type(JsonFieldType.NUMBER).description("주최 단체 ID"),
                                fieldWithPath("data.title").type(JsonFieldType.STRING).description("축제 제목"),
                                fieldWithPath("data.description").type(JsonFieldType.STRING).description("축제 설명"),
                                fieldWithPath("data.startTime").type(JsonFieldType.ARRAY).description("축제 시작 시간"),
                                fieldWithPath("data.endTime").type(JsonFieldType.ARRAY).description("축제 종료 시간"),
                                fieldWithPath("data.festivalStatus").type(JsonFieldType.STRING).description("축제 상태")
                        )
                ));
    }

    @Test
    @DisplayName("존재하지 않는 축제 ID로 상세 조회 시 400 에러 반환")
    void getFestivalDetail_NotFound() throws Exception {
        // Given
        Long nonExistentId = 9999L;
        given(festivalService.getFestivalDetail(nonExistentId))
                .willThrow(new ApiException(FestivalErrorCode.FestivalNotFoundException));

        // When & Then
        mockMvc.perform(RestDocumentationRequestBuilders.get("/api/v1/festivals/{festivalId}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(FestivalErrorCode.FestivalNotFoundException.getCode()))
                .andExpect(jsonPath("$.message").value(FestivalErrorCode.FestivalNotFoundException.getMessage()))
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
}