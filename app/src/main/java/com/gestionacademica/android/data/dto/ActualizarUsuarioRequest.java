package com.gestionacademica.android.data.dto;

import com.google.gson.annotations.SerializedName;

public final class ActualizarUsuarioRequest {
    @SerializedName("nombreCompleto") public String nombreCompleto;
    @SerializedName("email") public String email;

    public ActualizarUsuarioRequest() {}
    public ActualizarUsuarioRequest(String nombreCompleto, String email) {
        this.nombreCompleto = nombreCompleto;
        this.email = email;
    }
}