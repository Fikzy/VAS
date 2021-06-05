package vaf.scrapper;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;
import vaf.DateUtils;
import vaf.VAF;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class Scanner extends Scrapper {

//    private static final VAF.logger VAF.logger = VAF.loggerFactory.getVAF.logger(Scanner.class);

    public Scanner() {
        super(false);

        minimize();
    }

    public void scan() {

        ScannerProfile profile = VAF.INSTANCE.processScannerProfile();
        if (profile == null) {
            VAF.INSTANCE.stopScanning();
            return;
        }

        VAF.INSTANCE.onScannerStartScan.onNext(profile);

        if (scan(profile)) {
            VAF.INSTANCE.stopScanning();
            maximize();
            VAF.INSTANCE.onScannerSuccessfulScan.onNext(profile);
            return;
        }

        VAF.INSTANCE.onScannerStopScan.onNext(profile);
    }

    public boolean scan(final ScannerProfile profile) {

        // Load profile page
        driver.get(profile.url());
        Action.waitForBookingContent(2, 50).accept(driver);

        // Execute profile actions
        for (Action action : profile.actions())
            action.accept(driver);

        // Check if any slots available
        WebElement bookingAvailabilities = driver.findElement(By.className("booking-availabilities"));
        try {

            new FluentWait<>(driver)
                    .withTimeout(Duration.ofMillis(1000)) // Might need more
                    .pollingEvery(Duration.ofMillis(50))
                    .ignoring(NoSuchElementException.class)
                    .until(ExpectedConditions.presenceOfNestedElementLocatedBy(bookingAvailabilities, By.xpath(".//div[@class = \"booking-message booking-message-warning undefined\"]")));

            return false;
        } catch (NoSuchElementException | TimeoutException ignored) {
        }

        // Fetch appointment slots
        VAF.logger.info("looking for slots");

        List<WebElement> slots = bookingAvailabilities.findElements(By.xpath(".//div[@role = \"button\"]"));

        if (slots.isEmpty())
            return false;

        // Look for valid appointments
        for (WebElement slot : slots) {

            String title = slot.getAttribute("title");
            VAF.logger.info(title);

            LocalDateTime appointmentDate = DateUtils.dateFromTitle(title);
            if (appointmentDate == null)
                continue;

            VAF.logger.info(appointmentDate.toString());

            LocalTime appointmentTime = LocalTime.of(appointmentDate.getHour(), appointmentDate.getMinute(), 0);
            VAF.logger.info(appointmentTime.toString());

            VAF.logger.info("Checking if date falls whithin range");
            if (appointmentTime.isBefore(profile.fromTime()) || appointmentTime.isAfter(profile.toTime())) {
                VAF.logger.info("Appointment doesn't fall within time range");
                return false;
            }

            // Try to snatch appointment
            VAF.logger.info("Clicking");
            try {
                new WebDriverWait(driver, Duration.ofMillis(1000)).until(ExpectedConditions.elementToBeClickable(slot));
                slot.click();
            } catch (TimeoutException | StaleElementReferenceException ignored) {
                System.err.println("Slot element is no longer valid");
                continue;
            }
            VAF.logger.info("Clicked");

            // Check if failed (instantly returns false if failed)
            if (!Action.appointmentAlreadyTaken(driver)) {
                VAF.logger.info("Valid appointment");
                return true;
            }

            VAF.logger.info("Appointment already taken");
        }

        return false;
    }
}
