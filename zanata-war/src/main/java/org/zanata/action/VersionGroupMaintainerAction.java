package org.zanata.action;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.datamodel.DataModelSelection;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.faces.FacesMessages;
import org.zanata.dao.AccountDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HIterationGroup;
import org.zanata.model.HPerson;
import org.zanata.service.VersionGroupService;

@Name("versionGroupMaintainerAction")
@Scope(ScopeType.PAGE)
@Slf4j
public class VersionGroupMaintainerAction implements Serializable {
    private static final long serialVersionUID = 1L;

    @DataModel
    private List<HPerson> maintainerList;

    @DataModelSelection
    HPerson selectedPerson;

    @Getter
    @Setter
    private String slug;

    private HIterationGroup group;

    @In
    private VersionGroupService versionGroupServiceImpl;

    @In
    private AccountDAO accountDAO;

    public void init() {
        maintainerList = versionGroupServiceImpl.getMaintainerBySlug(slug);
    }

    public HPerson getSelectedPerson() {
        return this.selectedPerson;
    }

    @Restrict("#{s:hasPermission(versionGroupMaintainerManageAction.group,'update')}")
    public
            void deleteMaintainer(HPerson person) {
        log.debug("try to delete maintainer {0} from slug {1}",
                person.getName(), this.slug);
        HIterationGroup iterationGroup =
                versionGroupServiceImpl.getBySlug(this.slug);
        Set<HPerson> personList = iterationGroup.getMaintainers();
        for (HPerson l : personList) {
            if (l.getEmail().equals(person.getEmail())) {
                log.debug("remove the person");
                iterationGroup.getMaintainers().remove(l);
                break;
            }
        }

        versionGroupServiceImpl.makePersistent(iterationGroup);
        versionGroupServiceImpl.flush();
    }

    @Restrict("#{s:hasPermission(versionGroupMaintainerManageAction.group,'update')}")
    public
            String addMaintainers(String account) {
        HAccount a = accountDAO.getByUsername(account);
        if (a == null) {
            FacesMessages.instance().add("This account does not exist.");
            return "failure";
        } else if (a.isEnabled()) {
            HIterationGroup iterationGroup =
                    versionGroupServiceImpl.getBySlug(this.slug);
            Set<HPerson> personList = iterationGroup.getMaintainers();
            personList.add(a.getPerson());
            versionGroupServiceImpl.makePersistent(iterationGroup);
            versionGroupServiceImpl.flush();
            log.debug("add {} into maintainers", account);
            return "success";
        } else {
            FacesMessages.instance().add("This account is disabled.");
            return "failure";
        }
    }

    public HIterationGroup getGroup() {
        if (group == null) {
            group = versionGroupServiceImpl.getBySlug(slug);
        }

        return group;
    }
}
