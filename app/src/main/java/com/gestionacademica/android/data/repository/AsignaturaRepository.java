package com.gestionacademica.android.data.repository;

import android.content.Context;
import com.gestionacademica.android.data.dto.AsignaturaDto;
import com.gestionacademica.android.data.dto.AsignaturaRequest;
import com.gestionacademica.android.data.remote.ApiClient;
import com.gestionacademica.android.data.remote.AsignaturaApiService;
import com.gestionacademica.android.utils.ApiError;
import com.gestionacademica.android.utils.ApiErrorHandler;
import java.util.List;
import retrofit2.Call;
import retrofit2.Response;

public final class AsignaturaRepository {
    public interface Callback<T> {
        void onSuccess(T value);
        void onError(ApiError error);
    }

    private final AsignaturaApiService service;
    private final ApiError configurationError;

    public AsignaturaRepository(Context context) {
        AsignaturaApiService api = null;
        ApiError error = null;
        try {
            api = ApiClient.getInstance(context.getApplicationContext())
                    .createService(AsignaturaApiService.class);
        } catch (IllegalStateException exception) {
            error = new ApiError(-1, exception.getMessage(), null);
        }
        service = api;
        configurationError = error;
    }

    public void listar(Callback<List<AsignaturaDto>> callback) {
        if (isUnavailable(callback)) return;
        execute(service.listar(), callback);
    }

    public void obtener(long id, Callback<AsignaturaDto> callback) {
        if (isUnavailable(callback)) return;
        execute(service.obtener(id), callback);
    }

    public void crear(AsignaturaRequest request, Callback<AsignaturaDto> callback) {
        if (isUnavailable(callback)) return;
        execute(service.crear(request), callback);
    }

    public void actualizar(long id, AsignaturaRequest request, Callback<AsignaturaDto> callback) {
        if (isUnavailable(callback)) return;
        execute(service.actualizar(id, request), callback);
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