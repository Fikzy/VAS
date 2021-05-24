package vaf.scrapper;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import vaf.VAF;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ProfileFactory {

    public static List<Vaccine> vaccinesToCheck = Arrays.asList(Vaccine.values());

    private final WebDriver driver;

    public ProfileFactory() {
        this.driver = Browser.getDriver(true);

        Action.refuseCookies().accept(this.driver);
    }

    public void generateProfiles(final List<String> urls) {
        urls.forEach(this::generateProfiles);
    }

    public void generateProfiles(final String url) {

        final List<Action> actions = new ArrayList<>();

        // Load page
        driver.get(url);
        Action.waitForBookingContent(3, 50).accept(driver);

        // Generate list of actions
        addActionIfValid(actions, Action.selectSpeciality());
        addActionIfValid(actions, Action.specifyUnknownDoctor());

        List<VaccineSelection> vaccineSelections = getVaccineSelections();
        generateProfiles(url, actions, vaccineSelections);
    }


    private void generateProfiles(final String url, final List<Action> actions,
                                  final List<VaccineSelection> vaccineSelections) {

        WebElement headerNameElement = driver.findElement(By.className("dl-profile-header-name"));
        WebElement span = headerNameElement.findElement(By.tagName("span"));
        String centerTitle = span.getText();

        vaccineSelections.forEach(selection -> {
            final List<Action> actionList = new ArrayList<>(List.copyOf(actions));
            actionList.add(Action.selectVaccine(selection.selectIndex()));
            final ScannerProfile scannerProfile = new ScannerProfile(url, actionList, centerTitle, selection.vaccine());
            VAF.INSTANCE.addScannerProfile(scannerProfile);
        });
    }

    private void addActionIfValid(final List<Action> actions, final Action action) {
        try {
            action.accept(this.driver);
            actions.add(action);
        } catch (NoSuchElementException ignored) {
        }
    }

    private List<VaccineSelection> getVaccineSelections() {

        WebElement bookingMotiveElement;
        try {
            bookingMotiveElement = driver.findElement(By.id("booking_motive"));
        } catch (NoSuchElementException e) {
            System.err.println("Booking motive not found...");
            return List.of();
        }

        List<String> motives = bookingMotiveElement
                .findElements(By.tagName("option"))
                .stream().map(elt -> elt.getAttribute("value"))
                .collect(Collectors.toList());

        List<VaccineSelection> vaccineSelections = new ArrayList<>();
        for (int i = 0; i < motives.size(); i++) {
            for (Vaccine vaccine : vaccinesToCheck) {
                if (vaccine.pattern.matcher(motives.get(i)).find())
                    vaccineSelections.add(new VaccineSelection(vaccine, i));
            }
        }

        return vaccineSelections;
    }

    public void dispose() {
        driver.quit();
    }
}
