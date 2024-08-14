package com.wootecam.festivals.domain.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wootecam.festivals.docs.utils.RestDocsSupport;
import com.wootecam.festivals.domain.auth.exception.AuthErrorCode;
import com.wootecam.festivals.domain.auth.service.AuthService;
import com.wootecam.festivals.domain.auth.dto.LoginRequest;
import com.wootecam.festivals.global.exception.type.ApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private AuthController authController;

    @Override
    protected Object initController() {
        return authController;
    }

    @Test
    @DisplayName("로그인을 수행한다")
    void login() throws Exception {
        LoginRequest loginRequest = new LoginRequest("test@test.com");

        this.mockMvc.perform(post("/api/v1/auth/login")
                .content(objectMapper.writeValueAsString(loginRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(restDocs.document(
                        requestFields(
                                fieldWithPath("email").description("로그인할 이메일")
                        ),
                        responseFields(
                                fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터 (없음)")
                        )));
    }

    @Test
    @DisplayName("로그인에 실패한다")
    void login_failed() throws Exception {
        LoginRequest loginRequest = new LoginRequest("test@test.com");

        doThrow(new ApiException(AuthErrorCode.USER_LOGIN_FAILED))
                .when(authService).login(any());

        this.mockMvc.perform(post("/api/v1/auth/login")
                        .content(objectMapper.writeValueAsString(loginRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andDo(restDocs.document(
                        requestFields(
                                fieldWithPath("email").description("로그인할 이메일")
                        ),
                        responseFields(
                                fieldWithPath("errorCode").type(JsonFieldType.STRING).description("Error code"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("Error message")
                        )));
    }

    @Test
    @DisplayName("로그아웃을 수행한다")
    void logout() throws Exception {
        this.mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(restDocs.document(
                        responseFields(
                                fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터 (없음)")
                        )));
    }
}