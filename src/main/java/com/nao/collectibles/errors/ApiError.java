package com.nao.collectibles.errors;

public class ApiError {
    public final String message;
    public final String code;

    public ApiError(String message, String code) {
        this.message = message;
        this.code = code;
    }
}
