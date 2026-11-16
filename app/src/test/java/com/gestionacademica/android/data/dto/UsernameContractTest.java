package com.gestionacademica.android.data.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.gson.Gson;
import org.junit.Test;

public final class UsernameContractTest {
    private final Gson gson = new Gson();

    @Test
    public void registrationAndUpdateRequestsUseBackendFieldNames() {
        RegistroUsuarioRequest registration =
                new RegistroUsuarioRequest("ada.user", "Ada User", "ada@example.org", "password");
        ActualizarUsuarioRequest update =
                new ActualizarUsuarioRequest("ada.user2", "Ada Lovelace", "ada@example.org");

        assertEquals("ada.user", gson.toJsonTree(registration).getAsJsonObject().get("username").getAsString());
        assertEquals("Ada User", gson.toJsonTree(registration).getAsJsonObject()
                .get("nombreCompleto").getAsString());
        assertEquals("ada.user2", gson.toJsonTree(update).getAsJsonObject().get("username").getAsString());
    }

    @Test
    public void userAndLoginResponsesMapUsernameWithoutChangingEmailLoginRequest() {
        UsuarioDto user = gson.fromJson(
                "{\"id\":7,\"username\":\"ada.user\",\"nombreCompleto\":\"Ada User\",\"email\":\"ada@example.org\"}",
                UsuarioDto.class);
        LoginResponse login = gson.fromJson(
                "{\"accessToken\":\"token\",\"tokenType\":\"Bearer\",\"expiresIn\":3600,"
                        + "\"user\":{\"id\":7,\"username\":\"ada.user\",\"email\":\"ada@example.org\",\"role\":\"USER\"}}",
                LoginResponse.class);

        assertEquals("ada.user", user.username);
        assertNotNull(login.user);
        assertEquals("ada.user", login.user.username);
        assertEquals("ada@example.org", login.user.email);
        assertEquals("ada@example.org",
                gson.toJsonTree(new LoginRequest("ada@example.org", "password"))
                        .getAsJsonObject().get("email").getAsString());
    }
}
