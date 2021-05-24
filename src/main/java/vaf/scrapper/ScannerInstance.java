package vaf.scrapper;

// TODO: Remove
public record ScannerInstance(String centerTitle, VaccineSelection selection) {

    @Override
    public String toString() {
        return String.format("ScannerInstance: %s | %s", selection.vaccine(), centerTitle);
    }
}
