package com.gestionacademica.android.data.dto;

import com.google.gson.annotations.SerializedName;

public final class AsignaturaDto {
    @SerializedName("id") public Long id;
    @SerializedName("nombre") public String nombre;
    @SerializedName("nombreCompleto") public String nombreCompleto;
    @SerializedName("descripcion") public String descripcion;
    @SerializedName("areaConocimiento") public String areaConocimiento;
    @SerializedName("carrera") public String carrera;
    @SerializedName("numeroCreditos") public int numeroCreditos;
    @SerializedName("contenidoTematico") public String contenidoTematico;
    @SerializedName("semestre") public int semestre;
    @SerializedName("profesor") public String profesor;
}