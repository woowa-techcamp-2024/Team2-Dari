package com.wootecam.festivals.global.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * LocalDateTime을 "yyyy-MM-dd'T'HH:mm" 형식의 문자열로 직렬화합니다.
 * <p>
 * 이 직렬화기는 주로 Festival의 startTime과 endTime 필드에 사용되며, JSON 응답에서 초와 나노초를 제거하여 시와 분까지만 표시합니다.
 * <p>
 * 리스폰스로 나가는 곳에 @JsonSerialize(using = CustomLocalDateTimeSerializer.class)을 설정하면 동작합니다.
 */
public class CustomLocalDateTimeSerializer extends StdSerializer<LocalDateTime> {

    /**
     * 날짜와 시간을 원하는 형식으로 포맷팅하기 위한 DateTimeFormatter입니다. 이 포맷터는 "yyyy-MM-dd'T'HH:mm" 패턴을 사용하여 초와 나노초를 제외합니다.
     */
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    /**
     * 기본 생성자입니다. Jackson이 리플렉션을 통해 이 직렬화기의 인스턴스를 생성할 때 사용됩니다.
     */
    public CustomLocalDateTimeSerializer() {
        this(null);
    }

    /**
     * 매개변수가 있는 생성자입니다. 상위 클래스 StdSerializer의 생성자를 호출하기 위해 필요합니다.
     *
     * @param t 직렬화할 클래스 타입
     */
    public CustomLocalDateTimeSerializer(Class<LocalDateTime> t) {
        super(t);
    }

    /**
     * LocalDateTime 객체를 JSON 문자열로 직렬화합니다. 이 메서드는 Jackson이 LocalDateTime 필드를 직렬화할 때 호출됩니다.
     *
     * @param value    직렬화할 LocalDateTime 객체
     * @param gen      JSON 생성기
     * @param provider 직렬화 제공자
     * @throws IOException JSON 생성 중 발생할 수 있는 I/O 예외
     */
    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(value.format(FORMATTER));
    }
}