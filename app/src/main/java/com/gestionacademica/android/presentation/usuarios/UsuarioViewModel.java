package com.gestionacademica.android.presentation.usuarios;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.gestionacademica.android.data.dto.ActualizarAccesoUsuarioRequest;
import com.gestionacademica.android.data.dto.ActualizarUsuarioRequest;
import com.gestionacademica.android.data.dto.RegistroUsuarioRequest;
import com.gestionacademica.android.data.dto.UsuarioDto;
import com.gestionacademica.android.data.repository.SessionManager;
import com.gestionacademica.android.data.repository.UsuarioRepository;
import com.gestionacademica.android.domain.Session;
import com.gestionacademica.android.utils.ApiError;
import java.util.List;
import java.util.Optional;

public final class UsuarioViewModel extends AndroidViewModel {
    private final UsuarioRepository repository;
    private final SessionManager sessionManager;
    private final MutableLiveData<UsuarioUiState<List<UsuarioDto>>> listState =
            new MutableLiveData<>(UsuarioUiState.idle());
    private final MutableLiveData<UsuarioUiState<UsuarioDto>> detailState =
            new MutableLiveData<>(UsuarioUiState.idle());
    private final MutableLiveData<UsuarioUiState<UsuarioDto>> saveState =
            new MutableLiveData<>(UsuarioUiState.idle());
    private final MutableLiveData<UsuarioUiState<UsuarioDto>> accessState =
            new MutableLiveData<>(UsuarioUiState.idle());
    private final MutableLiveData<UsuarioUiState<Void>> deleteState =
            new MutableLiveData<>(UsuarioUiState.idle());

    public UsuarioViewModel(@NonNull Application application) {
        super(application);
        repository = new UsuarioRepository(application);
        sessionManager = new SessionManager(application);
    }

    public LiveData<UsuarioUiState<List<UsuarioDto>>> listState() { return listState; }
    public LiveData<UsuarioUiState<UsuarioDto>> detailState() { return detailState; }
    public LiveData<UsuarioUiState<UsuarioDto>> saveState() { return saveState; }
    public LiveData<UsuarioUiState<UsuarioDto>> accessState() { return accessState; }
    public LiveData<UsuarioUiState<Void>> deleteState() { return deleteState; }

    public void loadAll() {
        ApiError denied = authorizationError();
        if (denied != null) {
            listState.setValue(UsuarioUiState.error(denied));
            return;
        }
        listState.setValue(UsuarioUiState.loading());
        repository.listar(new UsuarioRepository.Callback<List<UsuarioDto>>() {
            @Override public void onSuccess(List<UsuarioDto> value) {
                listState.postValue(UsuarioUiState.success(value));
            }
            @Override public void onError(ApiError error) {
                listState.postValue(UsuarioUiState.error(error));
            }
        });
    }

    public void loadById(long id) {
        ApiError denied = authorizationError();
        if (denied != null) {
            detailState.setValue(UsuarioUiState.error(denied));
            return;
        }
        detailState.setValue(UsuarioUiState.loading());
        repository.obtener(id, new UsuarioRepository.Callback<UsuarioDto>() {
            @Override public void onSuccess(UsuarioDto value) {
                detailState.postValue(UsuarioUiState.success(value));
            }
            @Override public void onError(ApiError error) {
                detailState.postValue(UsuarioUiState.error(error));
            }
        });
    }

    public void register(RegistroUsuarioRequest request) {
        ApiError denied = authorizationError();
        if (denied != null) {
            saveState.setValue(UsuarioUiState.error(denied));
            return;
        }
        saveState.setValue(UsuarioUiState.loading());
        repository.registrar(request, userCallback(saveState));
    }

    public void update(long id, ActualizarUsuarioRequest request) {
        ApiError denied = authorizationError();
        if (denied != null) {
            saveState.setValue(UsuarioUiState.error(denied));
            return;
        }
        saveState.setValue(UsuarioUiState.loading());
        repository.actualizar(id, request, userCallback(saveState));
    }

    public void updateAccess(long id, ActualizarAccesoUsuarioRequest request) {
        ApiError denied = authorizationError();
        if (denied != null) {
            accessState.setValue(UsuarioUiState.error(denied));
            return;
        }
        accessState.setValue(UsuarioUiState.loading());
        repository.actualizarAcceso(id, request, userCallback(accessState));
    }

    public void delete(long id) {
        ApiError denied = authorizationError();
        if (denied != null) {
            deleteState.setValue(UsuarioUiState.error(denied));
            return;
        }
        deleteState.setValue(UsuarioUiState.loading());
        repository.eliminar(id, new UsuarioRepository.Callback<Void>() {
            @Override public void onSuccess(Void ignored) {
                deleteState.postValue(UsuarioUiState.success(null));
            }
            @Override public void onError(ApiError error) {
                deleteState.postValue(UsuarioUiState.error(error));
            }
        });
    }

    private ApiError authorizationError() {
        Optional<Session> session = sessionManager.getSession();
        if (session.isPresent() && "ADMIN".equals(session.get().getRole())) return null;
        return new ApiError(403, "Esta operación requiere una sesión ADMIN activa.", null);
    }

    private UsuarioRepository.Callback<UsuarioDto> userCallback(
            MutableLiveData<UsuarioUiState<UsuarioDto>> target) {
        return new UsuarioRepository.Callback<UsuarioDto>() {
            @Override public void onSuccess(UsuarioDto value) {
                target.postValue(UsuarioUiState.success(value));
            }
            @Override public void onError(ApiError error) {
                target.postValue(UsuarioUiState.error(error));
            }
        };
    }
}