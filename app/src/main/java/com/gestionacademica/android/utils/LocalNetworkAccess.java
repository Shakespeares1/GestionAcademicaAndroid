package com.gestionacademica.android.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import com.gestionacademica.android.BuildConfig;

import java.net.URI;
import java.net.URISyntaxException;

public final class LocalNetworkAccess {
    public static final String ACCESS_LOCAL_NETWORK =
            "android.permission.ACCESS_LOCAL_NETWORK";

    private LocalNetworkAccess() {}

    public static boolean isPermissionRequired(String baseUrl) {
        if (!BuildConfig.DEBUG || Build.VERSION.SDK_INT < 37 || baseUrl == null) {
            return false;
        }
        try {
            URI uri = new URI(baseUrl);
            return isLocalNetworkHost(uri.getHost());
        } catch (URISyntaxException exception) {
            return false;
        }
    }

    public static boolean isPermissionRequiredForHost(String host) {
        return BuildConfig.DEBUG && Build.VERSION.SDK_INT >= 37 && isLocalNetworkHost(host);
    }

    public static boolean isGranted(Context context, String baseUrl) {
        return !isPermissionRequired(baseUrl)
                || context.checkSelfPermission(ACCESS_LOCAL_NETWORK) == PackageManager.PERMISSION_GRANTED;
    }

    private static boolean isLocalNetworkHost(String host) {
        if (host == null) return false;
        String normalized = host.toLowerCase(java.util.Locale.ROOT);
        if ("localhost".equals(normalized) || "10.0.2.2".equals(normalized)) {
            return true;
        }
        String ipv6Host = normalized.startsWith("[") && normalized.endsWith("]")
                ? normalized.substring(1, normalized.length() - 1) : normalized;
        if (ipv6Host.contains(":")) {
            return "::1".equals(ipv6Host) || ipv6Host.startsWith("fe80:")
                    || ipv6Host.startsWith("fc") || ipv6Host.startsWith("fd");
        }

        String[] octets = normalized.split("\\.", -1);
        if (octets.length != 4) return false;
        try {
            int first = Integer.parseInt(octets[0]);
            int second = Integer.parseInt(octets[1]);
            int third = Integer.parseInt(octets[2]);
            int fourth = Integer.parseInt(octets[3]);
            if (!isOctet(first) || !isOctet(second) || !isOctet(third) || !isOctet(fourth)) {
                return false;
            }
            return first == 10
                    || first == 127
                    || (first == 172 && second >= 16 && second <= 31)
                    || (first == 192 && second == 168)
                    || (first == 169 && second == 254);
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    private static boolean isOctet(int value) {
        return value >= 0 && value <= 255;
    }
}
