package com.gestionacademica.android.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import com.gestionacademica.android.domain.Session;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Optional;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import org.json.JSONException;
import org.json.JSONObject;

public final class SessionManager {
    private static final String PREFERENCES = "secure_session";
    private static final String SESSION_KEY = "encrypted_session";
    private static final String KEY_ALIAS = "gestion_academica_session_v1";
    private final SharedPreferences preferences;

    public SessionManager(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    public synchronized void saveSession(String accessToken, String role, long expiresInSeconds) {
        if (accessToken == null || accessToken.trim().isEmpty() || expiresInSeconds <= 0) {
            throw new IllegalArgumentException("La sesión recibida no es válida.");
        }
        try {
            long expiresAt = Math.addExact(System.currentTimeMillis() / 1000L, expiresInSeconds);
            JSONObject json = new JSONObject()
                    .put("accessToken", accessToken)
                    .put("role", role == null ? "" : role)
                    .put("expiresAt", expiresAt);
            preferences.edit().putString(SESSION_KEY,
                    Base64.encodeToString(encrypt(json.toString()), Base64.NO_WRAP)).apply();
        } catch (GeneralSecurityException | IOException | JSONException exception) {
            throw new IllegalStateException("No se pudo proteger la sesión en este dispositivo.", exception);
        }
    }

    public synchronized Optional<Session> getSession() {
        String encoded = preferences.getString(SESSION_KEY, null);
        if (encoded == null) return Optional.empty();
        try {
            String jsonText = new String(decrypt(Base64.decode(encoded, Base64.NO_WRAP)), StandardCharsets.UTF_8);
            JSONObject json = new JSONObject(jsonText);
            String token = json.getString("accessToken");
            String role = json.optString("role", "");
            long expiresAt = json.getLong("expiresAt");
            if (token.trim().isEmpty() || expiresAt <= System.currentTimeMillis() / 1000L) {
                clearSession();
                return Optional.empty();
            }
            return Optional.of(new Session(token, role, expiresAt));
        } catch (GeneralSecurityException | IOException | JSONException | IllegalArgumentException exception) {
            clearSession();
            return Optional.empty();
        }
    }

    public synchronized void clearSession() {
        preferences.edit().remove(SESSION_KEY).apply();
    }

    private byte[] encrypt(String value) throws GeneralSecurityException, IOException {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey());
        byte[] iv = cipher.getIV();
        byte[] ciphertext = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
        ByteBuffer output = ByteBuffer.allocate(1 + iv.length + ciphertext.length);
        output.put((byte) iv.length).put(iv).put(ciphertext);
        return output.array();
    }

    private byte[] decrypt(byte[] payload) throws GeneralSecurityException, IOException {
        if (payload.length < 14) throw new GeneralSecurityException("Sesión cifrada incompleta.");
        ByteBuffer input = ByteBuffer.wrap(payload);
        int ivLength = input.get() & 0xff;
        if (ivLength < 12 || ivLength > 16 || payload.length <= 1 + ivLength) {
            throw new GeneralSecurityException("Sesión cifrada inválida.");
        }
        byte[] iv = new byte[ivLength];
        input.get(iv);
        byte[] ciphertext = new byte[input.remaining()];
        input.get(ciphertext);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), new GCMParameterSpec(128, iv));
        return cipher.doFinal(ciphertext);
    }

    private SecretKey getOrCreateKey() throws GeneralSecurityException, IOException {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        java.security.Key existing = keyStore.getKey(KEY_ALIAS, null);
        if (existing instanceof SecretKey) return (SecretKey) existing;
        KeyGenerator generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        generator.init(new KeyGenParameterSpec.Builder(KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .setUserAuthenticationRequired(false)
                .build());
        return generator.generateKey();
    }
}