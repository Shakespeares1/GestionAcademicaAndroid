package com.gestionacademica.android.presentation.asignaturas;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.gestionacademica.android.data.dto.AsignaturaDto;
import com.gestionacademica.android.data.dto.AsignaturaRequest;
import com.gestionacademica.android.data.repository.AsignaturaRepository;
import com.gestionacademica.android.data.repository.SessionManager;
import com.gestionacademica.android.domain.Session;
import com.gestionacademica.android.utils.ApiError;
import com.gestionacademica.android.utils.RoleAccessPolicy;
import java.util.Optional;
import java.util.List;

public final class AsignaturasViewModel extends AndroidViewModel {
    private final AsignaturaRepository repository;
    private final SessionManager sessionManager;
    private final MutableLiveData<AsignaturaUiState<List<AsignaturaDto>>> listState =
            new MutableLiveData<>(AsignaturaUiState.idle());
    private final MutableLiveData<AsignaturaUiState<AsignaturaDto>> detailState =
            new MutableLiveData<>(AsignaturaUiState.idle());
    private final MutableLiveData<AsignaturaUiState<AsignaturaDto>> saveState =
            new MutableLiveData<>(AsignaturaUiState.idle());
    private final MutableLiveData<AsignaturaUiState<Void>> deleteState =
            new MutableLiveData<>(AsignaturaUiState.idle());

    public AsignaturasViewModel(@NonNull Application application) {
        super(application);
        repository = new AsignaturaRepository(application);
        sessionManager = new SessionManager(application);
    }

    public LiveData<AsignaturaUiState<List<AsignaturaDto>>> listState() { return listState; }
    public LiveData<AsignaturaUiState<AsignaturaDto>> detailState() { return detailState; }
    public LiveData<AsignaturaUiState<AsignaturaDto>> saveState() { return saveState; }
    public LiveData<AsignaturaUiState<Void>> deleteState() { return deleteState; }

    public void loadAll() {
        ApiError accessError = accessError(false);
        if (accessError != null) {
            listState.setValue(AsignaturaUiState.error(accessError));
            return;
        }
        listState.setValue(AsignaturaUiState.loading());
        repository.listar(new AsignaturaRepository.Callback<List<AsignaturaDto>>() {
            @Override public void onSuccess(List<AsignaturaDto> value) {
                listState.postValue(AsignaturaUiState.success(value));
            }
            @Override public void onError(ApiError error) {
                listState.postValue(AsignaturaUiState.error(error));
            }
        });
    }

    public void loadById(long id) {
        ApiError accessError = accessError(false);
        if (accessError != null) {
            detailState.setValue(AsignaturaUiState.error(accessError));
            return;
        }
        detailState.setValue(AsignaturaUiState.loading());
        repository.obtener(id, new AsignaturaRepository.Callback<AsignaturaDto>() {
            @Override public void onSuccess(AsignaturaDto value) {
                detailState.postValue(AsignaturaUiState.success(value));
            }
            @Override public void onError(ApiError error) {
                detailState.postValue(AsignaturaUiState.error(error));
            }
        });
    }

    public void create(AsignaturaRequest request) {
        ApiError accessError = accessError(true);
        if (accessError != null) {
            saveState.setValue(AsignaturaUiState.error(accessError));
            return;
        }
        saveState.setValue(AsignaturaUiState.loading());
        repository.crear(request, saveCallback());
    }

    public void update(long id, AsignaturaRequest request) {
        ApiError accessError = accessError(true);
        if (accessError != null) {
            saveState.setValue(AsignaturaUiState.error(accessError));
            return;
        }
        saveState.setValue(AsignaturaUiState.loading());
        repository.actualizar(id, request, saveCallback());
    }

    public void delete(long id) {
        ApiError accessError = accessError(true);
        if (accessError != null) {
            deleteState.setValue(AsignaturaUiState.error(accessError));
            return;
        }
        deleteState.setValue(AsignaturaUiState.loading());
        repository.eliminar(id, new AsignaturaRepository.Callback<Void>() {
            @Override public void onSuccess(Void ignored) {
                deleteState.postValue(AsignaturaUiState.success(null));
            }
            @Override public void onError(ApiError error) {
                deleteState.postValue(AsignaturaUiState.error(error));
            }
        });
    }

    private ApiError accessError(boolean adminRequired) {
        Optional<Session> session = sessionManager.getSession();
        if (!session.isPresent()) {
            return new ApiError(401, "Inicia sesión para gestionar asignaturas.", null);
        }
        String role = session.get().getRole();
        if (adminRequired ? !RoleAccessPolicy.canManageAcademicData(role)
                : !RoleAccessPolicy.canViewAcademicModules(role)) {
            return new ApiError(403, adminRequired
                    ? "Solo ADMIN puede modificar asignaturas."
                    : "El rol de la sesión no puede consultar asignaturas.", null);
        }
        return null;
    }

    private AsignaturaRepository.Callback<AsignaturaDto> saveCallback() {
        return new AsignaturaRepository.Callback<AsignaturaDto>() {
            @Override public void onSuccess(AsignaturaDto value) {
                saveState.postValue(AsignaturaUiState.success(value));
            }
            @Override public void onError(ApiError error) {
                saveState.postValue(AsignaturaUiState.error(error));
            }
        };
    }
}