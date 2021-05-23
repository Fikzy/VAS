package vaf.scrapper;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import vaf.DateUtils;
import vaf.VAF;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class AppointmentScanner extends Scrapper {

    public final ScannerInstance scannerInstance;
    public final float refreshRate = 2.0f;

    public ScheduledFuture<?> scheduledFuture = null;

    public AppointmentScanner(final String url, final List<Action> actions, final ScannerInstance scannerInstance) {
        super(url);
        System.out.println(scannerInstance);
        this.actions.addAll(actions);
        this.scannerInstance = scannerInstance;
        this.actions.add(Action.selectVaccine(scannerInstance.selection().selectIndex()));
    }

    @Override
    protected ChromeOptions getOptions() {
        return VAF.headlessOptions;
    }

    public void scan() {

        System.out.println("scan");

        refresh();
        applyActions();

        // Find availabilities
        WebElement bookingAvailabilities = driver.findElement(By.className("booking-availabilities"));
        List<WebElement> slots = bookingAvailabilities.findElements(By.xpath("//div[@role = \"button\"]"));

        if (slots.isEmpty())
            return;

        // Look for valid appointments
        for (WebElement slot : slots) {

            String title = slot.getAttribute("title");
            LocalDateTime slotDate = DateUtils.dateFromTitle(title);
            System.out.println(slotDate);

            if (slotDate.isAfter(VAF.INSTANCE.maxDate))
                break;

            // TODO: Multiple instances?
            System.out.println("Valid appointment: " + slotDate);
            VAF.INSTANCE.instantiateScrapper(() -> new vaf.scrapper.AppointmentSnatcher(url, actions, scannerInstance));
            break;
        }
    }

    public void schedule() {
        scheduledFuture = VAF.INSTANCE.service.scheduleAtFixedRate(
                this::scan, 0, (long) (refreshRate * 1000), TimeUnit.MILLISECONDS
        );
    }

    public void stop() {

        System.out.println("stop");

        if (scheduledFuture != null)
            scheduledFuture.cancel(false);
        driver.quit();
    }
}
