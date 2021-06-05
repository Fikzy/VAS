import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import vaf.DateUtils;

import java.time.LocalDateTime;
import java.util.Calendar;

public class DateUtilsTests {

    @Test
    public void dateFromTitleTest1() {
        String input = "jeu. 20 mai 15:10";
        LocalDateTime expected = LocalDateTime.of(
                Calendar.getInstance().get(Calendar.YEAR),
                5, 20, 15, 10
        );

        LocalDateTime date = DateUtils.dateFromTitle(input);

        Assertions.assertEquals(date, expected);
    }
}
