package com.wootecam.festivals.domain.file.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.beneathPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wootecam.festivals.docs.utils.RestDocsSupport;
import com.wootecam.festivals.domain.file.dto.FileResponseDto;
import com.wootecam.festivals.domain.file.service.FileService;
import com.wootecam.festivals.domain.member.controller.MemberController;
import com.wootecam.festivals.domain.member.dto.MemberCreateRequest;
import com.wootecam.festivals.domain.member.dto.MemberIdResponse;
import com.wootecam.festivals.domain.member.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.ActiveProfiles;

@WebMvcTest(FileController.class)
@ActiveProfiles("test")
class FileControllerTest extends RestDocsSupport {

    @MockBean
    private FileService fileService;

    @Override
    protected Object initController() {
        return new FileController(fileService);
    }

    @Test
    @DisplayName("S3 presigned URL 요청 API")
    void createMember() throws Exception {
        String path = "https://test-bucket.s3.amazonaws.com/member/test.jpg";
        given(fileService.createPresignedUrl(any(), any())).willReturn(new FileResponseDto(path));

        mockMvc.perform(get("/api/v1/files/upload/member/test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.path").value(path))
                .andDo(restDocs.document(
                        responseFields(
                                beneathPath("data").withSubsectionId("data"),
                                fieldWithPath("path").type(JsonFieldType.STRING).description("파일을 업로드할 수 있는 S3 presigned URL")
                        )
                ));
    }
}