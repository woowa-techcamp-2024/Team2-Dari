package com.wootecam.festivals.docs;

import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wootecam.festivals.docs.utils.DocumentLinkGenerator;
import com.wootecam.festivals.docs.utils.DocumentLinkGenerator.DocUrl;
import com.wootecam.festivals.docs.utils.RestDocsSupport;
import com.wootecam.festivals.global.docs.TestController;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;

class TestControllerTest extends RestDocsSupport {

    @Override
    protected Object initController() {
        return new TestController();
    }

    @Test
    void testHello() throws Exception {
        this.mockMvc.perform(post("/api/v1/test")
                        .content(objectMapper.writeValueAsString(new TestController.HelloRequest("hello", "wootecamp")))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Hello, World!"))
                .andExpect(jsonPath("$.name").value("wootecamp"))
                .andExpect(jsonPath("$.local").exists())
                .andDo(restDocs.document(
                        requestFields(
                                fieldWithPath("name").type(JsonFieldType.STRING).description("The name of the service")
                                        .attributes(field("constraints", "Must not be null")),
                                fieldWithPath("message").type(JsonFieldType.STRING).optional()
                                        .description("The greeting message")
                        ),
                        responseFields(
                                fieldWithPath("message").type(JsonFieldType.STRING).description("The greeting message"),
                                fieldWithPath("name").type(JsonFieldType.STRING).description("The name of the service"),
                                fieldWithPath("local").type(JsonFieldType.STRING).description("The current time"),
                                fieldWithPath("enumType").description(
                                        DocumentLinkGenerator.generateLinkCode(DocUrl.GREET_STATUS))
                        )));
    }
}
