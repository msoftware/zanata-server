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
import java.util.Comparator;
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
import org.jboss.seam.security.management.JpaIdentityStore;
import org.zanata.annotation.CachedMethodResult;
import org.zanata.common.LocaleId;
import org.zanata.common.statistic.WordStatistic;
import org.zanata.model.HAccount;
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

    @Getter
    @Setter
    private HLocale selectedLocale;

    @In(required = false, value = JpaIdentityStore.AUTHENTICATED_USER)
    HAccount authenticatedAccount;

    @In
    private LocaleService localeServiceImpl;

    @In
    private GroupStatisticService groupStatisticServiceImpl;

    private OverallStatistics overallStatistics;

    private List<HLocale> activeLocales;

    private List<HProjectIteration> projectIterations;

    private Map<VersionLocaleKey, WordStatistic> statisticMap;

    private SortingType languageSortingList;

    private SortingType projectSortingList;

    private final LanguageComparator languageComparator =
            new LanguageComparator(getLanguageSortingList());

    private final VersionComparator versionComparator = new VersionComparator(
            getProjectSortingList());

    public SortingType getLanguageSortingList() {
        if (languageSortingList == null) {
            languageSortingList =
                    new SortingType(Lists.newArrayList(
                            SortingType.ALPHABETICAL, SortingType.HOURS,
                            SortingType.PERCENTAGE));
        }
        return languageSortingList;
    }

    public SortingType getProjectSortingList() {
        if (projectSortingList == null) {
            projectSortingList =
                    new SortingType(Lists.newArrayList(
                            SortingType.ALPHABETICAL, SortingType.HOURS,
                            SortingType.PERCENTAGE, SortingType.WORDS));
        }
        return projectSortingList;
    }

    private class LanguageComparator implements Comparator<HLocale> {
        private SortingType sortingType;

        public LanguageComparator(SortingType sortingType) {
            this.sortingType = sortingType;
        }

        @Override
        public int compare(HLocale locale, HLocale locale2) {
            final HLocale item1, item2;

            if (sortingType.isDescending()) {
                item1 = locale;
                item2 = locale2;
            } else {
                item1 = locale2;
                item2 = locale;
            }

            String selectedSortOption = sortingType.getSelectedSortOption();
            // Need to get statistic for comparison
            if (!selectedSortOption.equals(SortingType.ALPHABETICAL)) {
                WordStatistic wordStatistic1 =
                        getStatisticForLocale(item1.getLocaleId());
                WordStatistic wordStatistic2 =
                        getStatisticForLocale(item2.getLocaleId());

                if (selectedSortOption.equals(SortingType.PERCENTAGE)) {
                    return Double.compare(
                            wordStatistic1.getPercentTranslated(),
                            wordStatistic2.getPercentTranslated());
                } else if (selectedSortOption.equals(SortingType.HOURS)) {
                    return Double.compare(wordStatistic1.getRemainingHours(),
                            wordStatistic2.getRemainingHours());
                }
            } else {
                return item1.retrieveDisplayName().compareTo(
                        item2.retrieveDisplayName());
            }
            return 0;
        }
    }

    private class VersionComparator implements Comparator<HProjectIteration> {
        private SortingType sortingType;

        public VersionComparator(SortingType sortingType) {
            this.sortingType = sortingType;
        }

        @Override
        public int compare(HProjectIteration locale, HProjectIteration locale2) {
            final HProjectIteration item1, item2;

            if (sortingType.isDescending()) {
                item1 = locale;
                item2 = locale2;
            } else {
                item1 = locale2;
                item2 = locale;
            }

            String selectedSortOption = sortingType.getSelectedSortOption();
            // Need to get statistic for comparison
            if (!selectedSortOption.equals(SortingType.ALPHABETICAL)) {
                WordStatistic wordStatistic1 =
                        getStatisticMap().get(
                                new VersionLocaleKey(item1.getId(),
                                        selectedLocale.getLocaleId()));
                WordStatistic wordStatistic2 =
                        getStatisticMap().get(
                                new VersionLocaleKey(item2.getId(),
                                        selectedLocale.getLocaleId()));

                if (selectedSortOption.equals(SortingType.PERCENTAGE)) {
                    return Double.compare(
                            wordStatistic1.getPercentTranslated(),
                            wordStatistic2.getPercentTranslated());
                } else if (selectedSortOption.equals(SortingType.HOURS)) {
                    return Double.compare(wordStatistic1.getRemainingHours(),
                            wordStatistic2.getRemainingHours());
                } else if (selectedSortOption.equals(SortingType.WORDS)) {
                    return Integer.compare(wordStatistic1.getTotal(),
                            wordStatistic2.getTotal());
                }
            } else {
                return item1.getProject().getName()
                        .compareTo(item2.getProject().getName());
            }
            return 0;
        }
    }

    public void sortLanguageList(String sortOption) {
        if (getLanguageSortingList().getSelectedSortOption().equals(sortOption)) {
            getLanguageSortingList().setDescending(
                    !getLanguageSortingList().isDescending());
        }
        getLanguageSortingList().setSelectedSortOption(sortOption);
        Collections.sort(activeLocales, languageComparator);
    }

    public void sortProjectList(String sortOption) {
        if (getProjectSortingList().getSelectedSortOption().equals(sortOption)) {
            getProjectSortingList().setDescending(
                    !getProjectSortingList().isDescending());
        }
        getProjectSortingList().setSelectedSortOption(sortOption);
        Collections.sort(projectIterations, versionComparator);
    }

    public List<HLocale> getActiveLocales() {
        if (activeLocales == null) {
            Set<HLocale> groupActiveLocales =
                    localeServiceImpl.getGroupActiveLocales(getInstance()
                            .getSlug());
            activeLocales = Lists.newArrayList(groupActiveLocales);
        }
        Collections.sort(activeLocales, languageComparator);
        return activeLocales;
    }

    @CachedMethodResult
    public String getTranslatedPercentage(LocaleId localeId) {
        WordStatistic statistic = getStatisticForLocale(localeId);
        return StatisticsUtil
                .formatPercentage(statistic.getPercentTranslated());
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
        statistic
                .setRemainingHours(StatisticsUtil.getRemainingHours(statistic));
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
            int totalMessageCount =
                    groupStatisticServiceImpl.getTotalMessageCount(getSlug());

            overallStatistics =
                    new OverallStatistics(totalWordCount, totalMessageCount,
                            wordStatistic);

        }
        return overallStatistics;
    }

    public boolean isUserProjectMaintainer() {
        return authenticatedAccount != null
                && authenticatedAccount.getPerson().isMaintainerOfProjects();
    }

    public WordStatistic getStatisticForSelectedLocale(Long versionId) {
        return getStatisticMap().get(
                new VersionLocaleKey(versionId, selectedLocale.getLocaleId()));
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

            for (HLocale locale : getActiveLocales()) {
                statisticMap.putAll(groupStatisticServiceImpl
                        .getLocaleStatistic(getSlug(), locale.getLocaleId()));
            }
        }
        return statisticMap;
    }

    @AllArgsConstructor
    @Getter
    @Setter
    public final class OverallStatistics {
        private int totalWordCount;
        private int totalMessageCount;
        private WordStatistic statistic;
    }

    @Override
    protected HIterationGroup loadInstance() {
        Session session = (Session) getEntityManager().getDelegate();
        return (HIterationGroup) session.byNaturalId(HIterationGroup.class)
                .using("slug", getSlug()).load();
    }

    public List<HProjectIteration> getProjectIterations() {
        if (projectIterations == null) {
            projectIterations =
                    new ArrayList<HProjectIteration>(getInstance()
                            .getProjectIterations());
        }
        Collections.sort(projectIterations, versionComparator);
        return projectIterations;
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
}
