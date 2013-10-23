/*
 *
 *  * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
 *  * @author tags. See the copyright.txt file in the distribution for a full
 *  * listing of individual contributors.
 *  *
 *  * This is free software; you can redistribute it and/or modify it under the
 *  * terms of the GNU Lesser General Public License as published by the Free
 *  * Software Foundation; either version 2.1 of the License, or (at your option)
 *  * any later version.
 *  *
 *  * This software is distributed in the hope that it will be useful, but WITHOUT
 *  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 *  * details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public License
 *  * along with this software; if not, write to the Free Software Foundation,
 *  * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 *  * site: http://www.fsf.org.
 */

package org.zanata.service.impl;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.sf.ehcache.CacheManager;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.zanata.cache.CacheWrapper;
import org.zanata.cache.EhcacheWrapper;
import org.zanata.common.LocaleId;
import org.zanata.common.statistic.WordsStatistic;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.events.TextFlowTargetStateEvent;
import org.zanata.service.VersionStateCache;

import com.google.common.cache.CacheLoader;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Name("versionStateCacheImpl")
@Scope(ScopeType.APPLICATION)
public class VersionStateCacheImpl implements VersionStateCache {
    private static final String BASE = VersionStateCacheImpl.class.getName();

    private static final String VERSION_STATISTIC_CACHE_NAME = BASE
            + ".versionStatisticCache";

    private CacheManager cacheManager;

    private CacheWrapper<VersionLocaleKey, WordsStatistic> versionStatisticCache;
    private CacheLoader<VersionLocaleKey, WordsStatistic> versionStatisticLoader;

    public VersionStateCacheImpl() {
        // constructor for Seam
        this.versionStatisticLoader = new VersionStatisticLoader();
    }

    @Create
    public void create() {
        cacheManager = CacheManager.create();
        versionStatisticCache =
                EhcacheWrapper.create(VERSION_STATISTIC_CACHE_NAME,
                        cacheManager, versionStatisticLoader);
    }

    @Destroy
    public void destroy() {
        cacheManager.shutdown();
    }

    @Observer(TextFlowTargetStateEvent.EVENT_NAME)
    @Override
    public void textFlowStateUpdated(TextFlowTargetStateEvent event) {
        VersionLocaleKey key =
                new VersionLocaleKey(event.getVersionId(), event.getLocaleId());
        WordsStatistic stats = versionStatisticCache.get(key);

        if (stats != null) {
            stats.decrement(event.getPreviousState(), 1);
            stats.increment(event.getNewState(), 1);
            versionStatisticCache.put(key, stats);
        }
    }

    @Override
    public WordsStatistic
            getVersionStatistic(Long versionId, LocaleId localeId) {
        return versionStatisticCache.getWithLoader(new VersionLocaleKey(
                versionId, localeId));
    }

    private static class VersionStatisticLoader extends
            CacheLoader<VersionLocaleKey, WordsStatistic> {

        ProjectIterationDAO getProjectIterationDAO() {
            return (ProjectIterationDAO) Component
                    .getInstance(ProjectIterationDAO.class);
        }

        @Override
        public WordsStatistic load(VersionLocaleKey key) throws Exception {

            WordsStatistic wordsStatistic =
                    getProjectIterationDAO().getWordStatistic(
                            key.getVersionId(), key.getLocaleId());

            return wordsStatistic;
        }
    }

    @AllArgsConstructor
    @EqualsAndHashCode
    public static final class VersionLocaleKey implements Serializable {
        private static final long serialVersionUID = 1L;

        @Getter
        private Long versionId;

        @Getter
        private LocaleId localeId;
    }
}
