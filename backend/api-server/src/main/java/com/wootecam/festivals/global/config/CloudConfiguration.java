package com.wootecam.festivals.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "cloud")
public class CloudConfiguration {

    private Aws aws;
    private S3 s3;
    private Stack stack;

    public static class Aws {
        private String region;
        private Credentials credentials;

        public static class Credentials {
            private String accessKey;
            private String secretKey;

            // Getters and Setters
            public String getAccessKey() {
                return accessKey;
            }

            public void setAccessKey(String accessKey) {
                this.accessKey = accessKey;
            }

            public String getSecretKey() {
                return secretKey;
            }

            public void setSecretKey(String secretKey) {
                this.secretKey = secretKey;
            }
        }

        // Getters and Setters
        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        public Credentials getCredentials() {
            return credentials;
        }

        public void setCredentials(Credentials credentials) {
            this.credentials = credentials;
        }
    }

    public static class S3 {
        private String bucket;

        // Getters and Setters
        public String getBucket() {
            return bucket;
        }

        public void setBucket(String bucket) {
            this.bucket = bucket;
        }
    }

    public static class Stack {
        private boolean auto;

        // Getters and Setters
        public boolean isAuto() {
            return auto;
        }

        public void setAuto(boolean auto) {
            this.auto = auto;
        }
    }

    // Getters and Setters
    public Aws getAws() {
        return aws;
    }

    public void setAws(Aws aws) {
        this.aws = aws;
    }

    public S3 getS3() {
        return s3;
    }

    public void setS3(S3 s3) {
        this.s3 = s3;
    }

    public Stack getStack() {
        return stack;
    }

    public void setStack(Stack stack) {
        this.stack = stack;
    }
}
