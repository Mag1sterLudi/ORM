package com.learning.platform.exception;

/** Сущность не найдена (HTTP 404) */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

    public static NotFoundException of(String entity, Long id) {
        return new NotFoundException(entity + " with id " + id + " not found");
    }
}
