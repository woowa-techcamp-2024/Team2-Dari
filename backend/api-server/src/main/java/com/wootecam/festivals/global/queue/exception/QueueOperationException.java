package com.wootecam.festivals.global.queue.exception;

public class QueueOperationException extends RuntimeException{

    public QueueOperationException(String message) {
        super(message);
    }

    public QueueOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
