package com.wootecam.festivals.domain.organization.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.payload.PayloadDocumentation.beneathPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wootecam.festivals.docs.utils.RestDocsSupport;
import com.wootecam.festivals.domain.organization.dto.OrganizationCreateRequest;
import com.wootecam.festivals.domain.organization.dto.OrganizationIdResponse;
import com.wootecam.festivals.domain.organization.dto.OrganizationResponse;
import com.wootecam.festivals.domain.organization.exception.OrganizationErrorCode;
import com.wootecam.festivals.domain.organization.service.OrganizationService;
import com.wootecam.festivals.global.auth.Authentication;
import com.wootecam.festivals.global.exception.type.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.ActiveProfiles;

@WebMvcTest(OrganizationController.class)
@ActiveProfiles("test")
class OrganizationControllerTest extends RestDocsSupport {

    @MockBean
    private OrganizationService organizationService;

    @Autowired
    private OrganizationController organizationController;

    @Override
    protected Object initController() {
        return organizationController;
    }

    @BeforeEach
    void setUp() {
        MockHttpSession mockHttpSession = new MockHttpSession();
        Authentication auth = new Authentication(1L, "test name", "test@example.com");
        mockHttpSession.setAttribute("authentication", auth);
    }

    @Test
    @DisplayName("organization을 생성한다")
    void create() throws Exception {
        given(organizationService.createOrganization(any(), any())).willReturn(new OrganizationIdResponse(1L));
        this.mockMvc.perform(post("/api/v1/organizations")
                        .content(objectMapper.writeValueAsString(
                                new OrganizationCreateRequest("test organization", "test detail", "test profile")))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated())
                .andDo(restDocs.document(
                        requestFields(
                                fieldWithPath("name").type(JsonFieldType.STRING).description("조직 이름")
                                        .attributes(field("constraints", "1자 이상 20자 이하")),
                                fieldWithPath("detail").type(JsonFieldType.STRING).optional()
                                        .description("조직 설명").attributes(field("constraints", "200자 이하")),
                                fieldWithPath("profileImg").type(JsonFieldType.STRING).optional()
                                        .description("조직 프로필 이미지")
                        ),
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("organizationId").type(JsonFieldType.NUMBER).description("생성된 조직 ID")
                        )));
    }

    @Test
    @DisplayName("organization을 조회한다")
    void success_find() throws Exception {
        given(organizationService.findOrganization(1L))
                .willReturn(new OrganizationResponse(1L, "test organization", "test detail", "test profile"));

        this.mockMvc.perform(get("/api/v1/organizations/1"))
                .andExpect(status().isOk())
                .andDo(restDocs.document(
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("organizationId").type(JsonFieldType.NUMBER).description("조회한 조직 ID"),
                                fieldWithPath("name").type(JsonFieldType.STRING).description("조회한 조직 이름"),
                                fieldWithPath("detail").type(JsonFieldType.STRING).description("조회한 조직 설명"),
                                fieldWithPath("profileImg").type(JsonFieldType.STRING).description("조회한 조직 프로필 이미지")
                        )));
    }

    @Test
    @DisplayName("존재하지 않는 organization을 조회하면 404 에러를 반환한다")
    void fail_find() throws Exception {
        given(organizationService.findOrganization(1L))
                .willThrow(new ApiException(OrganizationErrorCode.ORGANIZATION_NOT_FOUND));

        this.mockMvc.perform(get("/api/v1/organizations/1"))
                .andExpect(status().isNotFound())
                .andDo(restDocs.document(
                        responseFields(
                                fieldWithPath("errorCode").type(JsonFieldType.STRING).description("에러 코드"),
                                fieldWithPath("message").type(JsonFieldType.STRING).description("에러 설명 메시지")
                        )));
    }
}