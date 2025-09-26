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

package org.kitodo.production.services.data;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.kitodo.api.externaldatamanagement.ImportConfigurationType;
import org.kitodo.api.externaldatamanagement.SearchInterfaceType;
import org.kitodo.data.database.beans.ImportConfiguration;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.ImportConfigurationDAO;
import org.kitodo.exceptions.ImportConfigurationInUseException;
import org.kitodo.production.helper.XMLUtils;
import org.kitodo.production.services.ServiceManager;
import org.primefaces.model.SortOrder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

public class ImportConfigurationService extends BaseBeanService<ImportConfiguration, ImportConfigurationDAO> {

    private static volatile ImportConfigurationService instance = null;

    /**
     * Standard constructor.
     */
    public ImportConfigurationService() {
        super(new ImportConfigurationDAO());
    }

    /**
     * Return singleton variable of type OpacConfigurationService.
     *
     * @return unique instance of OpacConfigurationService
     */
    public static ImportConfigurationService getInstance() {
        ImportConfigurationService localReference = instance;
        if (Objects.isNull(localReference)) {
            synchronized (ImportConfigurationService.class) {
                localReference = instance;
                if (Objects.isNull(localReference)) {
                    localReference = new ImportConfigurationService();
                    instance = localReference;
                }
            }
        }
        return localReference;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ImportConfiguration> loadData(int first, int pageSize, String sortField, SortOrder sortOrder, Map filters) {
        return dao.getByQuery("FROM ImportConfiguration"  + getSort(sortField, sortOrder), filters, first, pageSize);
    }

    @Override
    public Long count() throws DAOException {
        return count("SELECT COUNT(*) FROM ImportConfiguration");
    }

    @Override
    public Long countResults(Map filters) throws DAOException {
        return count();
    }

    /**
     * Delete import configuration identified by ID. If the corresponding configuration is currently used as default
     * configuration for a project, an exception is thrown.
     * @param id of import configuration to delete
     * @throws DAOException if import configuration could not be deleted from database
     * @throws ImportConfigurationInUseException if import configuration is assigned as default configuration or
     *         default child configuration to at least one project
     */
    @Override
    public void remove(Integer id) throws DAOException, ImportConfigurationInUseException {
        for (Project project : ServiceManager.getProjectService().getAll()) {
            ImportConfiguration defaultConfiguration = project.getDefaultImportConfiguration();
            if (Objects.nonNull(defaultConfiguration) && Objects.equals(defaultConfiguration.getId(), id)) {
                throw new ImportConfigurationInUseException(defaultConfiguration.getTitle(), project.getTitle());
            }
            ImportConfiguration defaultChildConfiguration = project.getDefaultChildProcessImportConfiguration();
            if (Objects.nonNull(defaultChildConfiguration) && Objects.equals(defaultChildConfiguration.getId(), id)) {
                throw new ImportConfigurationInUseException(defaultChildConfiguration.getTitle(), project.getTitle());
            }
        }
        dao.remove(id);
    }


    /**
     * Load and return all ImportConfigurations of type OPAC_SEARCH.
     * @return list of OPAC_SEARCH type ImportConfigurations
     * @throws DAOException when ImportConfigurations could not be loaded
     */
    public List<ImportConfiguration> getAllOpacSearchConfigurations() throws DAOException {
        return getAllImportConfigurations(ImportConfigurationType.OPAC_SEARCH);
    }

    /**
     * Load and return all ImportConfigurations of type FILE_UPLOAD.
     * @return list of FILE_UPLOAD type ImportConfigurations
     * @throws DAOException when ImportConfigurations could not be loaded
     */
    public List<ImportConfiguration> getAllFileUploadConfigurations() throws DAOException {
        return getAllImportConfigurations(ImportConfigurationType.FILE_UPLOAD);
    }

    /**
     * Load and return all ImportConfigurations sorted by title.
     * @return list of all ImportConfigurations sorted by title
     * @throws DAOException when ImportConfigurations could not be loaded
     */
    @Override
    public List<ImportConfiguration> getAll() throws DAOException {
        User currentUser = ServiceManager.getUserService().getCurrentUser();
        return super.getAll().stream()
                .filter(importConfiguration -> !Collections.disjoint(importConfiguration.getClients(), currentUser.getClients()))
                .sorted(Comparator.comparing(ImportConfiguration::getTitle))
                .collect(Collectors.toList());
    }

    private List<ImportConfiguration> getAllImportConfigurations(ImportConfigurationType type) throws DAOException {
        User currentUser = ServiceManager.getUserService().getCurrentUser();
        return super.getAll().stream()
                .filter(importConfiguration -> !Collections.disjoint(importConfiguration.getClients(), currentUser.getClients()))
                .filter(importConfiguration -> type.name()
                        .equals(importConfiguration.getConfigurationType()))
                .sorted(Comparator.comparing(ImportConfiguration::getTitle))
                .collect(Collectors.toList());
    }

    /**
     * Retrieve list of available SRU record schemata from SRU server defined in given ImportConfiguration.
     *
     * @param config ImportConfiguration containing SRU server connection data
     * @return list of available SRU record schemata as Strings
     * @throws IOException when loading the XML response from the SRU explain operation fails
     * @throws ParserConfigurationException when parsing the XML response from the SRU explain operation fails
     * @throws SAXException when parsing the XML response from the SRU explain operation fails
     */
    public static List<String> getAvailableSruRecordSchemata(ImportConfiguration config) throws IOException, ParserConfigurationException, SAXException {
        List<String> recordSchemata = new ArrayList<>();
        URL explainUrl = getSruExplainUrl(config);
        Document explainDocument = XMLUtils.loadXmlDocumentByUrl(explainUrl);
        NodeList schemaNodes = explainDocument.getElementsByTagName("schema");
        for (int i = 0; i < schemaNodes.getLength(); i++) {
            recordSchemata.add(schemaNodes.item(i).getAttributes().getNamedItem("name").getNodeValue());
        }
        return recordSchemata;
    }

    private static URL getSruExplainUrl(ImportConfiguration config) throws MalformedURLException {
        URL explainUrl = null;
        if (SearchInterfaceType.SRU.name().equals(config.getInterfaceType())) {
            String sruUrl = config.getScheme() + "://" + config.getHost()
                    + (Objects.nonNull(config.getPort())
                    ? ":" + config.getPort() : "")
                    + (Objects.nonNull(config.getPath()) ? config.getPath() : "")
                    + "?version=" + config.getSruVersion()
                    + "&operation=explain";
            explainUrl = new URL(sruUrl);
        }
        return explainUrl;
    }
}
