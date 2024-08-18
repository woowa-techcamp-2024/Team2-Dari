package com.wootecam.festivals.global.page;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

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
