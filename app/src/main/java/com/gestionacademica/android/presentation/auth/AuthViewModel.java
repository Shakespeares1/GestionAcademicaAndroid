package com.gestionacademica.android.presentation.auth;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.gestionacademica.android.data.dto.ForgotPasswordRequest;
import com.gestionacademica.android.data.dto.ForgotPasswordResponse;
import com.gestionacademica.android.data.dto.LoginRequest;
import com.gestionacademica.android.data.dto.LoginResponse;
import com.gestionacademica.android.data.dto.RegistroUsuarioRequest;
import com.gestionacademica.android.data.dto.ResetPasswordRequest;
import com.gestionacademica.android.data.dto.UsuarioDto;
import com.gestionacademica.android.data.repository.AuthRepository;

public final class AuthViewModel extends AndroidViewModel {
    private final AuthRepository repository;
    private final MutableLiveData<AuthUiState<LoginResponse>> loginState =
            new MutableLiveData<>(AuthUiState.idle());
    private final MutableLiveData<AuthUiState<UsuarioDto>> registrationState =
            new MutableLiveData<>(AuthUiState.idle());
    private final MutableLiveData<AuthUiState<ForgotPasswordResponse>> recoveryState =
            new MutableLiveData<>(AuthUiState.idle());
    private final MutableLiveData<AuthUiState<Void>> resetState =
            new MutableLiveData<>(AuthUiState.idle());

    public AuthViewModel(@NonNull Application application) {
        super(application);
        repository = new AuthRepository(application);
    }

    public LiveData<AuthUiState<LoginResponse>> loginState() { return loginState; }
    public LiveData<AuthUiState<UsuarioDto>> registrationState() { return registrationState; }
    public LiveData<AuthUiState<ForgotPasswordResponse>> recoveryState() { return recoveryState; }
    public LiveData<AuthUiState<Void>> resetState() { return resetState; }

    public void login(String email, String password) {
        loginState.setValue(AuthUiState.loading());
        repository.login(new LoginRequest(email, password), new AuthRepository.Callback<LoginResponse>() {
            @Override public void onSuccess(LoginResponse value) {
                loginState.postValue(AuthUiState.success(value));
            }
            @Override public void onError(com.gestionacademica.android.utils.ApiError error) {
                loginState.postValue(AuthUiState.error(error));
            }
        });
    }

    public void register(String nombreCompleto, String email, String password) {
        registrationState.setValue(AuthUiState.loading());
        repository.register(new RegistroUsuarioRequest(nombreCompleto, email, password),
                new AuthRepository.Callback<UsuarioDto>() {
                    @Override public void onSuccess(UsuarioDto value) {
                        registrationState.postValue(AuthUiState.success(value));
                    }
                    @Override public void onError(com.gestionacademica.android.utils.ApiError error) {
                        registrationState.postValue(AuthUiState.error(error));
                    }
                });
    }

    public void requestPasswordReset(String email) {
        recoveryState.setValue(AuthUiState.loading());
        repository.requestPasswordReset(new ForgotPasswordRequest(email),
                new AuthRepository.Callback<ForgotPasswordResponse>() {
                    @Override public void onSuccess(ForgotPasswordResponse value) {
                        recoveryState.postValue(AuthUiState.success(value));
                    }
                    @Override public void onError(com.gestionacademica.android.utils.ApiError error) {
                        recoveryState.postValue(AuthUiState.error(error));
                    }
                });
    }

    public void resetPassword(String token, String newPassword) {
        resetState.setValue(AuthUiState.loading());
        repository.resetPassword(new ResetPasswordRequest(token, newPassword),
                new AuthRepository.Callback<Void>() {
                    @Override public void onSuccess(Void ignored) {
                        resetState.postValue(AuthUiState.success(null));
                    }
                    @Override public void onError(com.gestionacademica.android.utils.ApiError error) {
                        resetState.postValue(AuthUiState.error(error));
                    }
                });
    }

    public void logout() {
        repository.logout();
    }
}