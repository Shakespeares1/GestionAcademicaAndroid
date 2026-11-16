package com.gestionacademica.android.data.remote;

import com.gestionacademica.android.data.dto.AsignaturaDto;
import com.gestionacademica.android.data.dto.ReportePageDto;
import com.gestionacademica.android.data.dto.UsuarioDto;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ReportesApiService {
    @GET("api/reportes/asignaturas/carrera-semestre-area")
    Call<ReportePageDto<AsignaturaDto>> asignaturasPorCarreraSemestreArea(
            @Query("carrera") String carrera,
            @Query("semestre") Integer semestre,
            @Query("areaConocimiento") String areaConocimiento,
            @Query("page") Integer page,
            @Query("size") Integer size);

    @GET("api/reportes/asignaturas/creditos-profesor-nombre")
    Call<ReportePageDto<AsignaturaDto>> asignaturasPorCreditosProfesorNombre(
            @Query("numeroCreditosMin") Integer numeroCreditosMin,
            @Query("numeroCreditosMax") Integer numeroCreditosMax,
            @Query("profesor") String profesor,
            @Query("nombre") String nombre,
            @Query("page") Integer page,
            @Query("size") Integer size);

    @GET("api/reportes/usuarios/rol-estado")
    Call<ReportePageDto<UsuarioDto>> usuariosPorRolEstado(
            @Query("role") String role,
            @Query("accountStatus") String accountStatus,
            @Query("page") Integer page,
            @Query("size") Integer size);

    @GET("api/reportes/usuarios/fecha-registro")
    Call<ReportePageDto<UsuarioDto>> usuariosPorFechaRegistro(
            @Query("fechaDesde") String fechaDesde,
            @Query("fechaHasta") String fechaHasta,
            @Query("page") Integer page,
            @Query("size") Integer size);

    @GET("api/reportes/usuarios/busqueda")
    Call<ReportePageDto<UsuarioDto>> buscarUsuarios(
            @Query("nombre") String nombre,
            @Query("username") String username,
            @Query("email") String email,
            @Query("page") Integer page,
            @Query("size") Integer size);
}