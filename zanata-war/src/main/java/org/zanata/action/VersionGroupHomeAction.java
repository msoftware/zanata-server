/*
 * Copyright 2012, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import org.hibernate.Session;
import org.hibernate.criterion.NaturalIdentifier;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.annotation.CachedMethodResult;
import org.zanata.common.LocaleId;
import org.zanata.common.statistic.WordStatistic;
import org.zanata.model.HIterationGroup;
import org.zanata.model.HLocale;
import org.zanata.model.HProjectIteration;
import org.zanata.service.GroupStatisticService;
import org.zanata.service.LocaleService;
import org.zanata.service.VersionLocaleKey;
import org.zanata.util.StatisticsUtil;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */

@Name("versionGroupHomeAction")
@Scope(ScopeType.PAGE)
public class VersionGroupHomeAction extends SlugHome<HIterationGroup> {
    private static final long serialVersionUID = 1L;

    @Getter
    @Setter
    private String slug;

    @Getter
    @Setter
    private boolean pageRendered = false;

    @In
    private LocaleService localeServiceImpl;

    @In
    private GroupStatisticService groupStatisticServiceImpl;

    private OverallStatistics overallStatistics;

    private List<LocaleItem> activeLocales;

    private Map<VersionLocaleKey, WordStatistic> statisticMap;

    @Override
    protected HIterationGroup loadInstance() {
        Session session = (Session) getEntityManager().getDelegate();
        return (HIterationGroup) session.byNaturalId(HIterationGroup.class)
                .using("slug", getSlug()).load();
    }

    public List<HProjectIteration> getInstanceProjectIterations() {
        return new ArrayList<HProjectIteration>(getInstance()
                .getProjectIterations());
    }

    @Override
    public NaturalIdentifier getNaturalId() {
        return Restrictions.naturalId().set("slug", slug);
    }

    @Override
    public boolean isIdDefined() {
        return slug != null;
    }

    @Override
    public Object getId() {
        return slug;
    }

    public void validateSuppliedId() {
        getInstance(); // this will raise an EntityNotFound exception
        // when id is invalid and conversation will not
        // start
    }

    public List<LocaleItem> getActiveLocales() {
        if (activeLocales == null) {
            activeLocales = Lists.newArrayList();

            List<HLocale> supportedLocales =
                    localeServiceImpl.getSupportedLocales();
            Set<HLocale> groupActiveLocales =
                    localeServiceImpl.getGroupActiveLocales(getInstance()
                            .getSlug());

            for (HLocale locale : supportedLocales) {
                if (groupActiveLocales.contains(locale)) {
                    activeLocales.add(new LocaleItem(true, locale));
                }
            }
        }
        Collections.sort(activeLocales);
        return activeLocales;
    }

    @CachedMethodResult
    public WordStatistic getStatisticForLocale(LocaleId localeId) {
        WordStatistic statistic = new WordStatistic();
        for (Map.Entry<VersionLocaleKey, WordStatistic> entry : getStatisticMap()
                .entrySet()) {
            if (entry.getKey().getLocaleId().equals(localeId)) {
                statistic.add(entry.getValue());
            }
        }
        return statistic;
    }

    private int getTotalWordsCountForGroup() {
        int total = 0;
        for (Map.Entry<VersionLocaleKey, WordStatistic> entry : getStatisticMap()
                .entrySet()) {
            total += entry.getValue().getTotal();
        }
        return total;
    }

    @CachedMethodResult
    public OverallStatistics getOverallStatistic() {
        if (overallStatistics == null) {

            WordStatistic wordStatistic = new WordStatistic();
            for (Map.Entry<VersionLocaleKey, WordStatistic> entry : getStatisticMap()
                    .entrySet()) {
                wordStatistic.add(entry.getValue());
            }
            wordStatistic.setRemainingHours(StatisticsUtil
                    .getRemainingHours(wordStatistic));

            int totalWordCount = getTotalWordsCountForGroup();
            int totalMessageCount = groupStatisticServiceImpl.getTotalMessageCount(slug);

            overallStatistics =
                    new OverallStatistics(totalWordCount, totalMessageCount,
                            wordStatistic);

        }
        return overallStatistics;
    }

    /**
     * Load up statistics for all project versions in all active locales in the
     * group
     *
     * @return
     */
    private Map<VersionLocaleKey, WordStatistic> getStatisticMap() {
        if (statisticMap == null) {
            statisticMap = Maps.newHashMap();

            for (LocaleItem localeItem : getActiveLocales()) {
                statisticMap.putAll(groupStatisticServiceImpl
                        .getLocaleStatistic(slug, localeItem.getLocale()
                                .getLocaleId()));
            }
        }
        return statisticMap;
    }

    @AllArgsConstructor
    @Getter
    @Setter
    public final class OverallStatistics {
        int totalWordCount;
        int totalMessageCount;
        WordStatistic statistic;
    }
}
