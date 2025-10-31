package com.nao.collectibles.errors;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String msg) { super(msg); }
}
