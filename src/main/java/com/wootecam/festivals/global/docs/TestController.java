package com.wootecam.festivals.global.docs;

import java.time.LocalDateTime;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    @PostMapping
    public HelloResponse hello(@RequestBody HelloRequest request) {
        return new HelloResponse("Hello, World!", request.name, LocalDateTime.now().toString(), GreetStatus.SUCCESS);
    }

    public enum GreetStatus implements EnumType {
        SUCCESS("성공"),
        FAIL("실패");

        private final String description;

        GreetStatus(String description) {
            this.description = description;
        }

        @Override
        public String getName() {
            return this.name();
        }

        @Override
        public String getDescription() {
            return this.description;
        }
    }

    public record HelloResponse(String message,
                                String name,
                                String local,
                                GreetStatus enumType) {
    }

    public record HelloRequest(String message,
                               String name) {
    }
}
