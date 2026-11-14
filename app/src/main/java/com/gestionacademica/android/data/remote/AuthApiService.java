package com.gestionacademica.android.data.remote;

import com.gestionacademica.android.data.dto.ForgotPasswordRequest;
import com.gestionacademica.android.data.dto.ForgotPasswordResponse;
import com.gestionacademica.android.data.dto.LoginRequest;
import com.gestionacademica.android.data.dto.LoginResponse;
import com.gestionacademica.android.data.dto.ResetPasswordRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApiService {
    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("api/auth/forgot-password")
    Call<ForgotPasswordResponse> forgotPassword(@Body ForgotPasswordRequest request);

    @POST("api/auth/reset-password")
    Call<Void> resetPassword(@Body ResetPasswordRequest request);
}