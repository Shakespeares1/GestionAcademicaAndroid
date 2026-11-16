package com.gestionacademica.android.data.remote;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.gestionacademica.android.data.dto.ReportePageDto;
import com.gestionacademica.android.data.dto.UsuarioDto;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import okhttp3.HttpUrl;
import org.junit.Test;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ReportesApiServiceContractTest {
    private final ReportesApiService service = new Retrofit.Builder()
            .baseUrl("https://api.example.invalid/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ReportesApiService.class);

    @Test
    public void searchByNameUsesExactQueryKeyAndPartialValue() {
        HttpUrl url = service.buscarUsuarios("Ana Mar", null, null, 0, 50).request().url();

        assertEquals("/api/reportes/usuarios/busqueda", url.encodedPath());
        assertEquals("Ana Mar", url.queryParameter("nombre"));
        assertNull(url.queryParameter("username"));
        assertNull(url.queryParameter("email"));
        assertEquals("0", url.queryParameter("page"));
        assertEquals("50", url.queryParameter("size"));
    }

    @Test
    public void searchByUsernameUsesExactQueryKey() {
        HttpUrl url = service.buscarUsuarios(null, "ada.dev", null, 0, 50).request().url();

        assertEquals("ada.dev", url.queryParameter("username"));
        assertNull(url.queryParameter("nombre"));
        assertNull(url.queryParameter("email"));
    }

    @Test
    public void searchByEmailUsesExactQueryKey() {
        HttpUrl url = service.buscarUsuarios(null, null, "example.org", 0, 50).request().url();

        assertEquals("example.org", url.queryParameter("email"));
        assertNull(url.queryParameter("nombre"));
        assertNull(url.queryParameter("username"));
    }

    @Test
    public void combinedFiltersAndPaginationAreForwarded() {
        HttpUrl url = service.buscarUsuarios("Ana", "ada", "example.org", 2, 20).request().url();

        assertEquals("Ana", url.queryParameter("nombre"));
        assertEquals("ada", url.queryParameter("username"));
        assertEquals("example.org", url.queryParameter("email"));
        assertEquals("2", url.queryParameter("page"));
        assertEquals("20", url.queryParameter("size"));
    }

    @Test
    public void noFiltersOmitsFilterParametersAndKeepsPagination() {
        HttpUrl url = service.buscarUsuarios(null, null, null, 0, 50).request().url();

        assertNull(url.queryParameter("nombre"));
        assertNull(url.queryParameter("username"));
        assertNull(url.queryParameter("email"));
        assertEquals("0", url.queryParameter("page"));
        assertEquals("50", url.queryParameter("size"));
    }

    @Test
    public void responseMapsTheBackendPublicUserFields() {
        String json = "{\"items\":[{\"id\":8,\"username\":\"ada.dev\","
                + "\"nombreCompleto\":\"Ada User\",\"email\":\"ada@example.org\"}],"
                + "\"page\":1,\"size\":20,\"totalElements\":21,\"totalPages\":2}";
        Type type = new TypeToken<ReportePageDto<UsuarioDto>>() {}.getType();
        ReportePageDto<UsuarioDto> response = new Gson().fromJson(json, type);

        assertEquals(1, response.page);
        assertEquals(20, response.size);
        assertEquals(21L, response.totalElements);
        assertEquals("ada.dev", response.items.get(0).username);
        assertEquals("Ada User", response.items.get(0).nombreCompleto);
        assertEquals("ada@example.org", response.items.get(0).email);
        assertNull(response.items.get(0).role);
        assertNull(response.items.get(0).accountStatus);
    }

    @Test
    public void existingReportEndpointsRemainAvailable() {
        assertEquals("/api/reportes/asignaturas/carrera-semestre-area",
                service.asignaturasPorCarreraSemestreArea(null, null, null, 0, 50)
                        .request().url().encodedPath());
        assertEquals("/api/reportes/asignaturas/creditos-profesor-nombre",
                service.asignaturasPorCreditosProfesorNombre(null, null, null, null, 0, 50)
                        .request().url().encodedPath());
        assertEquals("/api/reportes/usuarios/rol-estado",
                service.usuariosPorRolEstado(null, null, 0, 50).request().url().encodedPath());
        assertEquals("/api/reportes/usuarios/fecha-registro",
                service.usuariosPorFechaRegistro(null, null, 0, 50).request().url().encodedPath());
    }
}
