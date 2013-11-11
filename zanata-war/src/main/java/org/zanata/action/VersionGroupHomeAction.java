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

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.zanata.annotation.CachedMethodResult;
import org.zanata.common.LocaleId;
import org.zanata.common.statistic.WordStatistic;
import org.zanata.dao.VersionGroupDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.model.HProjectIteration;
import org.zanata.service.GroupStatisticService;
import org.zanata.service.LocaleService;
import org.zanata.service.VersionLocaleKey;
import org.zanata.util.StatisticsUtil;
import org.zanata.util.ZanataMessages;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */

@Name("versionGroupHomeAction")
@Scope(ScopeType.PAGE)
public class VersionGroupHomeAction implements Serializable {
    private static final long serialVersionUID = 1L;

    @In
    private LocaleService localeServiceImpl;

    @In
    private GroupStatisticService groupStatisticServiceImpl;

    @In
    private ZanataMessages zanataMessages;

    @In(required = false, value = JpaIdentityStore.AUTHENTICATED_USER)
    private HAccount authenticatedAccount;

    @In
    private VersionGroupDAO versionGroupDAO;

    @Getter
    @Setter
    private String slug;

    @Getter
    private boolean pageRendered = false;

    @Getter
    @Setter
    private HLocale selectedLocale;

    @Getter
    @Setter
    private HProjectIteration selectedVersion;

    @Getter
    private OverallStatistic overallStatistic;

    private List<HLocale> activeLocales;

    private List<HProjectIteration> projectIterations;

    private Map<VersionLocaleKey, WordStatistic> statisticMap;

    private SortingType languageSortingList;

    private SortingType projectSortingList;

    private final LanguageComparator languageComparator =
            new LanguageComparator(getLanguageSortingList());

    private final VersionComparator versionComparator = new VersionComparator(
            getProjectSortingList());

