package vaf.scrapper;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import vaf.VAF;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AppointmentSetupScanner extends Scrapper {

    public AppointmentSetupScanner(final String url) {
        super(url);
        System.out.println("AppointmentSetupScanner");
        setupActions();
        List<VaccineSelection> selections = getVaccineSelections();
        instanciateScanners(selections);
        this.driver.quit();
    }

    @Override
    protected ChromeOptions getOptions() {
        return VAF.headlessOptions;
    }

    private void setupActions() {
        addActionIfValid(Action.selectSpeciality());
        addActionIfValid(Action.specifyUnknownDoctor());
    }

    private void addVaccineSelection(final List<VaccineSelection> vaccineSelectionList, final String motive, final Vaccine vaccine, final int selectIndex) {
        if (vaccine.pattern.matcher(motive).find())
            vaccineSelectionList.add(new VaccineSelection(vaccine, selectIndex));
    }

    private List<VaccineSelection> getVaccineSelections() {

        WebElement bookingMotiveElement = driver.findElement(By.id("booking_motive"));
        List<String> motives = bookingMotiveElement
                .findElements(By.tagName("option"))
                .stream().map(elt -> elt.getAttribute("value"))
                .collect(Collectors.toList());

        List<VaccineSelection> vaccineSelections = new ArrayList<>();
        for (int i = 0; i < motives.size(); i++) {
            addVaccineSelection(vaccineSelections, motives.get(i), Vaccine.Pfizer, i);
            addVaccineSelection(vaccineSelections, motives.get(i), Vaccine.Moderna, i);
        }
        return vaccineSelections;
    }

    private void instanciateScanners(List<VaccineSelection> selections) {

        WebElement headerNameElement = driver.findElement(By.className("dl-profile-header-name"));
        WebElement span = headerNameElement.findElement(By.tagName("span"));
        String centerTitle = span.getText();

        selections.forEach(selection -> {
            final ScannerInstance instance = new ScannerInstance(centerTitle, selection);
            VAF.INSTANCE.instantiateScanner(() -> new AppointmentScanner(url, actions, instance));
        });
    }
}
