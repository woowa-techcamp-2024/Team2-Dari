package com.wootecam.festivals.domain.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wootecam.festivals.docs.utils.RestDocsSupport;
import com.wootecam.festivals.domain.auth.dto.LoginRequest;
import com.wootecam.festivals.domain.auth.service.AuthService;
import com.wootecam.festivals.global.auth.AuthErrorCode;
import com.wootecam.festivals.global.exception.type.ApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.ActiveProfiles;

@WebMvcTest(AuthController.class)
@ActiveProfiles("test")
class AuthControllerTest extends RestDocsSupport {

    @MockBean
    private AuthService authService;

    @Override
    protected Object initController() {
        return new AuthController(authService);
    }

    @Test
    @DisplayName("로그인 API")
    void login() throws Exception {
        LoginRequest loginRequest = new LoginRequest("test@test.com");

        mockMvc.perform(post("/api/v1/auth/login")
                        .content(objectMapper.writeValueAsString(loginRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(restDocs.document(
                        requestFields(
                                fieldWithPath("email").type(JsonFieldType.STRING).description("로그인할 이메일")
                                        .attributes(key("constraints").value("이메일 형식이어야 함"))
                        ),
                        responseFields(
                                fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터 (없음)")
                        )));
    }

    @Test
    @DisplayName("로그인 실패 API")
    void loginFailed() throws Exception {
        LoginRequest loginRequest = new LoginRequest("test@test.com");

        doThrow(new ApiException(AuthErrorCode.USER_LOGIN_FAILED))
                .when(authService).login(any());

        mockMvc.perform(post("/api/v1/auth/login")
                        .content(objectMapper.writeValueAsString(loginRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(restDocs.document(
                        requestFields(
                                fieldWithPath("email").type(JsonFieldType.STRING).description("로그인할 이메일")
                        ),
                        responseFields(
                                fieldWithPath("errorCode").type(JsonFieldType.STRING).description("에러 코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("에러 메시지")
                        )));
    }

    @Test
    @DisplayName("로그아웃 API")
    void logout() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(restDocs.document(
                        responseFields(
                                fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터 (없음)")
                        )));
    }
}