package com.wootecam.festivals.global.auth;

import java.io.Serializable;

public record Authentication(Long memberId) implements Serializable {

    public static Authentication from(Long memberId) {
        return new Authentication(memberId);
    }
}