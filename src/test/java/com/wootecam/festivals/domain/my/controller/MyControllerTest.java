package com.wootecam.festivals.domain.my.controller;

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
import com.wootecam.festivals.domain.my.dto.MyFestivalCursor;
import com.wootecam.festivals.domain.my.dto.MyFestivalResponse;
import com.wootecam.festivals.domain.my.service.MyService;
import com.wootecam.festivals.global.page.CursorBasedPage;
import com.wootecam.festivals.global.utils.DateTimeUtils;
import java.time.LocalDateTime;
import java.util.List;
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
        List<MyFestivalResponse> festivalResponses = List.of(
                new MyFestivalResponse(11L, "축제11", "image11", now.plusDays(11)),
                new MyFestivalResponse(10L, "축제10", "image10", now.plusDays(10)),
                new MyFestivalResponse(9L, "축제9", "image9", now.plusDays(9)),
                new MyFestivalResponse(8L, "축제8", "image8", now.plusDays(8)),
                new MyFestivalResponse(7L, "축제7", "image7", now.plusDays(7)),
                new MyFestivalResponse(6L, "축제6", "image6", now.plusDays(6)),
                new MyFestivalResponse(5L, "축제5", "image5", now.plusDays(5)),
                new MyFestivalResponse(4L, "축제4", "image4", now.plusDays(4)),
                new MyFestivalResponse(3L, "축제3", "image3", now.plusDays(2)),
                new MyFestivalResponse(2L, "축제2", "image2", now.plusDays(1)),
                new MyFestivalResponse(1L, "축제1", "image1", now)
        );
        MyFestivalCursor nextCursor = new MyFestivalCursor(now.plusDays(1), 2L);
        CursorBasedPage<MyFestivalResponse, MyFestivalCursor> result = new CursorBasedPage<>(festivalResponses,
                nextCursor, 10);

        given(myService.findHostedFestival(any(), any())).willReturn(result);

        mockMvc.perform(get("/api/v1/member/festivals")
                        .param("time", DateTimeUtils.normalizeDateTime(now.plusDays(11)).toString())
                        .param("id", "12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].festivalId").value(11))
                .andDo(restDocs.document(
                        queryParameters(
                                parameterWithName("time").description("다음 페이지 조회를 위한 시간 커서").optional(),
                                parameterWithName("id").description("다음 페이지 조회를 위한 ID 커서").optional()
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
}