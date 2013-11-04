package org.zanata.action;

import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import com.google.common.collect.Lists;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class SortingType implements Serializable {

    public static final String PERCENTAGE = "%";

    public static final String HOURS = "hours";

    public static final String WORDS = "words";

    public static final String ALPHABETICAL = "alphabetical";

    @Getter
    @Setter
    private boolean descending = true;

    @Getter
    private String selectedSortOption = ALPHABETICAL;

    @Getter
    private List<String> sortOptions = Lists.newArrayList();

    public SortingType(List<String> sortOptions) {
        this.sortOptions = sortOptions;
    }

    public void setSelectedSortOption(String selectedSortOption) {
        if (this.selectedSortOption.equals(selectedSortOption)) {
            descending = !descending;
        }
        this.selectedSortOption = selectedSortOption;
    }
}
