package com.gestionacademica.android.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import org.junit.Test;
import retrofit2.Response;

public final class ReportesHttpErrorHandlerTest {
    @Test
    public void badRequestProducesReadableFilterValidationMessage() {
        ApiError error = ApiErrorHandler.fromResponse(Response.error(400,
                ResponseBody.create(MediaType.parse("application/json"), "{\"error\":\"invalid filters\"}")));

        assertEquals(400, error.getStatusCode());
        assertTrue(error.getMessage().contains("datos inválidos"));
    }

    @Test
    public void forbiddenProducesReadablePermissionMessage() {
        ApiError error = ApiErrorHandler.fromResponse(Response.error(403,
                ResponseBody.create(MediaType.parse("application/json"), "{\"error\":\"forbidden\"}")));

        assertEquals(403, error.getStatusCode());
        assertTrue(error.getMessage().contains("permiso"));
    }
}
