package com.furever.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateUtils {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss 'UTC'")
            .withZone(ZoneId.of("UTC"));

    // 2. Метод конвертации
    public static String formatKickoff(long kickoffMillis) {
        return FORMATTER.format(Instant.ofEpochMilli(kickoffMillis));
    }
}
