package org.zanata.service.impl;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.common.LocaleId;
import org.zanata.common.statistic.WordsStatistic;
import org.zanata.model.HProjectIteration;
import org.zanata.service.GroupStatisticService;
import org.zanata.service.VersionStateCache;
import org.zanata.util.StatisticsUtil;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Name("groupStatisticServiceImpl")
@Scope(ScopeType.STATELESS)
public class GroupStatisticServiceImpl implements GroupStatisticService {

    @In
    private VersionStateCache versionStateCacheImpl;

    @Override
    public WordsStatistic getVersionLocaleStatistic(Long versionId,
            LocaleId localeId) {
        return versionStateCacheImpl.getVersionStatistic(versionId, localeId);
    }

    @Override
    public WordsStatistic getLocaleStatistic(
            List<HProjectIteration> versionList, LocaleId localeId) {
        WordsStatistic statistic = new WordsStatistic();

        for (HProjectIteration version : versionList) {
            statistic.add(getVersionLocaleStatistic(version.getId(), localeId));
        }
        statistic
                .setRemainingHours(StatisticsUtil.getRemainingHours(statistic));
        return statistic;
    }

}
