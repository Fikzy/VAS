package vaf.scrapper;

import io.reactivex.rxjava3.processors.PublishProcessor;
import javafx.application.Platform;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import vaf.VAF;
import vaf.app.App;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class CenterSearcher extends Scrapper {

//    private static final Logger logger = LoggerFactory.getLogger(CenterSearcher.class);

    private final PublishProcessor<String> locationsToSearch = PublishProcessor.create();

    public CenterSearcher() {
        super(true);

        locationsToSearch.subscribe(this::search);
    }

    public void submitSearch(final String location) {
        locationsToSearch.onNext(location);
    }

    public boolean search(final String input) {

        final String location = input.replace(' ', '-').toLowerCase(Locale.ROOT);
        VAF.logger.info("Searching location: " + location);

        driver.get("https://www.doctolib.fr/vaccination-covid-19/" + location + "?ref_visit_motive_ids[]=6970&ref_visit_motive_ids[]=7005&force_max_limit=1");

        WebElement resultsContainer;
        try {
            resultsContainer = driver.findElement(By.className("results"));
        } catch (NoSuchElementException ignored) {
            VAF.logger.error("Invalid Doctolib location search");
            Platform.runLater(App.INSTANCE::invalidLocationSearchWarning);
            return false;
        }

        // Elements with id='search-result-*'
        List<WebElement> searchResultsElements = resultsContainer.findElements(By.xpath(".//div[starts-with(@id,'search-result-')]"));

        // Get links from the elements
        List<String> links = searchResultsElements.stream().map(searchResult -> {
            WebElement linkElement = searchResult.findElement(By.xpath(".//a[@data-analytics-event-action=\"linkProfileName\"]"));
            return linkElement.getAttribute("href");
        }).collect(Collectors.toList());

//        links.forEach(System.out::println);
        links.forEach(VAF.INSTANCE.profileFactory::generateProfiles);
//        links.forEach(VAF.INSTANCE.profileFactory::submitUrl);

        VAF.INSTANCE.startScanning();

        return true;
    }
}
