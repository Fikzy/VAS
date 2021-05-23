package vaf.scrapper;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

@FunctionalInterface
public interface Action {

    void accept(Scrapper scrapper);

    static Action refuseCookies() {
        return scrapper -> {
            WebElement noButton = scrapper.driver.findElement(By.id("didomi-notice-disagree-button"));
            noButton.click();
        };
    }

    static Action selectSpeciality() {
        return scrapper -> {
            Select specialitySelect = (Select) scrapper.driver.findElement(By.id("booking_speciality"));
            specialitySelect.selectByVisibleText("Vaccination COVID-19");
        };
    }

    static Action specifyUnknownDoctor() {
        return scrapper -> {
            WebElement noButton = scrapper.driver.findElement(By.xpath("//label[@for = 'all_visit_motives-1']"));
            noButton.click();
        };
    }

    static Action selectVaccine(final int selectIndex) {
        return scrapper -> {
            WebElement bookingMotiveElement = scrapper.driver.findElement(By.id("booking_motive"));
            Select bookingMotiveSelector = new Select(bookingMotiveElement);
            bookingMotiveSelector.selectByIndex(selectIndex);
        };
    }
}
