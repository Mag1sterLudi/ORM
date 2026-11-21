package com.learning.platform.exception;

/** Нарушение правила - неверная роль, оценка выше максимума и тд и тп (HTTP 400). */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
