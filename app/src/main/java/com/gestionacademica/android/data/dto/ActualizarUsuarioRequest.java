package com.gestionacademica.android.data.dto;

import com.google.gson.annotations.SerializedName;

public final class ActualizarUsuarioRequest {
    @SerializedName("username") public String username;
    @SerializedName("nombreCompleto") public String nombreCompleto;
    @SerializedName("email") public String email;

    public ActualizarUsuarioRequest() {}
    public ActualizarUsuarioRequest(String username, String nombreCompleto, String email) {
        this.username = username;
        this.nombreCompleto = nombreCompleto;
        this.email = email;
    }
}