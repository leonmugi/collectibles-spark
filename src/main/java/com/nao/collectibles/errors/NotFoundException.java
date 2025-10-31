package com.nao.collectibles.errors;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String msg) { super(msg); }
}
