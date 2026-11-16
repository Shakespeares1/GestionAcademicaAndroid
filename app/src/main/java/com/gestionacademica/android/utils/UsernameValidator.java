package com.gestionacademica.android.utils;

import java.util.regex.Pattern;

public final class UsernameValidator {
    private static final Pattern FORMAT =
            Pattern.compile("[a-z0-9][a-z0-9._-]{1,28}[a-z0-9]");

    private UsernameValidator() {}

    public static String normalize(String username) {
        if (username == null) return "";
        StringBuilder normalized = new StringBuilder(username.length());
        for (int index = 0; index < username.length(); index++) {
            char character = username.charAt(index);
            if (character >= 'A' && character <= 'Z') {
                character = (char) (character + ('a' - 'A'));
            }
            normalized.append(character);
        }
        return normalized.toString();
    }

    public static boolean isValid(String username) {
        return username != null
                && username.length() >= 3
                && username.length() <= 30
                && FORMAT.matcher(username).matches();
    }
}
