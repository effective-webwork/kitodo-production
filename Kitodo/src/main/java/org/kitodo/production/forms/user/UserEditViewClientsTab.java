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

package org.kitodo.production.forms.user;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.exception.DataException;
import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.forms.BaseTabEditView;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.TaskService;
import org.primefaces.PrimeFaces;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

@Named("UserEditViewClientsTab")
@ViewScoped
public class UserEditViewClientsTab extends BaseTabEditView<User> {

    private static final Logger logger = LogManager.getLogger(UserEditViewClientsTab.class);

    /**
     * The user object that is being edited (variable "user" references to the user currently logged in, see BaseForm).
     */
    private User userObject;

    private List<Client> clients;
    private Integer removeClientId = null;


    /**
     * Initialize UserEditViewClientsTab.
     */
    @PostConstruct
    public void init() {
        sortBy = SortMeta.builder().field("name").order(SortOrder.ASCENDING).build();
    }

    /**
     * Return user object currently being edited.
     * 
     * @return the user currently being edited
     */
    public User getUserObject() {
        return this.userObject;
    }

    /**
     * Return list of clients available for assignment to the user.
     *
     * @return list of clients available for assignment to the user
     */
    public List<Client> getClients() {
        return clients;
    }       

    /**
     * Method that is called from viewAction of user edit form.
     *
     * @param userObject
     *            the user currently being edited
     */
    @Override
    public void load(User userObject) {
        // reset when user is loaded
        this.userObject = userObject;

        try {
            this.clients = ServiceManager.getClientService().getAllAvailableForAssignToUser(this.userObject)
                    .stream().sorted(Comparator.comparing(Client::getName)).collect(Collectors.toList());
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_MANY, new Object[] {ObjectType.CLIENT.getTranslationPlural() }, logger,
                    e);
            this.clients = new LinkedList<>();
        }
    }

    /**
     * Remove user from client.
     *
     * @return null (to stay one the same page)
     */
    public String deleteFromClient() {
        if (Objects.nonNull(removeClientId)) {
            try {
                for (Client client : this.userObject.getClients()) {
                    if (client.getId().equals(removeClientId)) {
                        this.userObject.getClients().remove(client);
                        if (Objects.nonNull(this.clients) && !this.clients.contains(client)) {
                            this.clients.add(client);
                            this.clients.sort(Comparator.comparing(Client::getName, String.CASE_INSENSITIVE_ORDER));
                        }
                        if (client.equals(this.userObject.getDefaultClient())) {
                            this.userObject.setDefaultClient(null);
                        }
                        break;
                    }
                }
            } catch (NumberFormatException e) {
                Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            }
        } else {
            Helper.setErrorMessage(ERROR_PARAMETER_MISSING, new Object[] {ID_PARAMETER});
        }
        return this.stayOnCurrentPage;
    }

    /**
     * Add client to user.
     *
     * @return null (to stay one the same page)
     */
    public String addToClient() {
        String idParameter = Helper.getRequestParameter("ID");
        if (Objects.nonNull(idParameter)) {
            int clientId = 0;
            try {
                clientId = Integer.parseInt(idParameter);
                Client client = ServiceManager.getClientService().getById(clientId);

                if (!this.userObject.getClients().contains(client)) {
                    this.userObject.getClients().add(client);
                    if (Objects.nonNull(this.clients)) {
                        this.clients.remove(client);
                    }
                }
            } catch (DAOException e) {
                Helper.setErrorMessage(ERROR_DATABASE_READING,
                        new Object[] {ObjectType.CLIENT.getTranslationSingular(), clientId }, logger, e);
            } catch (NumberFormatException e) {
                Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            }
        } else {
            Helper.setErrorMessage(ERROR_PARAMETER_MISSING, new Object[] {ID_PARAMETER});
        }
        return this.stayOnCurrentPage;
    }

    /**
     * Check whether User 'userObject' is currently assigned to any projects associated with the client
     * identified by given ID 'clientId'. If true, the user is prompted to confirm his removal from those projects
     * before removing the client from the user. If false, the client is remove from the user directly.
     *
     * @param clientId ID of client that is remove from the user
     */
    public void checkClientProjects(Integer clientId) {
        List<Project> assignedClientProjects = this.userObject.getProjects().stream()
                .filter(p -> p.getClient().getId().equals(clientId)).toList();
        removeClientId = clientId;
        if (assignedClientProjects.isEmpty()) {
            deleteFromClient();
        } else {
            PrimeFaces.current().ajax().update("removeClientDialog");
            PrimeFaces.current().executeScript("PF('removeClientDialog').show();");
        }
    }

    /**
     * Remove all projects from User 'userObject' that are associated with the client identified by ID
     * 'removeClientId'.
     */
    public void removeUserFromClientProjects() {
        try {
            if (Objects.nonNull(this.removeClientId)) {
                // FIXME: this call already _saves_ the updated task status! If the user decided _not_ to save the
                //  updated userObject without the removed client, the tasks should also not be set to open!
                //  -> move this method call to 'save' method of userForm!
                TaskService.resetTasksToOpen(getTasksInProgress(userObject, removeClientId));
                this.userObject.getRoles().removeAll(this.userObject.getRoles().stream()
                        .filter(r -> r.getClient().getId().equals(this.removeClientId)).toList());
                this.userObject.getProjects().removeAll(this.userObject.getProjects().stream()
                        .filter(p -> p.getClient().getId().equals(this.removeClientId)).toList());
            }
        } catch (DataException | DAOException e) {
            Helper.setErrorMessage(e);
        }
    }

    /**
     * Retrieve and return list of tasks that are assigned to the user, have TaskStatus "INWORK" and belong to processes
     * of the client with the given ID 'clientId'.
     *
     * @param user User whose tasks are reset
     * @param clientId ID of client by which tasks are filtered.
     * @return list of tasks
     */
    public List<Task> getTasksInProgress(User user, int clientId) {
        List<Task> tasks = ServiceManager.getTaskService().getTasksInProgress(user);
        return tasks.stream().filter(task -> task.getProcess().getProject().getClient().getId().equals(clientId))
                .collect(Collectors.toList());
    }


    /**
     * Return list of project titles that are associated with the client with ID 'removeClientId' and
     * assigned to User 'userObject'.
     * @return list of process titles
     */
    public List<String> getClientProjects() {
        if (Objects.nonNull(this.removeClientId)) {
            return this.userObject.getProjects().stream()
                    .filter(project -> project.getClient().getId().equals(this.removeClientId))
                    .map(Project::getTitle).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Get 'removeClientId'. ID of the client that is to be removed from User 'userObject'.
     *
     * @return removeClientId
     */
    public Integer getRemoveClientId() {
        return removeClientId;
    }

    /**
     * Set 'removeClientId'.
     * @param clientId ID of the client that is to be removed from User 'userObject'.
     */
    public void setRemoveClientId(Integer clientId) {
        this.removeClientId = clientId;
    }

}
