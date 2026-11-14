package com.gestionacademica.android.presentation.usuarios;

import com.gestionacademica.android.utils.ApiError;

public final class UsuarioUiState<T> {
    public enum Status { IDLE, LOADING, SUCCESS, ERROR }

    private final Status status;
    private final T value;
    private final ApiError error;

    private UsuarioUiState(Status status, T value, ApiError error) {
        this.status = status;
        this.value = value;
        this.error = error;
    }

    public static <T> UsuarioUiState<T> idle() {
        return new UsuarioUiState<>(Status.IDLE, null, null);
    }

    public static <T> UsuarioUiState<T> loading() {
        return new UsuarioUiState<>(Status.LOADING, null, null);
    }

    public static <T> UsuarioUiState<T> success(T value) {
        return new UsuarioUiState<>(Status.SUCCESS, value, null);
    }

    public static <T> UsuarioUiState<T> error(ApiError error) {
        return new UsuarioUiState<>(Status.ERROR, null, error);
    }

    public Status getStatus() { return status; }
    public T getValue() { return value; }
    public ApiError getError() { return error; }
}