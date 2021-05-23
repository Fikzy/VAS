package vaf.scrapper;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Scrapper {

    public final String url;
    public WebDriver driver;
    public final List<Action> actions;

    public Scrapper(final String url, final List<Action> actions) {
        this.url = url;
        this.driver = new ChromeDriver(getOptions());
        this.driver.get(url);
        this.actions = new ArrayList<>();
        Action.refuseCookies().accept(this);
        this.actions.addAll(actions);
    }

    public Scrapper(final String url) {
        this(url, Collections.emptyList());
    }

    protected abstract ChromeOptions getOptions();

    public void refresh() {
        driver.get(url);
    }

    public void applyActions() {
        for (Action action : actions)
            action.accept(this);
    }

    public boolean validAction(final Action action) {
        try {
            action.accept(this);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public void addActionIfValid(final Action action) {
        if (validAction(action))
            actions.add(action);
    }
}
