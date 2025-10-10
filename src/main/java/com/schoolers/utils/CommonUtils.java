package com.schoolers.utils;

import org.springframework.context.i18n.LocaleContextHolder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

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

    public static String formatSessionDate(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy", LocaleContextHolder.getLocale()));
    }

    public static String formatSessionTime(LocalTime startTime, LocalTime endTime) {
        return String.format("%s - %s",
                startTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                endTime.format(DateTimeFormatter.ofPattern("HH:mm")));
    }
}
