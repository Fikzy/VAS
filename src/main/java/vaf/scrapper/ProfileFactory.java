package vaf.scrapper;

import org.openqa.selenium.*;
import vaf.VAF;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProfileFactory extends Scrapper {

//    private final PublishProcessor<String> urlsToGenerateFrom = PublishProcessor.create();

    public ProfileFactory() {
        super(true);

//        urlsToGenerateFrom.subscribe(this::generateProfiles);
    }

//    public void submitUrl(final String url) {
//        urlsToGenerateFrom.onNext(url);
//    }

    public boolean generateProfiles(final String url) {

        final List<Action> actions = new ArrayList<>();

        // Load page
        try {
            driver.get(url);
            Action.waitForBookingContent(3, 50).accept(driver);
        } catch (InvalidArgumentException | TimeoutException ignored) {
            System.err.println("Invalid center url");
            return false;
        }

        // Generate list of actions
        addActionIfValid(actions, Action.selectSpeciality());
        addActionIfValid(actions, Action.specifyUnknownDoctor());

        List<VaccineSelection> vaccineSelections = getVaccineSelections();
        generateProfiles(url, actions, vaccineSelections);

        return true;
    }

    private void generateProfiles(final String url, final List<Action> actions,
                                  final List<VaccineSelection> vaccineSelections) {

        WebElement headerNameElement = driver.findElement(By.className("dl-profile-header-name"));
        WebElement span = headerNameElement.findElement(By.tagName("span"));
        String centerTitle = span.getText();

        vaccineSelections.forEach(selection -> {
            final List<Action> actionList = new ArrayList<>(List.copyOf(actions));
            actionList.add(Action.selectVaccine(selection.selectIndex()));
            final ScannerProfile scannerProfile = new ScannerProfile(url, actionList, centerTitle, selection.vaccine(),
                    VAF.INSTANCE.searchFromTime, VAF.INSTANCE.searchToTime);
            VAF.INSTANCE.addScannerProfile(scannerProfile);
        });
    }

    private void addActionIfValid(final List<Action> actions, final Action action) {
        try {
            action.accept(this.driver);
            actions.add(action);
        } catch (NoSuchElementException | ClassCastException ignored) {
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
            for (Vaccine vaccine : VAF.INSTANCE.searchedVaccines) {
                if (vaccine.pattern.matcher(motives.get(i)).find())
                    vaccineSelections.add(new VaccineSelection(vaccine, i));
            }
        }

        return vaccineSelections;
    }
}
