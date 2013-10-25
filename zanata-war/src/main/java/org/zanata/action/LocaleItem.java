package org.zanata.action;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import org.zanata.model.HLocale;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@AllArgsConstructor
public class LocaleItem implements Comparable<LocaleItem> {
    @Getter
    @Setter
    private boolean selected;

    @Getter
    @Setter
    private HLocale locale;

    @Override
    public int compareTo(LocaleItem localeItem) {
        return this.getLocale().retrieveDisplayName()
                .compareTo(localeItem.getLocale().retrieveDisplayName());
    }
}
