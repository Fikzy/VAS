package vaf.scrapper;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import vaf.DateUtils;
import vaf.VAF;

import java.util.List;

public class AppointmentSnatcher extends Scrapper {

    public final ScannerInstance scannerInstance;

    protected AppointmentSnatcher(final String url, final List<Action> actions, ScannerInstance scannerInstance) {
        super(url);
        System.out.println("AppointmentSnatcher");
        this.actions.addAll(actions);
        this.scannerInstance = scannerInstance;
        snatch();
    }

    @Override
    protected ChromeOptions getOptions() {
        return VAF.baseOptions;
    }

    private void snatch() {

        System.out.println("snatch");

        applyActions();

        WebElement bookingAvailabilities = driver.findElement(By.className("booking-availabilities"));
        List<WebElement> slots = bookingAvailabilities.findElements(By.xpath("//div[@role = \"button\"]"));

        if (slots.isEmpty()) {
            driver.quit();
            System.err.println("Failed to snatch any appointment...");
            return;
        }

        // Look for valid appointments
        for (WebElement slot : slots) {

            String title = slot.getAttribute("title");

            if (DateUtils.dateFromTitle(title).isAfter(VAF.INSTANCE.maxDate))
                break;

            slot.click();

            // TODO: Check if failed to continue?
        }
    }
}
