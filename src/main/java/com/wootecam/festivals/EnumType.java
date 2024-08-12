package com.wootecam.festivals;

/**
 * Spring Data JPA에서 EnumType을 나타내기 위한 인터페이스
 * Enum 타입은 getName()과 getDescription()을 구현해야 한다.
 */
public interface EnumType {

    String getName();

    String getDescription();
}
