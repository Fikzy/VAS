package vaf;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class DateUtils {

    static final Calendar calendar = Calendar.getInstance();

    static final List<String> abbreviatedFrenchMonths = Arrays.asList(
            "janv.", "févr.", "mars", "avr.", "mai", "juin",
            "juil.", "août", "sept.", "oct.", "nov.", "déc."
    );

    public static LocalDateTime dateFromTitle(final String title) {
        String[] res = title.split("\\s|:");
        System.out.println(Arrays.toString(res));
        int monthIndex = abbreviatedFrenchMonths.indexOf(res[2]);
        if (monthIndex == -1) {
            VAF.logger.error(String.format("Unknown month: '%s'", res[2]));
            return null;
        }
        try {
            return LocalDateTime.of(
                    calendar.get(Calendar.YEAR), monthIndex + 1,
                    Integer.parseInt(res[1]), Integer.parseInt(res[3]), Integer.parseInt(res[4])
            );
        } catch (NumberFormatException ignored) {
            VAF.logger.error("Unknown ");
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
}
