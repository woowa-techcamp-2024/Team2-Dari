package com.wootecam.festivals.domain.member.controller;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wootecam.festivals.docs.utils.RestDocsSupport;
import com.wootecam.festivals.domain.member.dto.MemberCreateDto;
import com.wootecam.festivals.domain.member.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
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

        // when
        this.mockMvc.perform(post("/api/v1/member")
                        .content(objectMapper.writeValueAsString(new MemberCreateDto(name, email, profileImg)))
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
                        ))
                );
    }
}
