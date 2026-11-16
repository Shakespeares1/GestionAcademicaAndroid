package com.gestionacademica.android.data.remote;

import java.io.IOException;

public final class LocalNetworkPermissionRequiredException extends IOException {
    public LocalNetworkPermissionRequiredException(String message) {
        super(message);
    }
}
