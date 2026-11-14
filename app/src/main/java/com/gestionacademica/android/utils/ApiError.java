package com.gestionacademica.android.utils;

public final class ApiError {
    private final int statusCode;
    private final String message;
    private final Long retryAfterSeconds;

    public ApiError(int statusCode, String message, Long retryAfterSeconds) {
        this.statusCode = statusCode;
        this.message = message;
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public int getStatusCode() { return statusCode; }
    public String getMessage() { return message; }
    public Long getRetryAfterSeconds() { return retryAfterSeconds; }
    public boolean isNetworkFailure() { return statusCode == 0; }
}