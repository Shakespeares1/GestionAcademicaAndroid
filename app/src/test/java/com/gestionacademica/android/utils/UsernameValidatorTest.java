package com.gestionacademica.android.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class UsernameValidatorTest {
    @Test
    public void aceptaUsernameValidoYLimitesDeLongitud() {
        assertTrue(UsernameValidator.isValid("abc"));
        assertTrue(UsernameValidator.isValid("valid.name_01-2"));
        assertTrue(UsernameValidator.isValid("a".repeat(30)));
    }

    @Test
    public void normalizaMayusculasAntesDeValidar() {
        String normalized = UsernameValidator.normalize("Student_01");
        assertEquals("student_01", normalized);
        assertTrue(UsernameValidator.isValid(normalized));
        assertFalse(UsernameValidator.isValid("Student_01"));
    }

    @Test
    public void rechazaEspaciosCaracteresYLongitudesFueraDelRango() {
        assertFalse(UsernameValidator.isValid(""));
        assertFalse(UsernameValidator.isValid("ab"));
        assertFalse(UsernameValidator.isValid("a".repeat(31)));
        assertFalse(UsernameValidator.isValid(" user"));
        assertFalse(UsernameValidator.isValid("user "));
        assertFalse(UsernameValidator.isValid("us er"));
        assertFalse(UsernameValidator.isValid(".user"));
        assertFalse(UsernameValidator.isValid("user_"));
        assertFalse(UsernameValidator.isValid("user$name"));
        assertFalse(UsernameValidator.isValid("mañana"));
        assertFalse(UsernameValidator.isValid(UsernameValidator.normalize("Kelvin")));
    }
}
