package com.wootecam.festivals.global.utils;

import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class TimeProvider {

    public LocalDateTime getCurrentTime() {
        return LocalDateTime.now();
    }
}
