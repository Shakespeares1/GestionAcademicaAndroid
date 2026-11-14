package com.gestionacademica.android.data.repository;

import android.content.Context;
import com.gestionacademica.android.data.dto.AsignaturaDto;
import com.gestionacademica.android.data.dto.ReportePageDto;
import com.gestionacademica.android.data.dto.UsuarioDto;
import com.gestionacademica.android.data.remote.ApiClient;
import com.gestionacademica.android.data.remote.ReportesApiService;
import com.gestionacademica.android.utils.ApiError;
import com.gestionacademica.android.utils.ApiErrorHandler;
import retrofit2.Call;
import retrofit2.Response;

public final class ReportesRepository {
    public interface Callback<T> {
        void onSuccess(T value);
        void onError(ApiError error);
    }

    private final ReportesApiService service;
    private final ApiError configurationError;

    public ReportesRepository(Context context) {
        ReportesApiService api = null;
        ApiError error = null;
        try {
            api = ApiClient.getInstance(context.getApplicationContext())
                    .createService(ReportesApiService.class);
        } catch (IllegalStateException exception) {
            error = new ApiError(-1, exception.getMessage(), null);
        }
        service = api;
        configurationError = error;
    }

    public void asignaturasPorCarreraSemestreArea(String carrera, Integer semestre,
            String areaConocimiento, int page, int size,
            Callback<ReportePageDto<AsignaturaDto>> callback) {
        if (isUnavailable(callback)) return;
        execute(service.asignaturasPorCarreraSemestreArea(
                carrera, semestre, areaConocimiento, page, size), callback);
    }

    public void asignaturasPorCreditosProfesorNombre(Integer numeroCreditosMin,
            Integer numeroCreditosMax, String profesor, String nombre, int page, int size,
            Callback<ReportePageDto<AsignaturaDto>> callback) {
        if (isUnavailable(callback)) return;
        execute(service.asignaturasPorCreditosProfesorNombre(
                numeroCreditosMin, numeroCreditosMax, profesor, nombre, page, size), callback);
    }

    public void usuariosPorRolEstado(String role, String accountStatus, int page, int size,
            Callback<ReportePageDto<UsuarioDto>> callback) {
        if (isUnavailable(callback)) return;
        execute(service.usuariosPorRolEstado(role, accountStatus, page, size), callback);
    }

    public void usuariosPorFechaRegistro(String fechaDesde, String fechaHasta, int page, int size,
            Callback<ReportePageDto<UsuarioDto>> callback) {
        if (isUnavailable(callback)) return;
        execute(service.usuariosPorFechaRegistro(fechaDesde, fechaHasta, page, size), callback);
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