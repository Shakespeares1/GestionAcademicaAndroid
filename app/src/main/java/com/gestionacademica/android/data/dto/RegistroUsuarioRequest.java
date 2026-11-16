package com.gestionacademica.android.data.dto;

import com.google.gson.annotations.SerializedName;

public final class RegistroUsuarioRequest {
    @SerializedName("username") public String username;
    @SerializedName("nombreCompleto") public String nombreCompleto;
    @SerializedName("email") public String email;
    @SerializedName("password") public String password;

    public RegistroUsuarioRequest() {}
    public RegistroUsuarioRequest(String username, String nombreCompleto, String email, String password) {
        this.username = username;
        this.nombreCompleto = nombreCompleto;
        this.email = email;
        this.password = password;
    }
}