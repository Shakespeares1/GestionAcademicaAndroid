package com.gestionacademica.android.data.dto;

import com.google.gson.annotations.SerializedName;

public final class AsignaturaRequest {
    @SerializedName("nombre") public String nombre;
    @SerializedName("nombreCompleto") public String nombreCompleto;
    @SerializedName("descripcion") public String descripcion;
    @SerializedName("areaConocimiento") public String areaConocimiento;
    @SerializedName("carrera") public String carrera;
    @SerializedName("numeroCreditos") public Integer numeroCreditos;
    @SerializedName("contenidoTematico") public String contenidoTematico;
    @SerializedName("semestre") public Integer semestre;
    @SerializedName("profesor") public String profesor;
}