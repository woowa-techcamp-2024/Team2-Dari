package com.wootecam.festivals.global.utils;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class UuidProvider {

    public String getUuid() {
        return UUID.randomUUID().toString();
    }
}
