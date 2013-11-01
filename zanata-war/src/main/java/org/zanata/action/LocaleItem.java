package org.zanata.action;

import java.util.Comparator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import org.zanata.model.HLocale;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@AllArgsConstructor
public class LocaleItem {
    @Getter
    @Setter
    private boolean selected;

    @Getter
    @Setter
    private HLocale locale;

    public static final Comparator<LocaleItem> Comparator =
            new Comparator<LocaleItem>() {
                @Override
                public int
                        compare(LocaleItem localeItem, LocaleItem localeItem2) {
                    return localeItem
                            .getLocale()
                            .retrieveDisplayName()
                            .compareTo(
                                    localeItem2.getLocale()
                                            .retrieveDisplayName());
                }
            };
}
