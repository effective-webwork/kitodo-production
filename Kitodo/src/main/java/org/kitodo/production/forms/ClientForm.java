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
import java.util.List;
import java.util.Objects;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.beans.Role;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.enums.LogEntryType;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.AuditingLogger;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.model.LazyDTOModel;
import org.kitodo.production.services.ServiceManager;

@Named("ClientForm")
@SessionScoped
public class ClientForm extends BaseForm {
    private Client client;
    private static final Logger logger = LogManager.getLogger(ClientForm.class);

    private final String clientEditPath = MessageFormat.format(REDIRECT_PATH, "clientEdit");
    private Client clientToCopyRoles;
    private List<Role> rolesForClient;

    private String originalStateClientName;

    /**
     * Empty default constructor that also sets the LazyDTOModel instance of this
     * bean.
     */
    public ClientForm() {
        super();
        super.setLazyDTOModel(new LazyDTOModel(ServiceManager.getClientService()));
    }

    /**
     * Save client.
     *
     * @return page or empty String
     */
    public String save() {
        try {
            for (Role role : rolesForClient) {
                ServiceManager.getRoleService().saveToDatabase(role);
            }
            ServiceManager.getClientService().saveToDatabase(this.client);
            rolesForClient = null;
            this.logChanges(this.originalStateClientName, this.client);
            return usersPage;
        } catch (DAOException | RuntimeException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.CLIENT.getTranslationSingular() }, logger, e);
            return this.stayOnCurrentPage;
        }
    }

    /**
     * Method being used as viewAction for client edit form. If 'clientId' is '0',
     * the form for creating a new client will be displayed.
     */
    public void load(int id) {
        try {
            rolesForClient = null;
            if (!Objects.equals(id, 0)) {
                this.client = ServiceManager.getClientService().getById(id);
            }
            this.originalStateClientName = Objects.nonNull(this.client.getName()) ? this.client.getName() : "";
            setSaveDisabled(true);
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE, new Object[] {ObjectType.CLIENT.getTranslationSingular(), id },
                logger, e);
        }
    }

    /**
     * Create new client.
     *
     * @return page address
     */
    public String newClient() {
        resetLoggedChanges();
        this.client = new Client();
        this.client.setListColumns(ServiceManager.getListColumnService().getAllStandardListColumns());
        return clientEditPath;
    }

    /**
     * Gets client.
     *
     * @return The client.
     */
    public Client getClient() {
        return client;
    }

    /**
     * Sets client.
     *
     * @param client
     *            The client.
     */
    public void setClient(Client client) {
        this.client = client;
    }

    /**
     * Set client by ID.
     *
     * @param clientID
     *          ID of client to set.
     */
    public void setClientById(int clientID) {
        try {
            setClient(ServiceManager.getClientService().getById(clientID));
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE, new Object[] {ObjectType.CLIENT.getTranslationSingular(), clientID }, logger, e);
        }
    }

    /**
     * Delete client.
     */
    public void delete() {
        try {
            this.client.getListColumns().clear();
            ServiceManager.getClientService().removeFromDatabase(this.client);
            logger.info("Client " + this.client.getId() + " deleted by user "
                    + ServiceManager.getUserService().getAuthenticatedUser().getId() + ".");
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_DELETING, new Object[] {ObjectType.CLIENT.getTranslationSingular() }, logger, e);
        }
    }

    /**
     * Gets all roles for a Client.
     *
     * @return a list of roles
     */
    public List<Role> getRolesForClient() {
        if (Objects.isNull(rolesForClient)) {
            if (Objects.nonNull(client) && Objects.nonNull(client.getId())) {
                rolesForClient = ServiceManager.getRoleService().getAllRolesByClientId(client.getId());
            } else {
                rolesForClient = new ArrayList<>();
            }
        }
        return rolesForClient;
    }

    /**
     * Get clientToCopyRoles.
     *
     * @return value of clientToCopyRoles
     */
    public Client getClientToCopyRoles() {
        return clientToCopyRoles;
    }

    /**
     * Set clientToCopyRoles.
     *
     * @param clientToCopyRoles
     *            as org.kitodo.data.database.beans.Client
     */
    public void setClientToCopyRoles(Client clientToCopyRoles) {
        this.clientToCopyRoles = clientToCopyRoles;
    }

    /**
     * Copies all roles from a chosen client to the current client.
     */
    public void copyRolesToClient() {
        List<Role> allRolesToCopy = ServiceManager.getRoleService().getAllRolesByClientId(clientToCopyRoles.getId());
        for (Role role : allRolesToCopy) {
            Role newRole = new Role();
            newRole.setTitle(role.getTitle());
            newRole.setClient(client);
            newRole.setAuthorities(new ArrayList<>(role.getAuthorities()));
            rolesForClient.add(newRole);
            this.loggedChanges.add(AuditingLogger.createPropertyAddOrRemoveLogEntry(LogEntryType.ADD,
                    ObjectType.ROLE.getMessageKeySingular(), newRole.getTitle(), newRole.getId(),
                    ObjectType.CLIENT.getMessageKeySingular(), this.client.getName(), this.client.getId()));
        }
    }

    /**
     * Removes a given role from a client.
     *
     * @param roleToRemove
     *            role to remove.
     */
    public void deleteRoleFromClient(Role roleToRemove) {
        rolesForClient.remove(roleToRemove);
        this.loggedChanges.add(AuditingLogger.createPropertyAddOrRemoveLogEntry(LogEntryType.REMOVE,
                ObjectType.ROLE.getMessageKeySingular(), roleToRemove.getTitle(), roleToRemove.getId(),
                ObjectType.CLIENT.getMessageKeySingular(), this.client.getName(), this.client.getId()));
    }

    /**
     * Get all clients where roles can be copied from.
     * @return a list of possible clients.
     * @throws DAOException when Database connection fails.
     */
    public List<Client> getPossibleClientsForCopying() throws DAOException {
        List<Client> allClients = ServiceManager.getClientService().getAll();
        allClients.remove(client);
        return allClients;
    }

    private void logChanges(String oldClientName, Client newClient) {
        for (String change : this.loggedChanges) {
            logger.info(change);
        }
        if (!Objects.equals(newClient.getName(), oldClientName)) {
            logger.info(AuditingLogger.createFieldUpdateLogEntry("title", oldClientName, newClient.getName(),
                    ObjectType.CLIENT.getMessageKeySingular(), oldClientName, newClient.getId()));
        }
        logger.info(AuditingLogger.createObjectSaveOrDeleteLogEntry(LogEntryType.SAVE,
                ObjectType.CLIENT.getMessageKeySingular(), newClient.getName(),
                newClient.getId()));
    }
}
