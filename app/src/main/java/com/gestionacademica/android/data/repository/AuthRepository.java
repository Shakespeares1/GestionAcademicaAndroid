package com.gestionacademica.android.data.repository;

import android.content.Context;
import com.gestionacademica.android.data.dto.ForgotPasswordRequest;
import com.gestionacademica.android.data.dto.ForgotPasswordResponse;
import com.gestionacademica.android.data.dto.LoginRequest;
import com.gestionacademica.android.data.dto.LoginResponse;
import com.gestionacademica.android.data.dto.RegistroUsuarioRequest;
import com.gestionacademica.android.data.dto.ResetPasswordRequest;
import com.gestionacademica.android.data.dto.UsuarioDto;
import com.gestionacademica.android.data.remote.ApiClient;
import com.gestionacademica.android.data.remote.AuthApiService;
import com.gestionacademica.android.data.remote.UsuarioApiService;
import com.gestionacademica.android.utils.ApiError;
import com.gestionacademica.android.utils.ApiErrorHandler;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Response;

public final class AuthRepository {
    public interface Callback<T> {
        void onSuccess(T value);
        void onError(ApiError error);
    }

    private final SessionManager sessionManager;
    private final AuthApiService authService;
    private final UsuarioApiService usuarioService;
    private final ApiError configurationError;

    public AuthRepository(Context context) {
        sessionManager = new SessionManager(context.getApplicationContext());
        AuthApiService auth = null;
        UsuarioApiService usuarios = null;
        ApiError error = null;
        try {
            ApiClient client = ApiClient.getInstance(context.getApplicationContext());
            auth = client.createService(AuthApiService.class);
            usuarios = client.createService(UsuarioApiService.class);
        } catch (IllegalStateException exception) {
            error = new ApiError(-1, exception.getMessage(), null);
        }
        authService = auth;
        usuarioService = usuarios;
        configurationError = error;
    }

    public void login(LoginRequest request, Callback<LoginResponse> callback) {
        if (!isConfigured(callback)) return;
        authService.login(request).enqueue(new retrofit2.Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (!response.isSuccessful()) {
                    callback.onError(ApiErrorHandler.fromResponse(response));
                    return;
                }
                LoginResponse body = response.body();
                if (body == null || body.accessToken == null || body.accessToken.trim().isEmpty()
                        || body.tokenType == null || !"Bearer".equalsIgnoreCase(body.tokenType.trim())
                        || body.expiresIn <= 0 || body.user == null || body.user.role == null) {
                    callback.onError(new ApiError(-1, "El servidor devolvió una sesión incompleta.", null));
                    return;
                }
                String role = body.user.role.trim().toUpperCase(Locale.ROOT);
                if (!"ADMIN".equals(role) && !"USER".equals(role)) {
                    callback.onError(new ApiError(-1, "El servidor devolvió un rol no reconocido.", null));
                    return;
                }
                try {
                    sessionManager.saveSession(body.accessToken, role, body.expiresIn);
                    callback.onSuccess(body);
                } catch (RuntimeException exception) {
                    callback.onError(new ApiError(-1,
                            "No se pudo guardar la sesión protegida en este dispositivo.", null));
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable exception) {
                if (!call.isCanceled()) callback.onError(ApiErrorHandler.fromNetworkFailure(exception));
            }
        });
    }

    public void register(RegistroUsuarioRequest request, Callback<UsuarioDto> callback) {
        if (!isConfigured(callback)) return;
        enqueue(usuarioService.registrar(request), callback);
    }

    public void requestPasswordReset(ForgotPasswordRequest request,
            Callback<ForgotPasswordResponse> callback) {
        if (!isConfigured(callback)) return;
        enqueue(authService.forgotPassword(request), callback);
    }

    public void resetPassword(ResetPasswordRequest request, Callback<Void> callback) {
        if (!isConfigured(callback)) return;
        enqueue(authService.resetPassword(request), callback);
    }

    public void logout() {
        sessionManager.clearSession();
    }

    private boolean isConfigured(Callback<?> callback) {
        if (configurationError == null) return true;
        callback.onError(configurationError);
        return false;
    }

    private <T> void enqueue(Call<T> request, Callback<T> callback) {
        request.enqueue(new retrofit2.Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                if (!response.isSuccessful()) {
                    callback.onError(ApiErrorHandler.fromResponse(response));
                    return;
                }
                callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<T> call, Throwable exception) {
                if (!call.isCanceled()) callback.onError(ApiErrorHandler.fromNetworkFailure(exception));
            }
        });
    }
}