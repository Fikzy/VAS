package vaf.scrapper;

import io.reactivex.rxjava3.processors.PublishProcessor;
import org.openqa.selenium.WebDriver;

public class Driver {

    public final WebDriver driver;

    private final PublishProcessor<ActionSequence> actions = PublishProcessor.create();

    public Driver(boolean headless) {
        this.driver = Browser.getDriver(headless);
        Action.refuseCookies().accept(driver);
        actions.subscribe(this::executeSequence);
    }

    public void submit(final ActionSequence actionSequence) {
        actions.onNext(actionSequence);
    }

    private void executeSequence(final ActionSequence actionSequence) {
        actionSequence.actions().forEach(action -> action.accept(driver));
    }

    public void dispose() {
        driver.quit();
    }
}
