package vaf.scrapper;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

@FunctionalInterface
public interface Action {

    void accept(WebDriver driver);

    static Action waitForBookingContent(long timeout, long pollingRate) {
        return driver -> new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(timeout))
                .pollingEvery(Duration.ofMillis(pollingRate))
                .ignoring(NoSuchElementException.class)
                .until(ExpectedConditions.presenceOfElementLocated(By.id("booking-content")));
    }

    static Action refuseCookies() {
        return driver -> {
            driver.get("https://www.doctolib.fr/");
            WebElement noButton = driver.findElement(By.id("didomi-notice-disagree-button"));
            noButton.click();
        };
    }

    static Action selectSpeciality() {
        return driver -> {
            Select specialitySelect = (Select) driver.findElement(By.id("booking_speciality"));
            specialitySelect.selectByVisibleText("Vaccination COVID-19");
        };
    }

    static Action specifyUnknownDoctor() {
        return driver -> {
            WebElement noButton = driver.findElement(By.xpath("//label[@for = 'all_visit_motives-1']"));
            noButton.click();
        };
    }

    static Action selectVaccine(final int selectIndex) {
        return driver -> {
            WebElement bookingMotiveElement = driver.findElement(By.id("booking_motive"));
            Select bookingMotiveSelector = new Select(bookingMotiveElement);
            bookingMotiveSelector.selectByIndex(selectIndex);
        };
    }

    static boolean appointmentAlreadyTaken(final WebDriver driver) {
        try {
            System.out.println("looking for popup");
            new WebDriverWait(driver, Duration.ofMillis(500))
                    .until(ExpectedConditions.presenceOfElementLocated(By.xpath("//button[@aria-label = 'close-toast']")));
            System.out.println("popup found!");
            return true;
        } catch (NoSuchElementException | TimeoutException e) {
            System.out.println("popup not found");
            return false;
        }
    }
}
