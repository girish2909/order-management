package com.example.ordermanagement.exception;

public class DuplicateOrderNumberException extends RuntimeException {
    public DuplicateOrderNumberException(String message) {
        super(message);
    }
}