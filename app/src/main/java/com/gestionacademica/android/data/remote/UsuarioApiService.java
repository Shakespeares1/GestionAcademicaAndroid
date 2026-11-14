package com.gestionacademica.android.data.remote;

import com.gestionacademica.android.data.dto.ActualizarAccesoUsuarioRequest;
import com.gestionacademica.android.data.dto.ActualizarUsuarioRequest;
import com.gestionacademica.android.data.dto.RegistroUsuarioRequest;
import com.gestionacademica.android.data.dto.UsuarioDto;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface UsuarioApiService {
    @POST("api/usuarios")
    Call<UsuarioDto> registrar(@Body RegistroUsuarioRequest request);

    @GET("api/usuarios")
    Call<List<UsuarioDto>> listar();

    @GET("api/usuarios/{id}")
    Call<UsuarioDto> obtener(@Path("id") long id);

    @PUT("api/usuarios/{id}")
    Call<UsuarioDto> actualizar(@Path("id") long id, @Body ActualizarUsuarioRequest request);

    @DELETE("api/usuarios/{id}")
    Call<Void> eliminar(@Path("id") long id);

    @PUT("api/usuarios/{id}/access")
    Call<UsuarioDto> actualizarAcceso(@Path("id") long id,
            @Body ActualizarAccesoUsuarioRequest request);
}