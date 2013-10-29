package org.zanata.service.impl;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.common.LocaleId;
import org.zanata.common.statistic.WordStatistic;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.dao.VersionGroupDAO;
import org.zanata.model.HIterationGroup;
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

    @In
    private VersionGroupDAO versionGroupDAO;

    @In
    private ProjectIterationDAO projectIterationDAO;

    @Override
    public WordStatistic getVersionLocaleStatistic(VersionLocaleKey key) {
        return versionStateCacheImpl.getVersionStatistic(key.getVersionId(),
                key.getLocaleId());
    }

    @Override
    public Map<VersionLocaleKey, WordStatistic> getLocaleStatistic(String slug,
            LocaleId localeId) {

        Map<VersionLocaleKey, WordStatistic> statisticMap = Maps.newHashMap();
        HIterationGroup group = versionGroupDAO.getBySlug(slug);

        for (HProjectIteration version : group.getProjectIterations()) {
            VersionLocaleKey key =
                    new VersionLocaleKey(version.getId(), localeId);

            WordStatistic statistic = getVersionLocaleStatistic(key);
            statistic.setRemainingHours(StatisticsUtil
                    .getRemainingHours(statistic));

            statisticMap.put(key, statistic);
        }

        return statisticMap;
    }

    @Override
    public int getTotalMessageCount(String slug) {
        int result = 0;
        HIterationGroup group = versionGroupDAO.getBySlug(slug);
        for (HProjectIteration version : group.getProjectIterations()) {
            result +=
                    projectIterationDAO.getTotalMessageCountForIteration(
                            version.getId()).intValue();
        }
        result = result * group.getActiveLocales().size();
        return result;
    }
}
