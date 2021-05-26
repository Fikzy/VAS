package vaf.scrapper;

import java.util.regex.Pattern;

public enum Vaccine {
    Pfizer(1, "1re.*pfizer"),
    Moderna(1, "1re.*moderna");

    public final int value;
    public final Pattern pattern;

    Vaccine(int value, final String regex) {
        this.value = value;
        this.pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }
}
