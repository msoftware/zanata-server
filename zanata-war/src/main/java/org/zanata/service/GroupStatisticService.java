package org.zanata.service;

import java.util.List;
import java.util.Map;

import org.zanata.common.LocaleId;
import org.zanata.common.statistic.WordsStatistic;
import org.zanata.model.HProjectIteration;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public interface GroupStatisticService {

    WordsStatistic getVersionLocaleStatistic(VersionLocaleKey key);

    Map<VersionLocaleKey, WordsStatistic> getLocaleStatistic(
        List<HProjectIteration> versionList,
        LocaleId localeId);
}
