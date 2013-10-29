package org.zanata.service;

import java.util.Map;

import org.zanata.common.LocaleId;
import org.zanata.common.statistic.WordStatistic;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public interface GroupStatisticService {

    WordStatistic getVersionLocaleStatistic(VersionLocaleKey key);

    Map<VersionLocaleKey, WordStatistic> getLocaleStatistic(String slug,
            LocaleId localeId);

    int getTotalMessageCount(String slug);
}
