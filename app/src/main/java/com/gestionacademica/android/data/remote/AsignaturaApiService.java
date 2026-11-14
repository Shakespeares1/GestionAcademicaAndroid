package com.gestionacademica.android.data.remote;

import com.gestionacademica.android.data.dto.AsignaturaDto;
import com.gestionacademica.android.data.dto.AsignaturaRequest;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface AsignaturaApiService {
    @GET("api/asignaturas")
    Call<List<AsignaturaDto>> listar();

    @GET("api/asignaturas/{id}")
    Call<AsignaturaDto> obtener(@Path("id") long id);

    @POST("api/asignaturas")
    Call<AsignaturaDto> crear(@Body AsignaturaRequest request);

    @PUT("api/asignaturas/{id}")
    Call<AsignaturaDto> actualizar(@Path("id") long id, @Body AsignaturaRequest request);

    @DELETE("api/asignaturas/{id}")
    Call<Void> eliminar(@Path("id") long id);
}