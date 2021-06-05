package vaf.scrapper;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;
import vaf.app.App;

import java.util.prefs.Preferences;

public enum Browser {
    Chrome(ChromeDriver.class),
    Edge(EdgeDriver.class),
    Firefox(FirefoxDriver.class),
    Opera(SafariDriver.class);

    public final Class<? extends WebDriver> driverClass;

    Browser(Class<? extends WebDriver> driverClass) {
        this.driverClass = driverClass;
    }

    public static final String preferenceNode = "/browser";
    public static Browser browser;

    public static void setBrowser(Browser selectedBrowser) {
        browser = selectedBrowser;
        WebDriverManager.getInstance(browser.driverClass).setup();
    }

    public static void selectBrowser() {

        // FIXME: clear preferences for testing
//        try {
//            Preferences.userRoot().node(Browser.preferenceNode).clear();
//        } catch (BackingStoreException e) {
//            e.printStackTrace();
//        }

        String browser = Preferences.userRoot().node(Browser.preferenceNode).get("value", null);
        if (browser == null) {
            Browser.browser = App.browserSelection();
            Preferences.userRoot().node(Browser.preferenceNode).put("value", Browser.browser.toString());
        } else {
            Browser.browser = Browser.valueOf(browser);
        }

        Browser.setBrowser(Browser.browser);
    }

    public static WebDriver getDriver(boolean headless) {
        return switch (browser) {
            case Chrome -> new ChromeDriver(headless ? chromeHeadless : chromeBase);
            case Edge -> new EdgeDriver(headless ? edgeHeadless : edgeBase);
            case Firefox -> new FirefoxDriver(headless ? firefoxHeadless : firefoxBase);
            case Opera -> new SafariDriver(headless ? safariHeadless : safariBase);
        };
    }

    private static final ChromeOptions chromeBase = new ChromeOptions();
    private static final ChromeOptions chromeHeadless = new ChromeOptions();

    private static final EdgeOptions edgeBase = new EdgeOptions();
    private static final EdgeOptions edgeHeadless = new EdgeOptions();

    private static final FirefoxOptions firefoxBase = new FirefoxOptions();
    private static final FirefoxOptions firefoxHeadless = new FirefoxOptions();

    private static final SafariOptions safariBase = new SafariOptions();
    private static final SafariOptions safariHeadless = new SafariOptions();

    static {
        final String baseArguments = "start-minimized --no-sandbox --disable-gpu" +
                "--disable-crash-reporter --disable-extensions --disable-in-process-stack-traces" +
                "--disable-logging --disable-dev-shm-usage --log-level=3 --silent --output=/dev/null";
        chromeBase.addArguments(baseArguments);
        chromeHeadless.addArguments(baseArguments).addArguments("--headless");
    }

    static {
        edgeHeadless.addArguments("headless");
        edgeHeadless.addArguments("disable-gpu");
    }

    static {
        firefoxHeadless.addArguments("-headless");
    }
}
