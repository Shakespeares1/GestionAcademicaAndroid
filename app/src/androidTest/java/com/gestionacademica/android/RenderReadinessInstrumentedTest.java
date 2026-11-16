package com.gestionacademica.android;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.gestionacademica.android.data.remote.ApiClient;
import java.io.IOException;
import okhttp3.ResponseBody;
import org.junit.Test;
import org.junit.runner.RunWith;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.GET;

/** Read-only smoke test for the configured debug API endpoint over the app's Retrofit/OkHttp stack. */
@RunWith(AndroidJUnit4.class)
public final class RenderReadinessInstrumentedTest {
    @Test
    public void publicReadinessUsesHttpsAndReturnsUp() throws IOException {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        ReadinessApi api = ApiClient.getInstance(context).createService(ReadinessApi.class);

        Response<ResponseBody> response = api.readiness().execute();
        assertEquals("Expected HTTP 200 from readiness", 200, response.code());
        ResponseBody body = response.body();
        assertNotNull("Readiness response body is missing", body);
        String json;
        try (ResponseBody responseBody = body) {
            json = responseBody.string();
        }
        assertTrue("Expected readiness JSON to report UP", json.contains("\"status\":\"UP\""));
    }

    private interface ReadinessApi {
        @GET("actuator/health/readiness")
        Call<ResponseBody> readiness();
    }
}
