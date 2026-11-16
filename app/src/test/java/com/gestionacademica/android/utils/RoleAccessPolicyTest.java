package com.gestionacademica.android.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class RoleAccessPolicyTest {
    @Test
    public void adminCanViewAndManageAcademicData() {
        assertTrue(RoleAccessPolicy.canViewAcademicModules("ADMIN"));
        assertTrue(RoleAccessPolicy.canManageAcademicData("ADMIN"));
    }

    @Test
    public void userCanViewButCannotManageAcademicData() {
        assertTrue(RoleAccessPolicy.canViewAcademicModules("USER"));
        assertFalse(RoleAccessPolicy.canManageAcademicData("USER"));
    }

    @Test
    public void unknownOrMissingRoleHasNoLocalAccess() {
        assertFalse(RoleAccessPolicy.canViewAcademicModules("GUEST"));
        assertFalse(RoleAccessPolicy.canViewAcademicModules(null));
        assertFalse(RoleAccessPolicy.canManageAcademicData("guest"));
    }
}
