package com.wootecam.festivals.domain.checkin.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wootecam.festivals.docs.utils.RestDocsSupport;
import com.wootecam.festivals.domain.checkin.exception.CheckinErrorCode;
import com.wootecam.festivals.domain.checkin.service.CheckinService;
import com.wootecam.festivals.global.exception.type.ApiException;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.PayloadDocumentation;
import org.springframework.test.context.ActiveProfiles;

@WebMvcTest(CheckinController.class)
@ActiveProfiles("test")
public class CheckinControllerTest extends RestDocsSupport {

    @MockBean
    private CheckinService checkinService;

    @Override
    protected Object initController() {
        return new CheckinController(checkinService);
    }

    static Stream<Arguments> provideException() {
        return Stream.of(
                Arguments.of(new ApiException(CheckinErrorCode.CHECKIN_NOT_FOUND)),
                Arguments.of(new ApiException(CheckinErrorCode.ALREADY_CHECKED_IN))
        );
    }

    @Test
    @DisplayName("체크인 성공 API")
    void updateCheckedIn() throws Exception {
        // given
        doNothing().when(checkinService).completeCheckin(any());

        // when & then
        this.mockMvc.perform(patch("/api/v1/festivals/{festivalId}/tickets/{ticketId}/checkins/{checkinId}", 1L, 1L, 1L))
                .andExpect(status().isOk())
                .andDo(restDocs.document(
                        PayloadDocumentation.responseFields(
                                PayloadDocumentation.fieldWithPath("data").type(JsonFieldType.NULL).description("데이터 (항상 null)")
                        )
                ));
    }

    @MethodSource("provideException")
    @ParameterizedTest
    @DisplayName("체크인 실패 API")
    void fail_updateCheckedIn(ApiException exception) throws Exception {
        // given
        doThrow(exception).when(checkinService).completeCheckin(any());

        // when & then
        this.mockMvc.perform(patch("/api/v1/festivals/{festivalId}/tickets/{ticketId}/checkins/{checkinId}", 1L, 1L, 1L))
                .andExpect(status().is(exception.getErrorCode().getHttpStatus().value()))
                .andDo(restDocs.document(
                        PayloadDocumentation.responseFields(
                                PayloadDocumentation.fieldWithPath("errorCode").type(JsonFieldType.STRING).description("에러 코드"),
                                PayloadDocumentation.fieldWithPath("message").type(JsonFieldType.STRING).description("에러 메시지")
                        )
                ));
    }
}