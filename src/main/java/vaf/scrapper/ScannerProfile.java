package vaf.scrapper;

import java.util.List;

public record ScannerProfile(String url, List<Action> actions, String centerTitle, Vaccine selectedVaccine) {

    @Override
    public String toString() {
        return String.format("%s | %s", centerTitle, selectedVaccine);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ScannerProfile profile)
            return this.url.equals(profile.url) && this.selectedVaccine.equals(profile.selectedVaccine);
        return false;
    }

    @Override
    public int hashCode() {
        return url.hashCode() + selectedVaccine.value;
    }
}
