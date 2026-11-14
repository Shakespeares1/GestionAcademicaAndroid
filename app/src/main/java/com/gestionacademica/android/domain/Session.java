package com.gestionacademica.android.domain;

public final class Session {
    private final String accessToken;
    private final String role;
    private final long expiresAtEpochSeconds;

    public Session(String accessToken, String role, long expiresAtEpochSeconds) {
        this.accessToken = accessToken;
        this.role = role;
        this.expiresAtEpochSeconds = expiresAtEpochSeconds;
    }

    public String getAccessToken() { return accessToken; }
    public String getRole() { return role; }
    public long getExpiresAtEpochSeconds() { return expiresAtEpochSeconds; }
}