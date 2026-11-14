package com.gestionacademica.android.data.remote;

import android.content.Context;
import com.gestionacademica.android.BuildConfig;
import com.gestionacademica.android.data.repository.SessionManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {
    private static volatile ApiClient instance;
    private final Retrofit retrofit;

    private ApiClient(Context context) {
        String baseUrl = BuildConfig.API_BASE_URL;
        validateBaseUrl(baseUrl);
        SessionManager sessionManager = new SessionManager(context.getApplicationContext());
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(new BearerTokenInterceptor(sessionManager))
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        Gson gson = new GsonBuilder().create();
        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    public static ApiClient getInstance(Context context) {
        ApiClient local = instance;
        if (local == null) {
            synchronized (ApiClient.class) {
                local = instance;
                if (local == null) instance = local = new ApiClient(context);
            }
        }
        return local;
    }

    public <T> T createService(Class<T> serviceType) {
        return retrofit.create(serviceType);
    }

    private static void validateBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.trim().isEmpty() || !baseUrl.endsWith("/")) {
            throw new IllegalStateException("Configura GA_API_BASE_URL como una URL HTTPS terminada en /.");
        }
        try {
            URI uri = new URI(baseUrl);
            if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                    || uri.getUserInfo() != null || uri.getQuery() != null || uri.getFragment() != null) {
                throw new IllegalStateException("La API debe usar una URL HTTPS válida sin credenciales.");
            }
        } catch (URISyntaxException exception) {
            throw new IllegalStateException("GA_API_BASE_URL no es una URL válida.", exception);
        }
    }
}