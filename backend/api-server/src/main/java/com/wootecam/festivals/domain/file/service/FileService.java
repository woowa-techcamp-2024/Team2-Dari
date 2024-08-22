package com.wootecam.festivals.domain.file.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.Headers;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.wootecam.festivals.domain.file.dto.FileResponseDto;
import com.wootecam.festivals.global.config.CloudConfiguration;
import com.wootecam.festivals.global.utils.UuidProvider;
import java.net.URL;
import java.util.Date;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FileService {

    private final CloudConfiguration cloudConfiguration;

    private final AmazonS3 amazonS3;

    private final UuidProvider uuidProvider;

    public FileResponseDto createPresignedUrl(FilePrefixType prefix, String fileName) {
        fileName = createPath(prefix, fileName);

        GeneratePresignedUrlRequest generatePresignedUrlRequest = getGeneratePresignedUrlRequest(cloudConfiguration.getS3().getBucket(), fileName);
        URL url = amazonS3.generatePresignedUrl(generatePresignedUrlRequest);

        return new FileResponseDto(url.toString());
    }

    private GeneratePresignedUrlRequest getGeneratePresignedUrlRequest(String bucket, String fileName) {
        GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucket, fileName)
                .withMethod(HttpMethod.PUT)
                .withExpiration(getPresignedUrlExpiration());

        generatePresignedUrlRequest.addRequestParameter(
                Headers.S3_CANNED_ACL,
                CannedAccessControlList.PublicRead.toString()
        );

        return generatePresignedUrlRequest;
    }

    private Date getPresignedUrlExpiration() {
        Date expiration = new Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 1000 * 60 * 2;
        expiration.setTime(expTimeMillis);

        return expiration;
    }

    private String createPath(FilePrefixType prefix, String fileName) {
        String fileId = createFileId();
        return String.format("%s/%s", prefix.getDescription(), fileId + "-" + fileName);
    }

    private String createFileId() {
        return uuidProvider.getUuid();
    }
}