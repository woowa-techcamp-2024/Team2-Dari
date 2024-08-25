package com.wootecam.festivals.domain.member.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.beneathPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wootecam.festivals.docs.utils.RestDocsSupport;
import com.wootecam.festivals.domain.member.dto.MemberCreateRequest;
import com.wootecam.festivals.domain.member.dto.MemberIdResponse;
import com.wootecam.festivals.domain.member.dto.MemberResponse;
import com.wootecam.festivals.domain.member.exception.MemberErrorCode;
import com.wootecam.festivals.domain.member.service.MemberService;
import com.wootecam.festivals.global.exception.type.ApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

    @Override
    protected Object initController() {
        return new MemberController(memberService);
    }

    @Test
    @DisplayName("회원가입 API")
    void createMember() throws Exception {
        MemberCreateRequest request = new MemberCreateRequest("test", "test@test.com", "test");
        given(memberService.createMember(any(MemberCreateRequest.class))).willReturn(new MemberIdResponse(1L));

        mockMvc.perform(post("/api/v1/member/signup")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1L))
                .andDo(restDocs.document(
                        requestFields(
                                fieldWithPath("name").type(JsonFieldType.STRING).description("회원 이름"),
                                fieldWithPath("email").type(JsonFieldType.STRING).description("회원 이메일"),
                                fieldWithPath("profileImg").type(JsonFieldType.STRING).description("프로필 이미지 URL")
                        ),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("id").type(JsonFieldType.NUMBER).description("생성된 회원 ID")
                        )
                ));
    }

    @Test
    @DisplayName("회원 탈퇴 API")
    void withdrawMember() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("authentication", new Authentication(1L));

        mockMvc.perform(delete("/api/v1/member")
                        .session(session))
                .andExpect(status().isOk())
                .andDo(restDocs.document());
    }

    @Test
    @DisplayName("회원가입 API - 이메일 중복 시 실패")
    void createMemberWithDuplicatedEmail() throws Exception {
        MemberCreateRequest request = new MemberCreateRequest("test", "test@test.com", "test");
        doThrow(new ApiException(MemberErrorCode.DUPLICATED_EMAIL))
                .when(memberService).createMember(any(MemberCreateRequest.class));

        mockMvc.perform(post("/api/v1/member/signup")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value(MemberErrorCode.DUPLICATED_EMAIL.getCode()))
                .andExpect(jsonPath("$.message").value(MemberErrorCode.DUPLICATED_EMAIL.getMessage()))
                .andDo(restDocs.document(
                        requestFields(
                                fieldWithPath("name").type(JsonFieldType.STRING).description("회원 이름"),
                                fieldWithPath("email").type(JsonFieldType.STRING).description("회원 이메일"),
                                fieldWithPath("profileImg").type(JsonFieldType.STRING).description("프로필 이미지 URL")
                        ),
                        responseFields(
                                fieldWithPath("errorCode").type(JsonFieldType.STRING).description("에러 코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("에러 메시지")
                        )
                ));
    }

    @Test
    @DisplayName("회원 정보 조회 API")
    void findMember() throws Exception {
        Long memberId = 1L;
        given(memberService.findMember(memberId))
                .willReturn(new MemberResponse(memberId, "test name", "test@example.com", "test-profile-img"));

        mockMvc.perform(get("/api/v1/member/{memberId}", memberId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(memberId))
                .andExpect(jsonPath("$.data.name").value("test name"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.profileImg").value("test-profile-img"))
                .andDo(restDocs.document(
                        pathParameters(
                                parameterWithName("memberId").description("조회할 회원의 ID")
                        ),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("id").type(JsonFieldType.NUMBER).description("회원 ID"),
                                fieldWithPath("name").type(JsonFieldType.STRING).description("회원 이름"),
                                fieldWithPath("email").type(JsonFieldType.STRING).description("회원 이메일"),
                                fieldWithPath("profileImg").type(JsonFieldType.STRING).description("프로필 이미지 URL")
                        )
                ));
    }

    @Test
    @DisplayName("회원 정보 조회 API - 존재하지 않는 회원")
    void findMemberNotFound() throws Exception {
        Long nonExistentMemberId = 999L;
        given(memberService.findMember(nonExistentMemberId))
                .willThrow(new ApiException(MemberErrorCode.USER_NOT_FOUND));

        mockMvc.perform(get("/api/v1/member/{memberId}", nonExistentMemberId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value(MemberErrorCode.USER_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value(MemberErrorCode.USER_NOT_FOUND.getMessage()))
                .andDo(restDocs.document(
                        pathParameters(
                                parameterWithName("memberId").description("조회할 회원의 ID")
                        ),
                        responseFields(
                                fieldWithPath("errorCode").type(JsonFieldType.STRING).description("에러 코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("에러 메시지")
                        )
                ));
    }
}
