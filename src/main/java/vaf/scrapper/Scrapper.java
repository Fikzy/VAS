package vaf.scrapper;

import org.openqa.selenium.WebDriver;

public abstract class Scrapper {

    protected final WebDriver driver;

    protected Scrapper(boolean headless) {
        this.driver = Browser.getDriver(headless);
        Action.refuseCookies().accept(driver);
    }

    public void dispose() {
        driver.quit();
    }
}
