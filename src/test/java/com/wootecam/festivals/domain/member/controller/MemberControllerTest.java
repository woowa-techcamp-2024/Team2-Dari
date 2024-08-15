package com.wootecam.festivals.domain.member.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wootecam.festivals.docs.utils.RestDocsSupport;
import com.wootecam.festivals.domain.member.dto.MemberCreateRequestDto;
import com.wootecam.festivals.domain.member.exception.UserErrorCode;
import com.wootecam.festivals.domain.member.service.MemberService;
import com.wootecam.festivals.global.auth.Authentication;
import com.wootecam.festivals.global.exception.type.ApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.ActiveProfiles;

@WebMvcTest(MemberController.class)
@ActiveProfiles("test")
class MemberControllerTest extends RestDocsSupport {

    @MockBean
    private MemberService memberService;

    @Autowired
    private MemberController memberController;

    @Override
    protected Object initController() {
        return memberController;
    }

    @Test
    @DisplayName("회원가입 테스트")
    void createMember() throws Exception {
        // given
        String name = "test";
        String email = "test@test.com";
        String profileImg = "test";

        // when, then
        this.mockMvc.perform(post("/api/v1/member/signup")
                        .content(objectMapper.writeValueAsString(new MemberCreateRequestDto(name, email, profileImg)))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated())
                .andDo(restDocs.document(
                        requestFields(
                                fieldWithPath("name").type(JsonFieldType.STRING).description("The name of the member")
                                        .attributes(field("constraints", "Must not be null")),
                                fieldWithPath("email").type(JsonFieldType.STRING).description("The email of the member")
                                        .attributes(field("constraints", "Must not be null")),
                                fieldWithPath("profileImg").type(JsonFieldType.STRING)
                                        .description("The profile image of the member")
                                        .attributes(field("constraints", "Must not be null"))
                        ),
                                responseFields(
                                        fieldWithPath("data").type(JsonFieldType.OBJECT).description("The response data"),
                                        fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("The id of the member")
                                )
                        )
                );
    }

    @Test
    @DisplayName("회원 탈퇴 테스트")
    void withdrawMember() throws Exception {
        // given 로그인한 유저
        MockHttpSession mockHttpSession = new MockHttpSession();
        mockHttpSession.setAttribute("authentication", new Authentication(1L, "test name", "test@example.com"));

        // when, then
        this.mockMvc.perform(delete("/api/v1/member"))
                .andExpect(status().isOk())
                .andDo(restDocs.document());
    }

    @Test
    @DisplayName("회원 가입 테스트 - 이메일이 중복되는 경우 실패")
    void createMemberWithDuplicatedUser() throws Exception {
        // given
        String name = "test";
        String email = "test@test.com";
        String profileImg = "test";
        MemberCreateRequestDto dto = new MemberCreateRequestDto(name, email, profileImg);

        doThrow(new ApiException(UserErrorCode.DUPLICATED_EMAIL))
                .when(memberService).createMember(any(MemberCreateRequestDto.class));

        // when, then
        this.mockMvc.perform(post("/api/v1/member/signup")
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value(UserErrorCode.DUPLICATED_EMAIL.getCode()))
                .andExpect(jsonPath("$.message").value(UserErrorCode.DUPLICATED_EMAIL.getMessage()))
                .andDo(restDocs.document(
                        requestFields(
                                fieldWithPath("name").type(JsonFieldType.STRING).description("The name of the member"),
                                fieldWithPath("email").type(JsonFieldType.STRING)
                                        .description("The email of the member"),
                                fieldWithPath("profileImg").type(JsonFieldType.STRING)
                                        .description("The profile image of the member")
                        ),
                        responseFields(
                                fieldWithPath("errorCode").type(JsonFieldType.STRING).description("Error code"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("Error message")
                        )
                ));
    }
}
