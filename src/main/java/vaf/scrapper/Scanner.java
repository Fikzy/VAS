package vaf.scrapper;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import vaf.DateUtils;
import vaf.VAF;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class Scanner extends Scrapper {

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
            VAF.INSTANCE.onScannerSuccessfulScan.onNext(profile);
            VAF.INSTANCE.stopScanning();
            maximize();
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
//                    .until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@class = \"booking-message booking-message-warning undefined\"]")));
                    .until(ExpectedConditions.presenceOfNestedElementLocatedBy(bookingAvailabilities, By.xpath(".//div[@class = \"booking-message booking-message-warning undefined\"]")));

//            System.out.println("no appointments");
            return false;
        } catch (NoSuchElementException | TimeoutException ignored) {
        }

        // Fetch appointment slots
        System.out.println("looking for slots");

//        List<WebElement> slots = new WebDriverWait(driver, Duration.ofSeconds(1))
//                .until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//div[@role = \"button\"]")));

        List<WebElement> slots = bookingAvailabilities.findElements(By.xpath(".//div[@role = \"button\"]"));

        if (slots.isEmpty())
            return false;

        // Look for valid appointments
        for (WebElement slot : slots) {

            String title = slot.getAttribute("title");
            System.out.println(title);

            LocalDateTime appointmentDate = DateUtils.dateFromTitle(title);

            if (appointmentDate.isAfter(VAF.INSTANCE.searchMaxDate))
                return false;

            LocalTime appointmentTime = LocalTime.of(appointmentDate.getHour(), appointmentDate.getMinute(), 0);

//            if (appointmentTime.isBefore(VAF.INSTANCE.searchFromTime) || appointmentTime.isAfter(VAF.INSTANCE.searchToTime))
            if (appointmentTime.isBefore(profile.fromTime()) || appointmentTime.isAfter(profile.toTime()))
                return false;

            // Try to snatch appointment
            slot.click();

            // Check if failed (instantly returns false if failed)
            if (!Action.appointmentAlreadyTaken(driver))
                return true;
        }

        return false;
    }
}
