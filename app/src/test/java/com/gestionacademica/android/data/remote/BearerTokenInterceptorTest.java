package com.gestionacademica.android.data.remote;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import okhttp3.Request;
import org.junit.Test;

public final class BearerTokenInterceptorTest {
    @Test
    public void loginRouteIsExcludedFromBearerAuthentication() {
        Request login = new Request.Builder()
                .url("https://example.invalid/api/auth/login")
                .build();

        assertTrue(BearerTokenInterceptor.isPublicRequest(login));
    }

    @Test
    public void passwordRecoveryAndRegistrationArePublic() {
        for (String path : new String[] {"/api/auth/forgot-password", "/api/auth/reset-password",
                "/api/usuarios"}) {
            Request request = new Request.Builder().url("https://example.invalid" + path)
                    .post(okhttp3.RequestBody.create(null, new byte[0])).build();
            assertTrue(BearerTokenInterceptor.isPublicRequest(request));
        }
    }

    @Test
    public void publicRequestsRemoveExistingAuthorizationWithoutReadingSession() throws Exception {
        for (String path : new String[] {"/api/auth/login", "/api/auth/forgot-password",
                "/api/auth/reset-password", "/api/usuarios"}) {
            Request request = new Request.Builder().url("https://example.invalid" + path)
                    .post(okhttp3.RequestBody.create(null, new byte[0]))
                    .header("Authorization", "Bearer synthetic-test-value").build();
            okhttp3.Interceptor.Chain chain = (okhttp3.Interceptor.Chain)
                    java.lang.reflect.Proxy.newProxyInstance(
                            okhttp3.Interceptor.Chain.class.getClassLoader(),
                            new Class<?>[] {okhttp3.Interceptor.Chain.class},
                            (proxy, method, args) -> {
                                if ("request".equals(method.getName())) return request;
                                if ("proceed".equals(method.getName())) {
                                    Request forwarded = (Request) args[0];
                                    org.junit.Assert.assertNull(forwarded.header("Authorization"));
                                    return new okhttp3.Response.Builder().request(forwarded)
                                            .protocol(okhttp3.Protocol.HTTP_1_1).code(200)
                                            .message("OK").body(okhttp3.ResponseBody.create(null, "")).build();
                                }
                                throw new AssertionError("Unexpected chain method: " + method.getName());
                            });
            try (okhttp3.Response ignored = new BearerTokenInterceptor(null).intercept(chain)) {
                // A null session manager also verifies public calls never read a stored session.
            }
        }
    }

    @Test
    public void protectedRoutesAreNotExcluded() {
        Request assignments = new Request.Builder()
                .url("https://example.invalid/api/asignaturas")
                .build();
        Request similarlyNamedRoute = new Request.Builder()
                .url("https://example.invalid/api/auth/login-history")
                .build();

        for (String method : new String[] {"GET", "PUT", "DELETE"}) {
            Request request = new Request.Builder().url("https://example.invalid/api/usuarios")
                    .method(method, "PUT".equals(method)
                            ? okhttp3.RequestBody.create(null, new byte[0]) : null).build();
            assertFalse(BearerTokenInterceptor.isPublicRequest(request));
        }
        assertFalse(BearerTokenInterceptor.isPublicRequest(assignments));
        assertFalse(BearerTokenInterceptor.isPublicRequest(similarlyNamedRoute));
    }
}
