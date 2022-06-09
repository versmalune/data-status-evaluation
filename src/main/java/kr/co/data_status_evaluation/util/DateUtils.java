package kr.co.data_status_evaluation.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public static final int DEFAULT_ZONE_OFFSET = 9;

    public static Instant localDateToInstant(LocalDate localDate) {
        return localDate.atStartOfDay().toInstant(ZoneOffset.ofHours(DEFAULT_ZONE_OFFSET));
    }

    public static Instant localDateToInstant(LocalDate localDate, int offset) {
        return localDate.atStartOfDay().toInstant(ZoneOffset.ofHours(offset));
    }

    public static Instant stringToInstant(String dateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return localDateToInstant(LocalDate.parse(dateStr, formatter));
    }

    public static Date localDateToDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public static Date getDateWithoutTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static Date getDateWithLastTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 59);
        return cal.getTime();
    }

    public static Instant getDateFromInstant(Instant instant) {
        Date date = Date.from(instant);
        Date dateWithoutTime = getDateWithoutTime(date);
        return dateWithoutTime.toInstant();
    }
}
