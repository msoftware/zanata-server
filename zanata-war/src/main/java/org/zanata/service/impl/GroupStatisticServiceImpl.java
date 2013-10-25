package org.zanata.service.impl;

import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.common.LocaleId;
import org.zanata.common.statistic.WordsStatistic;
import org.zanata.model.HProjectIteration;
import org.zanata.service.GroupStatisticService;
import org.zanata.service.VersionLocaleKey;
import org.zanata.service.VersionStateCache;
import org.zanata.util.StatisticsUtil;

import com.google.common.collect.Maps;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Name("groupStatisticServiceImpl")
@Scope(ScopeType.STATELESS)
public class GroupStatisticServiceImpl implements GroupStatisticService {

    @In
    private VersionStateCache versionStateCacheImpl;

    @Override
    public WordsStatistic getVersionLocaleStatistic(VersionLocaleKey key) {
        return versionStateCacheImpl.getVersionStatistic(key.getVersionId(),
                key.getLocaleId());
    }

    @Override
    public Map<VersionLocaleKey, WordsStatistic> getLocaleStatistic(
            List<HProjectIteration> versionList, LocaleId localeId) {

        Map<VersionLocaleKey, WordsStatistic> statisticMap = Maps.newHashMap();

        for (HProjectIteration version : versionList) {
            VersionLocaleKey key =
                    new VersionLocaleKey(version.getId(), localeId);

            WordsStatistic statistic = getVersionLocaleStatistic(key);
            statistic.setRemainingHours(StatisticsUtil
                    .getRemainingHours(statistic));

            statisticMap.put(key, statistic);
        }

        return statisticMap;
    }
}
