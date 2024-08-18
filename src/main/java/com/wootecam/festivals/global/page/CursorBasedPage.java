package com.wootecam.festivals.global.page;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Cursor 기반 페이징을 위한 클래스
 * <p>
 * 페이지 사이즈보다 컨텐츠의 사이즈가 크다면 hasNext를 true로 설정하고 컨텐츠를 페이지 사이즈만큼 잘라서 저장한다.
 * <p>
 * hasNext가 false라면 cursor를 null로 설정한다.
 *
 * @param <T> content의 타입
 * @param <U> cursor의 타입
 */
public class CursorBasedPage<T, U> {

    private final List<T> content;
    private final U cursor;
    private final boolean hasNext;

    public CursorBasedPage(List<T> content, U cursor, int pageSize) {
        if (content.size() > pageSize) {
            this.content = content.subList(0, pageSize);
            this.hasNext = true;
        } else {
            this.content = content;
            this.hasNext = false;
        }

        this.cursor = (content.size() > pageSize) ? cursor : null;
    }

    public List<T> getContent() {
        return content;
    }

    public U getCursor() {
        return cursor;
    }

    @JsonProperty("hasNext")
    public boolean hasNext() {
        return hasNext;
    }
}
