package com.learning.platform.exception;

/** Конфликт с текущим состоянием данных, например дубликат (HTTP 409) */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
