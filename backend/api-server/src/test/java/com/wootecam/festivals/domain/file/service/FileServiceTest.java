package com.wootecam.festivals.domain.file.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.Headers;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.wootecam.festivals.domain.file.dto.FileResponseDto;
import com.wootecam.festivals.global.config.CloudConfiguration;
import com.wootecam.festivals.global.config.CloudConfiguration.S3;
import com.wootecam.festivals.global.utils.UuidProvider;
import java.net.MalformedURLException;
import java.net.URL;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private CloudConfiguration cloudConfiguration;

    @Mock
    private AmazonS3 amazonS3;

    @Mock
    private UuidProvider uuidProvider;

    @InjectMocks
    private FileService fileService;

    @Nested
    @DisplayName("createPath 메소드는")
    class CreatePath {

        @Nested
        @DisplayName("prefix와 파일 이름이 주어지면")
        class Context_with_prefix_and_fileName {

            FilePrefixType prefix = FilePrefixType.FESTIVAL;
            String fileName = "testFile.txt";

            @Test
            @DisplayName("S3 presigned URL을 생성하여 반환한다.")
            void It_Return_Presigned_URL_Path() throws MalformedURLException {
                // Given
                String expectedFilePath = String.format("%s/%s-%s", prefix.getDescription(), "generated-uuid",
                        fileName);
                URL expectedUrl = new URL("https://test-bucket.s3.amazonaws.com/" + expectedFilePath);
                when(amazonS3.generatePresignedUrl(any())).thenReturn(expectedUrl);

                S3 s3 = new S3();
                s3.setBucket("test-bucket");
                when(cloudConfiguration.getS3()).thenReturn(s3);
                when(uuidProvider.getUuid()).thenReturn("generated-uuid");

                // When
                FileResponseDto responseDto = fileService.createPresignedUrl(prefix, fileName);

                // Then
                assertEquals(expectedUrl.toString(), responseDto.path());

                ArgumentCaptor<GeneratePresignedUrlRequest> captor = ArgumentCaptor.forClass(
                        GeneratePresignedUrlRequest.class);
                verify(amazonS3).generatePresignedUrl(captor.capture());

                GeneratePresignedUrlRequest request = captor.getValue();
                assertAll(() -> assertEquals("test-bucket", request.getBucketName()),
                        () -> assertEquals(expectedFilePath, request.getKey()),
                        () -> assertEquals(HttpMethod.PUT, request.getMethod()),
                        () -> assertEquals(CannedAccessControlList.PublicRead.toString(),
                                request.getRequestParameters().get(Headers.S3_CANNED_ACL)));
            }
        }
    }
}
