package com.gestionacademica.android.data.dto;

import com.google.gson.annotations.SerializedName;

public final class UsuarioDto {
    @SerializedName("id") public Long id;
    @SerializedName("username") public String username;
    @SerializedName("nombreCompleto") public String nombreCompleto;
    @SerializedName("email") public String email;
    @SerializedName("role") public String role;
    @SerializedName("accountStatus") public String accountStatus;
    @SerializedName("fechaCreacion") public String fechaCreacion;
}