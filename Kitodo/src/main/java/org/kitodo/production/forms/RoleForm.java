/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package org.kitodo.production.forms;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Authority;
import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.beans.Role;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.enums.LogEntryType;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.AuditingLogger;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.model.LazyDTOModel;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.RoleService;
import org.primefaces.model.DualListModel;

@Named("RoleForm")
@SessionScoped
public class RoleForm extends BaseForm {
    private static final Logger logger = LogManager.getLogger(RoleForm.class);
    private Role role = new Role();

    private final String roleEditPath = MessageFormat.format(REDIRECT_PATH, "roleEdit");

    private Role originalStateRole;
    private List<Authority> originalAuthorities;

    /**
     * Default constructor that also sets the LazyDTOModel instance of this bean.
     */
    public RoleForm() {
        super();
        super.setLazyDTOModel(new LazyDTOModel(ServiceManager.getRoleService()));
    }

    /**
     * Create new role.
     *
     * @return page address
     */
    public String newRole() {
        this.role = new Role();

        if (!ServiceManager.getSecurityAccessService().hasAuthorityGlobalToAddOrEditRole()) {
            Client sessionClient = ServiceManager.getUserService().getSessionClientOfAuthenticatedUser();
            if (Objects.nonNull(sessionClient)) {
                this.role.setClient(sessionClient);
            }
        }

        return roleEditPath;
    }

