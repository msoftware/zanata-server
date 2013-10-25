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

import lombok.Getter;
import lombok.Setter;

import org.hibernate.Session;
import org.hibernate.criterion.NaturalIdentifier;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.common.LocaleId;
import org.zanata.common.statistic.WordsStatistic;
import org.zanata.model.HIterationGroup;
import org.zanata.model.HLocale;
import org.zanata.model.HProjectIteration;
import org.zanata.service.GroupStatisticService;
import org.zanata.service.LocaleService;
import org.zanata.service.VersionLocaleKey;

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
    private boolean documentLoaded;

    @In
    private LocaleService localeServiceImpl;

    @In
    private GroupStatisticService groupStatisticServiceImpl;

    private List<LocaleItem> activeLocales;

    private Map<VersionLocaleKey, WordsStatistic> statisticMap;

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

    public WordsStatistic getStatisticForLocale(LocaleId localeId) {
        WordsStatistic statistic = new WordsStatistic();
        for (Map.Entry<VersionLocaleKey, WordsStatistic> entry : getStatisticMap()
                .entrySet()) {
            if (entry.getKey().getLocaleId().equals(localeId)) {
                statistic.add(entry.getValue());
            }
        }

        return statistic;
    }

    public String getOverallTranslatedPercentage() {
        WordsStatistic statistic = new WordsStatistic();
        for (Map.Entry<VersionLocaleKey, WordsStatistic> entry : getStatisticMap()
                .entrySet()) {
            statistic.add(entry.getValue());
        }
        return statistic.getPercentTranslated() + "%";
    }

    /**
     * Load up statistics for all project versions in all active locales in the
     * group
     * 
     * @return
     */
    private Map<VersionLocaleKey, WordsStatistic> getStatisticMap() {
        if (statisticMap == null) {
            Map<VersionLocaleKey, WordsStatistic> statisticMap =
                    Maps.newHashMap();

            for (LocaleItem localeItem : getActiveLocales()) {
                statisticMap.putAll(groupStatisticServiceImpl
                        .getLocaleStatistic(getInstanceProjectIterations(),
                                localeItem.getLocale().getLocaleId()));
            }
        }
        return statisticMap;
    }

}
