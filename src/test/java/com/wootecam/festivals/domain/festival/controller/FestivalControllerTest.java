package com.wootecam.festivals.domain.festival.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wootecam.festivals.docs.utils.RestDocsSupport;
import com.wootecam.festivals.domain.festival.dto.FestivalCreateRequestDto;
import com.wootecam.festivals.domain.festival.dto.FestivalCreateResponseDto;
import com.wootecam.festivals.domain.festival.service.FestivalService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(FestivalController.class)
@ActiveProfiles("test")
class FestivalControllerTest extends RestDocsSupport {

    @MockBean
    private FestivalService festivalService;

    private FestivalController festivalController;

    private FestivalCreateRequestDto validRequestDto;

    @Override
    protected Object initController() {
        festivalController = new FestivalController(festivalService);
        return festivalController;
    }

    @BeforeEach
    void setUp() {
        validRequestDto = new FestivalCreateRequestDto(
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
        FestivalCreateResponseDto responseDto = new FestivalCreateResponseDto(expectedFestivalId);
        given(festivalService.createFestival(any(FestivalCreateRequestDto.class)))
                .willReturn(responseDto);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/festivals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequestDto)))
                .andExpect(status().isOk())
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
        FestivalCreateRequestDto invalidRequestDto = new FestivalCreateRequestDto(
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
}