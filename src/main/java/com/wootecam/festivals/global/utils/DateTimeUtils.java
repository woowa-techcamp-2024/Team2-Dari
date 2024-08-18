package com.wootecam.festivals.global.utils;

import java.time.LocalDateTime;

/**
 * 날짜와 시간 관련 유틸리티 메서드를 제공하는 클래스입니다. 이 클래스는 {@link LocalDateTime} 객체를 다루는 데 필요한 정적 메서드를 포함합니다.
 *
 * @author bellringstar
 * @version 1.0
 * @since 1.0
 */
public class DateTimeUtils {

    public DateTimeUtils() {

    }

    /**
     * 주어진 LocalDateTime 객체를 정규화합니다. 정규화는 초와 나노초를 0으로 설정하여 시와 분까지만 유지하는 과정입니다.
     *
     * @param dateTime 정규화할 LocalDateTime 객체
     * @return 초와 나노초가 0으로 설정된 새로운 LocalDateTime 객체
     * @throws NullPointerException dateTime이 null인 경우
     */
    public static LocalDateTime normalizeDateTime(LocalDateTime dateTime) {
        return dateTime.withSecond(0).withNano(0);
    }
}
