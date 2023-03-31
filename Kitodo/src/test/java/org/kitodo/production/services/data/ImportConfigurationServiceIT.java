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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.ImportConfiguration;
import org.kitodo.production.services.ServiceManager;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ImportConfigurationServiceIT {

    private static final long NUMBER_OF_IMPORT_CONFIGURATIONS = 3;
    private static final String FIRST_IMPORT_CONFIGURATION_TITLE = "GBV";
    private static final String LAST_IMPORT_CONFIGURATION_TITLE = "Kalliope";

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertMappingFiles();
        MockDatabase.insertImportConfigurations();
        MockDatabase.setUpAwaitility();
    }

    /**
     * Verifies number and order of import configurations returned by service class.
     * @throws Exception when import configurations could not be loaded from database
     */
    @Test
    public void shouldGetAllImportConfigurationsInAlphabeticOrder() throws Exception {
        List<ImportConfiguration> importConfigurations = ServiceManager.getImportConfigurationService().getAll();
        int numberOfImportConfigurations = importConfigurations.size();
        assertEquals("Wrong number of import configurations", NUMBER_OF_IMPORT_CONFIGURATIONS,
                numberOfImportConfigurations);
        assertEquals("Wrong first import configuration", FIRST_IMPORT_CONFIGURATION_TITLE, importConfigurations
                .get(0).getTitle());
        assertEquals("Wrong last import configuration", LAST_IMPORT_CONFIGURATION_TITLE, importConfigurations
                .get(numberOfImportConfigurations - 1).getTitle());
    }

    @AfterClass
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

}
