package com.gestionacademica.android.presentation.reportes;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.gestionacademica.android.data.dto.ReportePageDto;
import com.gestionacademica.android.data.dto.UsuarioDto;
import com.gestionacademica.android.data.repository.ReportesRepository;
import com.gestionacademica.android.data.repository.SessionManager;
import com.gestionacademica.android.domain.Session;
import com.gestionacademica.android.utils.ApiError;
import java.util.Optional;

public final class ReportesViewModel extends AndroidViewModel {
    private final ReportesRepository repository;
    private final SessionManager sessionManager;
    private final MutableLiveData<ReportesUiState> state =
            new MutableLiveData<>(ReportesUiState.idle());
    private long requestGeneration;

    public ReportesViewModel(@NonNull Application application) {
        super(application);
        repository = new ReportesRepository(application);
        sessionManager = new SessionManager(application);
    }

    public LiveData<ReportesUiState> state() { return state; }

    public void asignaturasPorCarreraSemestreArea(String carrera, Integer semestre,
            String areaConocimiento, int page, int size) {
        long generation = begin(ReportType.ASSIGNMENTS_CAREER_SEMESTER_AREA, false);
        if (generation < 0) return;
        repository.asignaturasPorCarreraSemestreArea(carrera, semestre, areaConocimiento,
                page, size, callback(ReportType.ASSIGNMENTS_CAREER_SEMESTER_AREA, generation));
    }

    public void asignaturasPorCreditosProfesorNombre(Integer numeroCreditosMin,
            Integer numeroCreditosMax, String profesor, String nombre, int page, int size) {
        long generation = begin(ReportType.ASSIGNMENTS_CREDITS_PROFESSOR_NAME, false);
        if (generation < 0) return;
        repository.asignaturasPorCreditosProfesorNombre(numeroCreditosMin, numeroCreditosMax,
                profesor, nombre, page, size,
                callback(ReportType.ASSIGNMENTS_CREDITS_PROFESSOR_NAME, generation));
    }

    public void usuariosPorRolEstado(String role, String accountStatus, int page, int size) {
        long generation = begin(ReportType.USERS_ROLE_STATUS, true);
        if (generation < 0) return;
        repository.usuariosPorRolEstado(role, accountStatus, page, size,
                callback(ReportType.USERS_ROLE_STATUS, generation));
    }

    public void usuariosPorFechaRegistro(String fechaDesde, String fechaHasta, int page, int size) {
        long generation = begin(ReportType.USERS_REGISTRATION_DATE, true);
        if (generation < 0) return;
        repository.usuariosPorFechaRegistro(fechaDesde, fechaHasta, page, size,
                callback(ReportType.USERS_REGISTRATION_DATE, generation));
    }

    public void buscarUsuarios(String nombre, String username, String email, int page, int size) {
        long generation = begin(ReportType.USERS_SEARCH, true);
        if (generation < 0) return;
        repository.buscarUsuarios(nombre, username, email, page, size,
                callback(ReportType.USERS_SEARCH, generation));
    }

    private long begin(ReportType type, boolean adminOnly) {
        long generation = ++requestGeneration;
        ApiError error = authorizationError(adminOnly);
        if (error != null) {
            state.setValue(ReportesUiState.error(type, error));
            return -1L;
        }
        state.setValue(ReportesUiState.loading(type));
        return generation;
    }

    private ApiError authorizationError(boolean adminOnly) {
        Optional<Session> session = sessionManager.getSession();
        if (!session.isPresent()) {
            return new ApiError(401, "Inicia sesión para consultar reportes.", null);
        }
        String role = session.get().getRole();
        if (!"ADMIN".equals(role) && !"USER".equals(role)) {
            return new ApiError(403, "El rol de la sesión no está autorizado.", null);
        }
        if (adminOnly && !"ADMIN".equals(role)) {
            return new ApiError(403, "Los reportes de usuarios requieren ADMIN.", null);
        }
        return null;
    }

    private <T> ReportesRepository.Callback<ReportePageDto<T>> callback(
            ReportType type, long generation) {
        return new ReportesRepository.Callback<ReportePageDto<T>>() {
            @Override public void onSuccess(ReportePageDto<T> value) {
                if (generation == requestGeneration) {
                    state.postValue(ReportesUiState.success(type, value));
                }
            }

            @Override public void onError(ApiError error) {
                if (generation == requestGeneration) {
                    state.postValue(ReportesUiState.error(type, error));
                }
            }
        };
    }
}