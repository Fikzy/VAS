package vaf.scrapper;

import java.util.regex.Pattern;

public enum Vaccine {
    Pfizer("1re.*pfizer"),
    Moderna("1re.*moderna");

    Vaccine(final String regex) {
        this.pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    public final Pattern pattern;
}
