/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.zanata.dao.ProjectDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HPerson;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.service.VersionGroupService;
import org.zanata.service.VersionGroupService.SelectableHProject;

import com.google.common.collect.Lists;

@Name("versionGroupJoinAction")
@Scope(ScopeType.PAGE)
public class VersionGroupJoinAction implements Serializable {
    private static final long serialVersionUID = 1L;

    @In
    private VersionGroupService versionGroupServiceImpl;

    @In
    private ProjectDAO projectDAO;

    @In
    private ProjectIterationDAO projectIterationDAO;

    @In(create = true)
    private SendEmailAction sendEmail;

    @In(required = false, value = JpaIdentityStore.AUTHENTICATED_USER)
    private HAccount authenticatedAccount;

    @Getter
    private List<SelectableHProject> projectVersions = Lists.newArrayList();

    @Getter
    @Setter
    private String slug;

    @Getter
    @Setter
    private String iterationSlug;

    @Getter
    @Setter
    private String projectSlug;

    @RequestParameter
    private String[] slugParam;

    @Getter
    @Setter
    private String searchTerm = "";

    @Getter
    @Setter
    private boolean selectAll = false;

    @Getter
    private List<SelectableHProject> searchResults = Lists.newArrayList();

    public boolean isParamExists() {
        return slugParam != null && slugParam.length != 0;
    }

    public void selectAll() {
        for (SelectableHProject selectableVersion : getSearchResults()) {
            if (!isVersionInGroup(selectableVersion.getProjectIteration()
                    .getId())) {
                selectableVersion.setSelected(selectAll);
            }
        }
    }

    public void searchMaintainedProjectVersion() {
        Set<HProject> maintainedProjects =
                authenticatedAccount.getPerson().getMaintainerProjects();
        for (HProject project : maintainedProjects) {
            for (HProjectIteration projectIteration : projectDAO
                    .getAllIterations(project.getSlug())) {
                projectVersions.add(new SelectableHProject(projectIteration,
                        false));
            }
        }
    }

    public void searchProjectVersion() {
        if (StringUtils.isNotEmpty(iterationSlug)
                && StringUtils.isNotEmpty(projectSlug)) {
            HProjectIteration projectIteration =
                    projectIterationDAO.getBySlug(projectSlug, iterationSlug);
            if (projectIteration != null) {
                projectVersions.add(new SelectableHProject(projectIteration,
                        true));
            }

        }
    }

    public boolean isVersionInGroup(Long projectIterationId) {
        return versionGroupServiceImpl.isVersionInGroup(slug,
                projectIterationId);
    }

    public String cancel() {
        return sendEmail.cancel();
    }

    public String send() {
        boolean isAnyVersionSelected = false;
        for (SelectableHProject projectVersion : projectVersions) {
            if (projectVersion.isSelected()) {
                isAnyVersionSelected = true;
            }
        }
        if (isAnyVersionSelected) {
            List<HPerson> maintainers = new ArrayList<HPerson>();
            for (HPerson maintainer : versionGroupServiceImpl
                    .getMaintainerBySlug(slug)) {
                maintainers.add(maintainer);
            }
            return sendEmail.sendToVersionGroupMaintainer(maintainers);
        } else {
            FacesMessages.instance().add(
                    "#{messages['jsf.NoProjectVersionSelected']}");
            return "success";
        }

    }

    public String getQuery() {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append(slug);
        queryBuilder.append("/");
        if (!projectVersions.isEmpty()) {
            queryBuilder.append("?");

            for (int i = 0; i < projectVersions.size(); i++) {
                SelectableHProject projectVersion = projectVersions.get(i);
                if (projectVersion.isSelected()) {
                    if (i != 0) {
                        queryBuilder.append("&");
                    }
                    queryBuilder.append("slugParam=");
                    queryBuilder.append(projectVersion.getProjectIteration()
                            .getProject().getSlug());
                    queryBuilder.append(":");
                    queryBuilder.append(projectVersion.getProjectIteration()
                            .getSlug());
                }
            }
        }
        return queryBuilder.toString();
    }

    /**
     * Run search on unique project version if projectSlug, iterationSlug exits
     * else search versions available
     */
    public void executePreSearch() {
        if (isParamExists()) {
            for (String param : slugParam) {
                String[] paramSet = param.split(":");

                if (paramSet.length == 2) {
                    HProjectIteration projectVersion =
                            versionGroupServiceImpl.getProjectIterationBySlug(
                                    paramSet[0], paramSet[1]);
                    if (projectVersion != null) {
                        getSearchResults().add(
                                new SelectableHProject(projectVersion, true));
                    }
                }
            }
        } else {
            searchProjectAndVersion();
        }

    }

    @Transactional
    @Restrict("#{s:hasPermission(versionGroupHomeAction.instance, 'update')}")
    private void joinVersionGroup(Long projectIterationId) {
        versionGroupServiceImpl.joinVersionGroup(slug, projectIterationId);
    }

    @Transactional
    @Restrict("#{s:hasPermission(versionGroupHomeAction.instance, 'update')}")
    public void leaveVersionGroup(Long projectIterationId) {
        versionGroupServiceImpl.leaveVersionGroup(slug, projectIterationId);
        searchProjectAndVersion();
    }

    public void searchProjectAndVersion() {
        getSearchResults().clear();
        List<HProjectIteration> result =
                versionGroupServiceImpl
                        .searchLikeSlugOrProjectSlug(this.searchTerm);
        for (HProjectIteration version : result) {
            getSearchResults().add(new SelectableHProject(version, false));
        }
    }

    public void addSelected() {
        for (SelectableHProject selectableVersion : getSearchResults()) {
            if (selectableVersion.isSelected()) {
                joinVersionGroup(selectableVersion.getProjectIteration()
                        .getId());
            }
        }
    }

    public boolean isUserProjectMaintainer() {
        return authenticatedAccount != null
                && authenticatedAccount.getPerson().isMaintainerOfProjects();
    }
}
