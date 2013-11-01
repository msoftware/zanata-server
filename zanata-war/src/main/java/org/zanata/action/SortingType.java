package org.zanata.action;

import java.io.Serializable;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@EqualsAndHashCode(of = { "displayString" })
public class SortingType implements Serializable {

    public static final SortingType PERCENTAGE = new SortingType("%");

    public static final SortingType HOURS = new SortingType("hours");

    public static final SortingType WORDS = new SortingType("words");

    public static final SortingType ALPHABETICAL = new SortingType(
            "alphabetical");

    @Getter
    private String displayString;

    @Getter
    @Setter
    private boolean descending = true;

    @Getter
    @Setter
    private boolean active = false;

    public SortingType(SortingType sortingType) {
        this.displayString = sortingType.displayString;
        this.descending = sortingType.descending;
    }

    private SortingType(String displayString) {
        this.displayString = displayString;
    }

    public static SortingType getPercentageType() {
        return new SortingType(PERCENTAGE);
    }

    public static SortingType getHoursType() {
        return new SortingType(HOURS);
    }

    public static SortingType getWordsType() {
        return new SortingType(WORDS);
    }

    public static SortingType getAlphabeticalType() {
        return new SortingType(ALPHABETICAL);
    }
}
