package vaf;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Calendar;
import java.util.Locale;

public class DateUtils {

    static Calendar calendar = Calendar.getInstance();

    // Example: jeu. 20 mai 15:10
    public static SimpleDateFormat dateFormat = new SimpleDateFormat(
            "EEE dd MMM hh:mm", Locale.FRENCH
    );

    public static DateTimeFormatter doctolibDateFormat = new DateTimeFormatterBuilder()
            .appendPattern("EEE dd MMM hh:mm")
            .parseDefaulting(ChronoField.YEAR, calendar.get(Calendar.YEAR))
            .toFormatter(Locale.FRENCH);

    public static LocalDateTime dateFromTitle(final String title) {
        return LocalDateTime.parse(title, doctolibDateFormat);
    }

    public static LocalDateTime zeroedCurrentDate() {
        return LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
    }

    public static LocalDateTime getZeroedDateOffset(int offset) {
        return zeroedCurrentDate().plusDays(offset);
    }
}
