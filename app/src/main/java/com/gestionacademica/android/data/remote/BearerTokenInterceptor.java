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
        Optional<Session> session = sessionManager.getSession();
        if (!session.isPresent()) return chain.proceed(original);
        String token = session.get().getAccessToken();
        if (token == null || token.trim().isEmpty()) return chain.proceed(original);
        Request authenticated = original.newBuilder()
                .header("Authorization", "Bearer " + token)
                .build();
        return chain.proceed(authenticated);
    }
}