package com.gestionacademica.android.data.remote;

import com.gestionacademica.android.data.repository.SessionManager;
import com.gestionacademica.android.domain.Session;
import java.io.IOException;
import java.util.Optional;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public final class BearerTokenInterceptor implements Interceptor {
    private final SessionManager sessionManager;

    public BearerTokenInterceptor(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        if (isPublicRequest(original)) {
            return chain.proceed(original.newBuilder().removeHeader("Authorization").build());
        }
        Optional<Session> session = sessionManager.getSession();
        if (!session.isPresent()) return chain.proceed(original);
        String token = session.get().getAccessToken();
        if (token == null || token.trim().isEmpty()) return chain.proceed(original);
        Request authenticated = original.newBuilder()
                .header("Authorization", "Bearer " + token)
                .build();
        return chain.proceed(authenticated);
    }

    static boolean isPublicRequest(Request request) {
        String path = request.url().encodedPath();
        return "/api/auth/login".equals(path)
                || "/api/auth/forgot-password".equals(path)
                || "/api/auth/reset-password".equals(path)
                || ("POST".equals(request.method()) && "/api/usuarios".equals(path));
    }
}