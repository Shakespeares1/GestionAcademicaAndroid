package com.gestionacademica.android.utils;

/**
 * Local UI access rules for roles understood by the Android client.
 * The backend remains authoritative for every protected API operation.
 */
public final class RoleAccessPolicy {
    private RoleAccessPolicy() {}

    public static boolean isAdmin(String role) {
        return "ADMIN".equals(role);
    }

    public static boolean canViewAcademicModules(String role) {
        return isAdmin(role) || "USER".equals(role);
    }

    public static boolean canManageAcademicData(String role) {
        return isAdmin(role);
    }
}
