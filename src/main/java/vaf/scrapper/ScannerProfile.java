package vaf.scrapper;

import java.time.LocalTime;
import java.util.List;

public record ScannerProfile(String url, List<Action> actions, String centerTitle, Vaccine selectedVaccine,
                             LocalTime fromTime, LocalTime toTime) {

    @Override
    public String toString() {
        return String.format("%s | %s | %s - %s", centerTitle, selectedVaccine, fromTime, toTime);
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
