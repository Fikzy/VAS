package vaf.scrapper;

import java.util.List;

public record ScannerProfile(String url, List<Action> actions, String centerTitle, Vaccine selectedVaccine) {

    @Override
    public String toString() {
        return String.format("%s | %s", centerTitle, selectedVaccine);
    }
}
