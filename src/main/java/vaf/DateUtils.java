package vaf;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Locale;

public class DateUtils {

    static Calendar calendar = Calendar.getInstance();

    // Example: jeu. 20 mai 15:10
    public static DateTimeFormatter doctolibDateFormat = new DateTimeFormatterBuilder()
            .appendPattern("EEE d[d] MMM HH:mm")
            .parseDefaulting(ChronoField.YEAR, calendar.get(Calendar.YEAR))
            .toFormatter(Locale.FRANCE);

    public static LocalDateTime dateFromTitle(final String title) {
        try {
            return LocalDateTime.parse(title, doctolibDateFormat);
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            System.err.println("Failed to parse: " + title);
            VAF.INSTANCE.shutdown();
            return null;
        }
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

    public static boolean isLocalDateTimeInLocalTimeRange(final LocalDateTime date, final LocalTime rangeStart,
                                                          final LocalTime rangeEnd) {
        return date.getHour() >= rangeStart.getHour() && date.getMinute() >= rangeStart.getMinute() &&
                date.getHour() <= rangeEnd.getHour() && date.getMinute() <= rangeEnd.getMinute();
    }
}
