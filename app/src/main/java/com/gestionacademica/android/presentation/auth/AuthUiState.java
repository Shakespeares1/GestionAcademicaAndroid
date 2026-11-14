package com.gestionacademica.android.presentation.auth;

import com.gestionacademica.android.utils.ApiError;

public final class AuthUiState<T> {
    public enum Status { IDLE, LOADING, SUCCESS, ERROR }

    private final Status status;
    private final T value;
    private final ApiError error;

    private AuthUiState(Status status, T value, ApiError error) {
        this.status = status;
        this.value = value;
        this.error = error;
    }

    public static <T> AuthUiState<T> idle() {
        return new AuthUiState<>(Status.IDLE, null, null);
    }

    public static <T> AuthUiState<T> loading() {
        return new AuthUiState<>(Status.LOADING, null, null);
    }

    public static <T> AuthUiState<T> success(T value) {
        return new AuthUiState<>(Status.SUCCESS, value, null);
    }

    public static <T> AuthUiState<T> error(ApiError error) {
        return new AuthUiState<>(Status.ERROR, null, error);
    }

    public Status getStatus() { return status; }
    public T getValue() { return value; }
    public ApiError getError() { return error; }
}