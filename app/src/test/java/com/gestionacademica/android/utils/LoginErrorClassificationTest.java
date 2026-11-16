package com.gestionacademica.android.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import org.junit.Test;
import retrofit2.Response;

public final class LoginErrorClassificationTest {
    @Test
    public void unauthorizedLoginUsesLoginSpecificMessage() {
        ApiError loginError = ApiErrorHandler.fromLoginResponse(error(401));
        ApiError protectedResourceError = ApiErrorHandler.fromResponse(error(401));

        assertEquals(401, loginError.getStatusCode());
        assertTrue(loginError.getMessage().contains("iniciar sesión"));
        assertNotEquals(loginError.getMessage(), protectedResourceError.getMessage());
    }

    @Test
    public void otherLoginStatusesRemainDistinct() {
        assertEquals(400, ApiErrorHandler.fromLoginResponse(error(400)).getStatusCode());
        assertEquals(403, ApiErrorHandler.fromLoginResponse(error(403)).getStatusCode());
        assertEquals(404, ApiErrorHandler.fromLoginResponse(error(404)).getStatusCode());
        assertEquals(429, ApiErrorHandler.fromLoginResponse(error(429)).getStatusCode());
        assertEquals(500, ApiErrorHandler.fromLoginResponse(error(500)).getStatusCode());
    }

    @Test
    public void recoveryUnauthorizedErrorsDoNotUseSessionMessage() {
        String forgot = ApiErrorHandler.fromResponse(error(401), "/api/auth/forgot-password").getMessage();
        String reset = ApiErrorHandler.fromResponse(error(401), "/api/auth/reset-password").getMessage();
        assertTrue(forgot.contains("recuperación"));
        assertTrue(reset.contains("nuevo enlace"));
        assertNotEquals(ApiErrorHandler.fromResponse(error(401)).getMessage(), forgot);
        assertNotEquals(forgot, reset);
    }

    @Test
    public void recoveryUnavailableErrorsExplainMailAvailability() {
        for (String path : new String[] {"/api/auth/forgot-password", "/api/auth/reset-password"}) {
            assertEquals("El envío de correo no está disponible.",
                    ApiErrorHandler.fromResponse(error(503), path).getMessage());
        }
        assertEquals("El servicio no está disponible temporalmente.",
                ApiErrorHandler.fromResponse(error(503), "/api/asignaturas").getMessage());
    }

    private static Response<Object> error(int status) {
        return Response.error(status, ResponseBody.create(
                MediaType.parse("application/json"), "{}"));
    }
}
