package org.zanata.service;

import java.util.List;

import org.zanata.common.LocaleId;
import org.zanata.common.statistic.WordsStatistic;
import org.zanata.model.HProjectIteration;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public interface GroupStatisticService {

    WordsStatistic getVersionLocaleStatistic(Long versionId, LocaleId localeId);

    WordsStatistic getLocaleStatistic(List<HProjectIteration> versionList,
            LocaleId localeId);
}
