package com.easybilling.util;

import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for date and time operations.
 */
public final class DateTimeUtils {
    
    private DateTimeUtils() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    public static final DateTimeFormatter ISO_DATE_TIME = DateTimeFormatter.ISO_DATE_TIME;
    public static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_DATE;
    public static final DateTimeFormatter DISPLAY_DATE_TIME = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    public static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    
    public static Instant now() {
        return Instant.now();
    }
    
    public static LocalDateTime nowLocalDateTime() {
        return LocalDateTime.now();
    }
    
    public static LocalDate nowLocalDate() {
        return LocalDate.now();
    }
    
    public static LocalDateTime toLocalDateTime(Instant instant, ZoneId zoneId) {
        return LocalDateTime.ofInstant(instant, zoneId);
    }
    
    public static Instant toInstant(LocalDateTime localDateTime, ZoneId zoneId) {
        return localDateTime.atZone(zoneId).toInstant();
    }
    
    public static String format(LocalDateTime dateTime, DateTimeFormatter formatter) {
        return dateTime.format(formatter);
    }
    
    public static String format(LocalDate date, DateTimeFormatter formatter) {
        return date.format(formatter);
    }
    
    public static boolean isBetween(Instant instant, Instant start, Instant end) {
        return !instant.isBefore(start) && !instant.isAfter(end);
    }
}
