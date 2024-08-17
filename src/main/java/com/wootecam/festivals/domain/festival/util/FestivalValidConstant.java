package com.wootecam.festivals.domain.festival.util;

public final class FestivalValidConstant {

    public static final int MIN_TITLE_LENGTH = 1;
    public static final int MAX_TITLE_LENGTH = 100;
    public static final int MAX_DESCRIPTION_LENGTH = 2000;

    public static final String ADMIN_ID_NULL_MESSAGE = "주최 단체 정보는 필수입니다.";
    public static final String TITLE_BLANK_MESSAGE = "축제 제목은 필수입니다.";
    public static final String TITLE_SIZE_MESSAGE =
            "축제 제목은 " + MIN_TITLE_LENGTH + "자 이상 " + MAX_TITLE_LENGTH + "자 이하여야 합니다.";
    public static final String DESCRIPTION_BLANK_MESSAGE = "축제 설명은 필수입니다.";
    public static final String DESCRIPTION_SIZE_MESSAGE = "축제 설명은 " + MAX_DESCRIPTION_LENGTH + "자 이하여야 합니다.";
    public static final String START_TIME_NULL_MESSAGE = "시작 시간은 필수입니다.";
    public static final String START_TIME_FUTURE_MESSAGE = "시작 시간은 현재보다 미래여야 합니다.";
    public static final String END_TIME_NULL_MESSAGE = "종료 시간은 필수입니다.";
    public static final String END_TIME_FUTURE_MESSAGE = "종료 시간은 현재보다 미래여야 합니다.";
    public static final String END_TIME_AFTER_START_TIME_MESSAGE = "종료 시간은 시작 시간보다 늦어야 합니다.";

    private FestivalValidConstant() {
    }
}