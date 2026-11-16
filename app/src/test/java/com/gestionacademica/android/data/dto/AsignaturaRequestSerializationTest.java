package com.gestionacademica.android.data.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Test;

public final class AsignaturaRequestSerializationTest {
    @Test
    public void serializesRequiredFieldsAndOmitsOptionalNullFields() {
        AsignaturaRequest request = new AsignaturaRequest();
        request.nombre = "Bases de datos";
        request.nombreCompleto = "Fundamentos de bases de datos";
        request.numeroCreditos = 4;
        request.semestre = 5;

        JsonObject json = JsonParser.parseString(new Gson().toJson(request)).getAsJsonObject();

        assertEquals("Bases de datos", json.get("nombre").getAsString());
        assertEquals("Fundamentos de bases de datos", json.get("nombreCompleto").getAsString());
        assertEquals(4, json.get("numeroCreditos").getAsInt());
        assertEquals(5, json.get("semestre").getAsInt());
        assertFalse(json.has("descripcion"));
        assertFalse(json.has("areaConocimiento"));
        assertFalse(json.has("carrera"));
        assertFalse(json.has("contenidoTematico"));
        assertFalse(json.has("profesor"));
    }
}
