package com.gestionacademica.android.utils;

import retrofit2.Response;

public final class ApiErrorHandler {
    private ApiErrorHandler() {}

    public static ApiError fromResponse(Response<?> response) {
        return errorForResponse(response, false);
    }

    public static ApiError fromResponse(Response<?> response, String path) {
        boolean forgot = "/api/auth/forgot-password".equals(path);
        boolean reset = "/api/auth/reset-password".equals(path);
        if (forgot || reset) {
            String message;
            if (response.code() == 401) {
                message = reset
                        ? "No se pudo restablecer la contraseña. Solicita un nuevo enlace de recuperación."
                        : "No se pudo solicitar la recuperación de contraseña. Intenta nuevamente.";
            } else if (response.code() == 503) {
                message = "El envío de correo no está disponible.";
            } else {
                return fromResponse(response);
            }
            return new ApiError(response.code(), message,
                    parseRetryAfter(response.headers().get("Retry-After")));
        }
        return fromResponse(response);
    }

    public static ApiError fromLoginResponse(Response<?> response) {
        return errorForResponse(response, true);
    }

    private static ApiError errorForResponse(Response<?> response, boolean loginRequest) {
        int status = response.code();
        Long retryAfter = parseRetryAfter(response.headers().get("Retry-After"));
        String message = loginRequest && status == 401
                ? "No se pudo iniciar sesión. Verifica los datos de acceso y que la cuenta esté habilitada."
                : messageFor(status);
        return new ApiError(status, message, retryAfter);
    }

    public static ApiError fromNetworkFailure(Throwable exception) {
        Throwable cause = exception;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        String message;
        if (cause instanceof com.gestionacademica.android.data.remote.LocalNetworkPermissionRequiredException
                && cause.getMessage() != null) {
            message = cause.getMessage();
        } else if (cause instanceof java.net.SocketTimeoutException) {
            message = "La solicitud tardó demasiado. Intenta nuevamente.";
        } else {
            message = "No se pudo conectar con el servidor. Revisa tu conexión.";
        }
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