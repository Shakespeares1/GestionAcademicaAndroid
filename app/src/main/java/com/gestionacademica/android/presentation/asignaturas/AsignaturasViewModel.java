package com.gestionacademica.android.presentation.asignaturas;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.gestionacademica.android.data.dto.AsignaturaDto;
import com.gestionacademica.android.data.dto.AsignaturaRequest;
import com.gestionacademica.android.data.repository.AsignaturaRepository;
import com.gestionacademica.android.utils.ApiError;
import java.util.List;

public final class AsignaturasViewModel extends AndroidViewModel {
    private final AsignaturaRepository repository;
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
    }

    public LiveData<AsignaturaUiState<List<AsignaturaDto>>> listState() { return listState; }
    public LiveData<AsignaturaUiState<AsignaturaDto>> detailState() { return detailState; }
    public LiveData<AsignaturaUiState<AsignaturaDto>> saveState() { return saveState; }
    public LiveData<AsignaturaUiState<Void>> deleteState() { return deleteState; }

    public void loadAll() {
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
        saveState.setValue(AsignaturaUiState.loading());
        repository.crear(request, saveCallback());
    }

    public void update(long id, AsignaturaRequest request) {
        saveState.setValue(AsignaturaUiState.loading());
        repository.actualizar(id, request, saveCallback());
    }

    public void delete(long id) {
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