    /**
     * Save role.
     *
     * @return page or empty String
     */
    public String save() {
        try {
            ServiceManager.getRoleService().saveToDatabase(this.role);
            this.logChanges(this.originalStateRole, this.role, this.originalAuthorities);
            return usersPage;
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.ROLE.getTranslationSingular() }, logger, e);
            return this.stayOnCurrentPage;
        }
    }

    /**
     * Remove role.
     */
    public void delete() {
        try {
            if (!this.role.getUsers().isEmpty()) {
                for (User user : this.role.getUsers()) {
                    user.getRoles().remove(this.role);
                }
                this.role.setUsers(new ArrayList<>());
                ServiceManager.getRoleService().saveToDatabase(this.role);

            }
            if (!this.role.getTasks().isEmpty()) {
                Helper.setErrorMessage("roleAssignedError");
                return;
            }
            if (!this.role.getAuthorities().isEmpty()) {
                this.role.setAuthorities(new ArrayList<>());
                ServiceManager.getRoleService().saveToDatabase(this.role);
            }
            ServiceManager.getRoleService().removeFromDatabase(this.role);
            logger.info(createObjectSaveOrDeleteLogEntry(this.role.getTitle(), this.role.getId(), LogEntryType.DELETE));
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_DELETING, new Object[] {ObjectType.ROLE.getTranslationSingular() }, logger, e);
        }
    }

    /**
     * Method being used as viewAction for role edit form. Selectable clients
     * and projects are initialized as well.
     *
     * @param id
     *            ID of the role to load
     */
    public void load(int id) {
        if (!Objects.equals(id, 0)) {
            setRoleById(id);
        }
        setSaveDisabled(true);
        resetLoggedChanges();
        this.originalStateRole = new Role(this.role);
        this.originalAuthorities = new LinkedList<>(this.role.getAuthorities());
    }

    /**
     * Get the role.
     *
     * @return the role
     */
    public Role getRole() {
        return this.role;
    }

    /**
     * Set the role.
     *
     * @param role
     *            the role
     */
    public void setRole(Role role) {
        this.role = role;
    }

    /**
     * Set role by id.
     *
     * @param id
     *            of role to set
     */
    public void setRoleById(int id) {
        try {
            setRole(ServiceManager.getRoleService().getById(id));
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE, new Object[] {ObjectType.ROLE.getTranslationSingular(), id },
                logger, e);
        }
    }

    /**
     * Get all available clients.
     *
     * @return list of Client objects
     */
    public List<Client> getClients() {
        try {
            return ServiceManager.getClientService().getAll();
        } catch (DAOException e) {
            return new ArrayList<>();
        }
    }

    /**
     * Return the list of available authorization levels and the list of authority
     * levels currently assigned to 'role' as a combined 'DualListModel' that
     * is used by the frontend for authority management of roles utilizing a
     * PrimeFaces PickList object.
     *
     * @return DualListModel of available and assigned authority levels
     */
    public DualListModel<Authority> getGlobalAssignableAuthorities() {
        List<Authority> assignedAuthorities = ServiceManager.getAuthorityService()
                .filterAssignableGlobal(this.role.getAuthorities());
        List<Authority> availableAuthorities = new ArrayList<>();
        try {
            availableAuthorities = ServiceManager.getAuthorityService().getAllAssignableGlobal();
            availableAuthorities.removeAll(assignedAuthorities);
        } catch (DAOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        return new DualListModel<>(sortAuthorityListByTitle(availableAuthorities), sortAuthorityListByTitle(assignedAuthorities));
    }

    /**
     * Assign the target property of given DualListModel of authorities to
     * 'role' using a PrimeFaces PickList object.
     *
     * @param globalAuthoritiesModel
     *            list of authority assigned to 'role'
     */
    public void setGlobalAssignableAuthorities(DualListModel<Authority> globalAuthoritiesModel) {
        setAssignableAuthorities(globalAuthoritiesModel);
    }

    /**
     * Return the list of available authorization levels which can be assigned
     * client specific and the list of authority levels currently client specific
     * assigned to 'role' as a combined 'DualListModel' that is used by the
     * frontend for authority management of user groups utilizing a PrimeFaces
     * PickList object.
     *
     * @return DualListModel of available and assigned authority levels
     */
    public DualListModel<Authority> getClientAssignableAuthorities() {
        List<Authority> assignedAuthorities = ServiceManager.getAuthorityService()
                .filterAssignableToClients(this.role.getAuthorities());
        List<Authority> availableAuthorities = new ArrayList<>();
        try {
            availableAuthorities = ServiceManager.getAuthorityService().getAllAssignableToClients();
            availableAuthorities.removeAll(assignedAuthorities);
        } catch (DAOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        return new DualListModel<>(sortAuthorityListByTitle(availableAuthorities), sortAuthorityListByTitle(assignedAuthorities));
    }

    /**
     * Assign the target property of given DualListModel of authorities to
     * 'role' using a PrimeFaces PickList object.
     *
     * @param clientAuthoritiesModel
     *            list of authority assigned to 'role'
     */
    public void setClientAssignableAuthorities(DualListModel<Authority> clientAuthoritiesModel) {
        setAssignableAuthorities(clientAuthoritiesModel);
    }

    private void setAssignableAuthorities(DualListModel<Authority> authoritiesModel) {
        for (Authority authority : authoritiesModel.getSource()) {
            this.role.getAuthorities().remove(authority);
        }
        for (Authority authority : authoritiesModel.getTarget()) {
            if (!this.role.getAuthorities().contains(authority)) {
                this.role.getAuthorities().add(authority);
            }
        }
    }

    private String createPropertyAddOrRemoveLogEntry(String propertyName, Integer propertyId, LogEntryType type) {
        return AuditingLogger.createPropertyAddOrRemoveLogEntry(type, ObjectType.AUTHORITY.getMessageKeySingular(),
                propertyName, propertyId, ObjectType.ROLE.getMessageKeySingular(), this.role.getTitle(),
                this.role.getId());
    }

    private String createFieldUpdateLogEntry(String field, String oldValue, String newValue) {
        return AuditingLogger.createFieldUpdateLogEntry(field, oldValue, newValue,
                ObjectType.ROLE.getMessageKeySingular(), this.originalStateRole.getTitle(),
                this.originalStateRole.getId());
    }

    private String createObjectSaveOrDeleteLogEntry(String name, Integer id, LogEntryType type) {
        return AuditingLogger.createObjectSaveOrDeleteLogEntry(type, ObjectType.ROLE.getMessageKeySingular(), name, id);
    }

    private List<String> createAuthorityDifferenceLogEntries(List<Authority> oldAuthorities,
                                                             List<Authority> newAuthorities) {
        LinkedList<String> changedAuthoritiesLogEntries = new LinkedList<>();
        HashSet<Authority> removedAuthorities = new HashSet<>(oldAuthorities);
        removedAuthorities.removeAll(new HashSet<>(newAuthorities));

        HashSet<Authority> addedAuthorities = new HashSet<>(newAuthorities);
        addedAuthorities.removeAll(new HashSet<>(oldAuthorities));

        for (Authority authority : removedAuthorities) {
            changedAuthoritiesLogEntries.add(createPropertyAddOrRemoveLogEntry(authority.getTitle(), authority.getId(),
                    LogEntryType.REMOVE));
        }

        for (Authority authority : addedAuthorities) {
            changedAuthoritiesLogEntries.add(createPropertyAddOrRemoveLogEntry(authority.getTitle(), authority.getId(),
                    LogEntryType.ADD));
        }
        return changedAuthoritiesLogEntries;
    }

    private void logChanges(Role oldRole, Role newRole, List<Authority> oldAuthorities) {
        if (Objects.nonNull(oldRole) && Objects.nonNull(newRole)) {
            for (Map.Entry<String, Pair<String, String>> change :
                    RoleService.detectChanges(oldRole, newRole).entrySet()) {
                loggedChanges.add(createFieldUpdateLogEntry(change.getKey(), change.getValue().getLeft(),
                        change.getValue().getRight()));
            }
            loggedChanges.addAll(createAuthorityDifferenceLogEntries(oldAuthorities,
                    newRole.getAuthorities()));
        }
        for (String change : loggedChanges) {
            logger.info(change);
        }
    }

    private List<Authority> sortAuthorityListByTitle(List<Authority> authorityListToSort) {
        return authorityListToSort.stream()
                .sorted(Comparator.comparing(authority -> Helper.getTranslation(authority.getTitleWithoutSuffix())))
                .collect(Collectors.toList());
    }
}
