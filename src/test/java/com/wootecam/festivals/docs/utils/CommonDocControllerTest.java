package com.wootecam.festivals.docs.utils;

import static org.springframework.restdocs.payload.PayloadDocumentation.beneathPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.snippet.Attributes.attributes;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.PayloadSubsectionExtractor;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;


class CommonDocControllerTest extends RestDocsSupport {

    // 커스텀 템플릿 사용을 위한 함수
    public static CustomResponseFieldsSnippet customResponseFields
    (String type,
     PayloadSubsectionExtractor<?> subsectionExtractor,
     Map<String, Object> attributes, FieldDescriptor... descriptors) {
        return new CustomResponseFieldsSnippet(type, subsectionExtractor, Arrays.asList(descriptors), attributes
                , true);
    }

    // Map으로 넘어온 enumValue를 fieldWithPath로 변경하여 리턴
    private static FieldDescriptor[] enumConvertFieldDescriptor(Map<String, String> enumValues) {
        return enumValues.entrySet().stream()
                .map(x -> fieldWithPath(x.getKey()).description(x.getValue()))
                .toArray(FieldDescriptor[]::new);
    }

    @Test
    void enums() throws Exception {
        ResultActions result = this.mockMvc.perform(
                get("/test/enums")
                        .contentType(MediaType.APPLICATION_JSON)
        );

        MvcResult mvcResult = result.andReturn();
        EnumDocs enumDocs = getData(mvcResult);
        System.out.println(enumDocs);

        result.andExpect(status().isOk())
                .andDo(restDocs.document(
                        customResponseFields("custom-response",
                                beneathPath("greetStatus").withSubsectionId("greetStatus"),
                                attributes(key("title").value("greetStatus")),
                                enumConvertFieldDescriptor((enumDocs.getGreetStatus()))
                        )
                ));
    }

    // mvc result 데이터 파싱
    private EnumDocs getData(MvcResult result) throws IOException {
        System.out.println(result);

        return objectMapper
                .readValue(result.getResponse().getContentAsByteArray(),
                        new TypeReference<EnumDocs>() {
                        }
                );
    }

    @Override
    protected Object initController() {
        return new CommonDocController();
    }
}