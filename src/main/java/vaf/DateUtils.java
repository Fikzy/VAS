package vaf;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Locale;

public class DateUtils {

    static Calendar calendar = Calendar.getInstance();

    // Example: jeu. 20 mai 15:10
    public static DateTimeFormatter doctolibDateFormat = new DateTimeFormatterBuilder()
            .appendPattern("EEE dd MMM HH:mm")
            .parseDefaulting(ChronoField.YEAR, calendar.get(Calendar.YEAR))
            .toFormatter(Locale.FRANCE);

    public static LocalDateTime dateFromTitle(final String title) {
        return LocalDateTime.parse(title, doctolibDateFormat);
    }

    public static LocalDateTime zeroedCurrentDate() {
        return LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
    }

    public static LocalDateTime getZeroedDateOffset(int offset) {
        return zeroedCurrentDate().plusDays(offset);
    }

    public static LocalTime localTimeRoundedToFiveMinutes(final LocalTime time) {
        return time.minusMinutes(time.getMinute() % 5).truncatedTo(ChronoUnit.MINUTES);
    }
}
