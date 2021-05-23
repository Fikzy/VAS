package vaf.scrapper;

public record ScannerInstance(String centerTitle, VaccineSelection selection) {

    @Override
    public String toString() {
        return String.format("ScannerInstance: %s | %s", selection.vaccine(), centerTitle);
    }
}

//public class ScannerInstance {
//
//    public final String centerTitle;
//    public final VaccineSelection selection;
//
//    public ScannerInstance(String centerTitle, VaccineSelection selection) {
//        this.centerTitle = centerTitle;
//        this.selection = selection;
//    }
//}
