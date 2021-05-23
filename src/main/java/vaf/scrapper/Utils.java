package vaf.scrapper;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class Utils {

    static List<WebElement> getAppointmentSlots(final WebDriver driver) {
        WebElement bookingAvailabilities = driver.findElement(By.className("booking-availabilities"));
        return bookingAvailabilities.findElements(By.xpath("//div[@role = \"button\"]"));
    }
}
