package com.gestionacademica.android.data.repository;

import android.content.Context;
import com.gestionacademica.android.data.dto.ActualizarAccesoUsuarioRequest;
import com.gestionacademica.android.data.dto.ActualizarUsuarioRequest;
import com.gestionacademica.android.data.dto.RegistroUsuarioRequest;
import com.gestionacademica.android.data.dto.UsuarioDto;
import com.gestionacademica.android.data.remote.ApiClient;
import com.gestionacademica.android.data.remote.UsuarioApiService;
import com.gestionacademica.android.utils.ApiError;
import com.gestionacademica.android.utils.ApiErrorHandler;
import java.util.List;
import retrofit2.Call;
import retrofit2.Response;

public final class UsuarioRepository {
    public interface Callback<T> {
        void onSuccess(T value);
        void onError(ApiError error);
    }

    private final UsuarioApiService service;
    private final ApiError configurationError;

    public UsuarioRepository(Context context) {
        UsuarioApiService api = null;
        ApiError error = null;
        try {
            api = ApiClient.getInstance(context.getApplicationContext())
                    .createService(UsuarioApiService.class);
        } catch (IllegalStateException exception) {
            error = new ApiError(-1, exception.getMessage(), null);
        }
        service = api;
        configurationError = error;
    }

    public void listar(Callback<List<UsuarioDto>> callback) {
        if (isUnavailable(callback)) return;
        execute(service.listar(), callback);
    }

    public void obtener(long id, Callback<UsuarioDto> callback) {
        if (isUnavailable(callback)) return;
        execute(service.obtener(id), callback);
    }

    public void registrar(RegistroUsuarioRequest request, Callback<UsuarioDto> callback) {
        if (isUnavailable(callback)) return;
        execute(service.registrar(request), callback);
    }

    public void actualizar(long id, ActualizarUsuarioRequest request,
            Callback<UsuarioDto> callback) {
        if (isUnavailable(callback)) return;
        execute(service.actualizar(id, request), callback);
    }

    public void actualizarAcceso(long id, ActualizarAccesoUsuarioRequest request,
            Callback<UsuarioDto> callback) {
        if (isUnavailable(callback)) return;
        execute(service.actualizarAcceso(id, request), callback);
    }

    public void eliminar(long id, Callback<Void> callback) {
        if (isUnavailable(callback)) return;
        execute(service.eliminar(id), callback);
    }

    private boolean isUnavailable(Callback<?> callback) {
        if (configurationError == null) return false;
        callback.onError(configurationError);
        return true;
    }

    private <T> void execute(Call<T> request, Callback<T> callback) {
        request.enqueue(new retrofit2.Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                if (!response.isSuccessful()) {
                    callback.onError(ApiErrorHandler.fromResponse(response));
                } else {
                    callback.onSuccess(response.body());
                }
            }

            @Override
            public void onFailure(Call<T> call, Throwable error) {
                if (!call.isCanceled()) {
                    callback.onError(ApiErrorHandler.fromNetworkFailure(error));
                }
            }
        });
    }
}