package com.gestionacademica.android.presentation.asignaturas;

import com.gestionacademica.android.utils.ApiError;

public final class AsignaturaUiState<T> {
    public enum Status { IDLE, LOADING, SUCCESS, ERROR }

    private final Status status;
    private final T value;
    private final ApiError error;

    private AsignaturaUiState(Status status, T value, ApiError error) {
        this.status = status;
        this.value = value;
        this.error = error;
    }

    public static <T> AsignaturaUiState<T> idle() {
        return new AsignaturaUiState<>(Status.IDLE, null, null);
    }

    public static <T> AsignaturaUiState<T> loading() {
        return new AsignaturaUiState<>(Status.LOADING, null, null);
    }

    public static <T> AsignaturaUiState<T> success(T value) {
        return new AsignaturaUiState<>(Status.SUCCESS, value, null);
    }

    public static <T> AsignaturaUiState<T> error(ApiError error) {
        return new AsignaturaUiState<>(Status.ERROR, null, error);
    }

    public Status getStatus() { return status; }
    public T getValue() { return value; }
    public ApiError getError() { return error; }
}