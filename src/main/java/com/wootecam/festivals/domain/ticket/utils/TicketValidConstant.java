package com.wootecam.festivals.domain.ticket.utils;

public final class TicketValidConstant {

    public static final int MIN_TICKET_NAME_LENGTH = 1;
    public static final int MAX_TICKET_NAME_LENGTH = 100;

    public static final int MAX_TICKET_DETAIL_LENGTH = 1000;

    public static final long MIN_TICKET_PRICE = 0L;
    public static final long MAX_TICKET_PRICE = 9999999999L;

    public static final int MIN_TICKET_QUANTITY = 1;
    public static final int MAX_TICKET_QUANTITY = 100000;

    public static final String TICKET_NAME_VALID_MESSAGE =
            "티켓 이름은 " + MIN_TICKET_NAME_LENGTH + "자 이상 " + MAX_TICKET_NAME_LENGTH + "자 이하로 입력해야 합니다.";

    public static final String TICKET_DETAIL_VALID_MESSAGE = "티켓 설명은 " + MAX_TICKET_DETAIL_LENGTH + "자 이하로 입력해야 합니다.";

    public static final String TICKET_PRICE_VALID_MESSAGE =
            "티켓 가격은 " + MIN_TICKET_PRICE + "원 이상 " + MAX_TICKET_PRICE + "원 이하로 입력해야 합니다.";

    public static final String TICKET_QUANTITY_VALID_MESSAGE =
            "티켓 수량은 " + MIN_TICKET_QUANTITY + "개 이상 " + MAX_TICKET_QUANTITY + "개 이하로 입력해야 합니다.";

    public static final String TICKET_TIME_VALID_MESSAGE = "티켓 판매 시작 시간은 판매 종료 시간, 환불 종료 시간보다 빨라야 합니다.";

    public static final String TICKET_START_TIME_VALID_MESSAGE = "티켓 판매 시작 시간은 현재 시간 이후이여야 합니다.";

    public static final String TICKET_END_TIME_VALID_MESSAGE = "티켓 판매 종료 시간은 현재 시간 이후 이벤트 종료 시간 이전이여야 합니다.";

    public static final String TICKET_REFUND_TIME_VALID_MESSAGE = "티켓 환불 종료 시간은 현재 시간 이후여야 합니다.";

    public static final String TICKET_FESTIVAL_VALID_MESSAGE = "페스티벌은 필수 입력값입니다.";

    public static final String TICKET_NAME_EMPTY_VALID_MESSAGE = "티켓 이름은 필수 값 입니다.";

    public static final String TICKET_PRICE_EMPTY_VALID_MESSAGE = "티켓 가격은 필수 값 입니다.";

    public static final String TICKET_QUANTITY_EMPTY_VALID_MESSAGE = "티켓 수량은 필수 값 입니다.";

    public static final String TICKET_START_TIME_EMPTY_VALID_MESSAGE = "티켓 판매 시작 시간은 필수 입력값입니다.";

    public static final String TICKET_END_TIME_EMPTY_VALID_MESSAGE = "티켓 판매 종료 시간은 필수 입력값입니다.";

    public static final String TICKET_REFUND_TIME_EMPTY_VALID_MESSAGE = "티켓 환불 종료 시간은 필수 입력값입니다.";

    private TicketValidConstant() {
    }
}
