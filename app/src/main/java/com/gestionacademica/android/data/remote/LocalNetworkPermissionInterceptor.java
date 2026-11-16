package com.gestionacademica.android.data.remote;

import android.content.Context;

import com.gestionacademica.android.R;
import com.gestionacademica.android.utils.LocalNetworkAccess;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

public final class LocalNetworkPermissionInterceptor implements Interceptor {
    private final Context applicationContext;

    public LocalNetworkPermissionInterceptor(Context context) {
        applicationContext = context.getApplicationContext();
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        String host = chain.request().url().host();
        if (LocalNetworkAccess.isPermissionRequiredForHost(host)
                && applicationContext.checkSelfPermission(LocalNetworkAccess.ACCESS_LOCAL_NETWORK)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            throw new LocalNetworkPermissionRequiredException(
                    applicationContext.getString(R.string.local_network_permission_error));
        }
        return chain.proceed(chain.request());
    }
}
