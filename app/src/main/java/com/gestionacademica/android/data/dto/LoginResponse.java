package com.gestionacademica.android.data.dto;

import com.google.gson.annotations.SerializedName;

public final class LoginResponse {
    @SerializedName("accessToken") public String accessToken;
    @SerializedName("tokenType") public String tokenType;
    @SerializedName("expiresIn") public long expiresIn;
    @SerializedName("user") public PublicUser user;

    public static final class PublicUser {
        @SerializedName("id") public Long id;
        @SerializedName("username") public String username;
        @SerializedName("nombreCompleto") public String nombreCompleto;
        @SerializedName("email") public String email;
        @SerializedName("role") public String role;
    }
}