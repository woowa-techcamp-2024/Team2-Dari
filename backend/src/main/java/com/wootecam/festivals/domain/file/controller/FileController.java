package com.wootecam.festivals.domain.file.controller;

import com.wootecam.festivals.domain.file.dto.FileResponseDto;
import com.wootecam.festivals.domain.file.service.FilePrefixType;
import com.wootecam.festivals.domain.file.service.FileService;
import com.wootecam.festivals.global.api.ApiResponse;
import com.wootecam.festivals.global.auth.AuthUser;
import com.wootecam.festivals.global.auth.Authentication;
import java.beans.PropertyEditorSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/files")
@Slf4j
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(FilePrefixType.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                setValue(FilePrefixType.valueOf(text.toUpperCase()));
            }
        });
    }

    @GetMapping("/upload/{prefix}/{fileName}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<FileResponseDto> createPresignedUrl(@PathVariable FilePrefixType prefix, @PathVariable String fileName) {
        FileResponseDto presignedUrl = fileService.createPresignedUrl(prefix, fileName);
        log.debug("presignedUrl: {}", presignedUrl);

        return ApiResponse.of(presignedUrl);
    }
}