    public void setPageRendered(boolean pageRendered) {
        if (pageRendered) {
            loadStatistic();
        }
        this.pageRendered = pageRendered;
    }

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
                        statisticMap.get(new VersionLocaleKey(item1.getId(),
                                selectedLocale.getLocaleId()));
                WordStatistic wordStatistic2 =
                        statisticMap.get(new VersionLocaleKey(item2.getId(),
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

    public boolean isUserProjectMaintainer() {
        return authenticatedAccount != null
                && authenticatedAccount.getPerson().isMaintainerOfProjects();
    }

    public void sortLanguageList() {
        Collections.sort(activeLocales, languageComparator);
    }

    public void sortProjectList() {
        Collections.sort(projectIterations, versionComparator);
    }

    public List<HLocale> getActiveLocales() {
        if (activeLocales == null) {
            Set<HLocale> groupActiveLocales =
                    localeServiceImpl.getGroupActiveLocales(getSlug());
            activeLocales = Lists.newArrayList(groupActiveLocales);
        }
        Collections.sort(activeLocales, languageComparator);
        return activeLocales;
    }

    @CachedMethodResult
    public String getStatisticFigureForLocale(String sortOption,
            LocaleId localeId) {
        WordStatistic statistic = getStatisticForLocale(localeId);

        return getStatisticFigure(sortOption, statistic);
    }

    @CachedMethodResult
    public String getStatisticFigureForProjectWithLocale(String sortOption,
            LocaleId localeId, Long versionId) {
        WordStatistic statistic =
                statisticMap.get(new VersionLocaleKey(versionId, localeId));

        return getStatisticFigure(sortOption, statistic);
    }

    @CachedMethodResult
    public String
            getStatisticFigureForProject(String sortOption, Long versionId) {
        WordStatistic statistic = getStatisticForProject(versionId);

        return getStatisticFigure(sortOption, statistic);
    }

    private String
            getStatisticFigure(String sortOption, WordStatistic statistic) {
        if (sortOption.equals(SortingType.HOURS)) {
            return StatisticsUtil.formatHours(statistic.getRemainingHours());
        } else if (sortOption.equals(SortingType.WORDS)) {
            return String.valueOf(statistic.getTotal());
        } else {
            return StatisticsUtil.formatPercentage(statistic
                    .getPercentTranslated()) + "%";
        }
    }

    public String getStatisticUnit(String sortOption) {
        if (sortOption.equals(SortingType.HOURS)) {
            return zanataMessages.getMessage("jsf.stats.HoursRemaining");
        } else if (sortOption.equals(SortingType.WORDS)) {
            return zanataMessages.getMessage("jsf.Words");
        } else {
            return zanataMessages.getMessage("jsf.Translated");
        }
    }

    @CachedMethodResult
    public WordStatistic getStatisticForLocale(LocaleId localeId) {
        WordStatistic statistic = new WordStatistic();
        for (Map.Entry<VersionLocaleKey, WordStatistic> entry : statisticMap
                .entrySet()) {
            if (entry.getKey().getLocaleId().equals(localeId)) {
                statistic.add(entry.getValue());
            }
        }
        statistic
                .setRemainingHours(StatisticsUtil.getRemainingHours(statistic));
        return statistic;
    }

    @CachedMethodResult
    public WordStatistic getStatisticForProject(Long versionId) {
        WordStatistic statistic = new WordStatistic();
        for (Map.Entry<VersionLocaleKey, WordStatistic> entry : statisticMap
                .entrySet()) {
            if (entry.getKey().getVersionId().equals(versionId)) {
                statistic.add(entry.getValue());
            }
        }
        statistic
                .setRemainingHours(StatisticsUtil.getRemainingHours(statistic));
        return statistic;
    }

    @CachedMethodResult
    public WordStatistic getSelectedLocaleStatistic(Long versionId) {
        return statisticMap.get(new VersionLocaleKey(versionId, selectedLocale
                .getLocaleId()));
    }

    @CachedMethodResult
    public WordStatistic getSelectedVersionStatistic(LocaleId localeId) {
        return statisticMap.get(new VersionLocaleKey(selectedVersion.getId(),
                localeId));
    }

    public List<HPerson> getMaintainers() {
        return versionGroupDAO.getMaintainerBySlug(getSlug());
    }

    /**
     * Load up statistics for all project versions in all active locales in the
     * group.
     */
    private void loadStatistic() {
        statisticMap = Maps.newHashMap();

        for (HLocale locale : getActiveLocales()) {
            statisticMap.putAll(groupStatisticServiceImpl.getLocaleStatistic(
                    getSlug(), locale.getLocaleId()));
        }

        WordStatistic overallWordStatistic = new WordStatistic();
        int totalWordCount = 0;
        for (Map.Entry<VersionLocaleKey, WordStatistic> entry : statisticMap
                .entrySet()) {
            overallWordStatistic.add(entry.getValue());
            totalWordCount += entry.getValue().getTotal();
        }
        overallWordStatistic.setRemainingHours(StatisticsUtil
                .getRemainingHours(overallWordStatistic));

        int totalMessageCount =
                groupStatisticServiceImpl.getTotalMessageCount(getSlug());

        overallStatistic =
                new OverallStatistic(totalWordCount, totalMessageCount,
                        overallWordStatistic);
    }

    @AllArgsConstructor
    @Getter
    @Setter
    public final class OverallStatistic {
        private int totalWordCount;
        private int totalMessageCount;
        private WordStatistic statistic;
    }

    public List<HProjectIteration> getProjectIterations() {
        if (projectIterations == null) {
            projectIterations = versionGroupDAO.getVersionsBySlug(slug);
        }
        Collections.sort(projectIterations, versionComparator);
        return projectIterations;
    }

    // reset all page cached statistics
    public void resetPageData() {
        projectIterations = null;
        activeLocales = null;
        loadStatistic();
    }
}
