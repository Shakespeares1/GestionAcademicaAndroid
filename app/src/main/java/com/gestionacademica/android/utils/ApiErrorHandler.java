package com.gestionacademica.android.utils;

import retrofit2.Response;

public final class ApiErrorHandler {
    private ApiErrorHandler() {}

    public static ApiError fromResponse(Response<?> response) {
        int status = response.code();
        Long retryAfter = parseRetryAfter(response.headers().get("Retry-After"));
        return new ApiError(status, messageFor(status), retryAfter);
    }

    public static ApiError fromNetworkFailure(Throwable exception) {
        Throwable cause = exception;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        String message = cause instanceof java.net.SocketTimeoutException
                ? "La solicitud tardó demasiado. Intenta nuevamente."
                : "No se pudo conectar con el servidor. Revisa tu conexión.";
        return new ApiError(0, message, null);
    }

    private static String messageFor(int status) {
        switch (status) {
            case 400: return "La solicitud contiene datos inválidos.";
            case 401: return "Credenciales inválidas o sesión vencida.";
            case 403: return "Tu rol no tiene permiso para esta operación.";
            case 404: return "No se encontró el recurso solicitado.";
            case 409: return "La operación entra en conflicto con datos existentes.";
            case 429: return "Se alcanzó el límite de solicitudes. Intenta más tarde.";
            case 503: return "El servicio no está disponible temporalmente.";
            default: return "Error del servidor (HTTP " + status + ").";
        }
    }

    private static Long parseRetryAfter(String header) {
        if (header == null) return null;
        try {
            return Math.max(0L, Long.parseLong(header.trim()));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}