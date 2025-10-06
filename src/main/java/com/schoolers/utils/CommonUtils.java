package com.schoolers.utils;

public class CommonUtils {

    private CommonUtils() {

    }

    public static String normalizePhoneNumber(String raw) {
        if (raw == null || raw.isBlank()) return null;

        raw = raw.trim()
                .replaceAll("[^0-9+]", "");

        if (raw.startsWith("+62")) {
            return raw.replaceFirst("\\+", "");
        } else if (raw.startsWith("62")) {
            return raw; // already good
        } else if (raw.startsWith("0")) {
            return "62" + raw.substring(1);
        } else {
            return "62" + raw;
        }
    }
}
