package com.gestionacademica.android.data.dto;

import com.google.gson.annotations.SerializedName;

public final class ForgotPasswordRequest {
    @SerializedName("email") public String email;

    public ForgotPasswordRequest() {}
    public ForgotPasswordRequest(String email) { this.email = email; }
}