package com.gestionacademica.android.data.dto;

import com.google.gson.annotations.SerializedName;

public final class ActualizarAccesoUsuarioRequest {
    @SerializedName("role") public String role;
    @SerializedName("accountStatus") public String accountStatus;

    public ActualizarAccesoUsuarioRequest() {}
    public ActualizarAccesoUsuarioRequest(String role, String accountStatus) {
        this.role = role;
        this.accountStatus = accountStatus;
    }
}