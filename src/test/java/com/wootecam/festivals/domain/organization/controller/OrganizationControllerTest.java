package com.wootecam.festivals.domain.organization.controller;

import static org.springframework.restdocs.payload.PayloadDocumentation.beneathPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wootecam.festivals.docs.utils.RestDocsSupport;
import com.wootecam.festivals.domain.organization.dto.OrganizationCreateDto;
import com.wootecam.festivals.domain.organization.service.OrganizationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
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

    @Test
    @DisplayName("Organization을 생성한다")
    void create() throws Exception {
        this.mockMvc.perform(post("/api/v1/organizations")
                        .content(objectMapper.writeValueAsString(
                                new OrganizationCreateDto("testOrganization", "test detail", "test profile")))
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
}