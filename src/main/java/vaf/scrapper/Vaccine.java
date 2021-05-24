package vaf.scrapper;

import java.util.regex.Pattern;

public enum Vaccine {
    Pfizer("1re.*pfizer"),
    Moderna("1re.*moderna");

    public final Pattern pattern;

    Vaccine(final String regex) {
        this.pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }
}
