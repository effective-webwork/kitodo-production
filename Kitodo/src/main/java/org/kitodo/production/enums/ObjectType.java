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

package org.kitodo.production.enums;

import java.util.ArrayList;
import java.util.List;

import org.kitodo.data.database.beans.Authority;
import org.kitodo.data.database.beans.BaseBean;
import org.kitodo.data.database.beans.Batch;
import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.beans.DataEditorSetting;
import org.kitodo.data.database.beans.Docket;
import org.kitodo.data.database.beans.Filter;
import org.kitodo.data.database.beans.Folder;
import org.kitodo.data.database.beans.ImportConfiguration;
import org.kitodo.data.database.beans.LdapGroup;
import org.kitodo.data.database.beans.LdapServer;
import org.kitodo.data.database.beans.MappingFile;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.beans.Role;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.beans.Workflow;
import org.kitodo.production.helper.Helper;

public enum ObjectType {
    AUTHORITY("authority", "authorities", false, Authority.class),
    CLIENT("client", "clients", false, Client.class),
    BATCH("batch", "batches", true, Batch.class),
    DATAEDITORSETTING("dataEditorSetting", "dataEditorSettings", false, DataEditorSetting.class),
    DOCKET("docket", "dockets", true, Docket.class),
    FOLDER("folder", "folders", false, Folder.class),
    LDAP_GROUP("ldapGroup", "ldapGroups", false, LdapGroup.class),
    LDAP_SERVER("ldapServer", "ldapServers", false, LdapServer.class),
    PROCESS("process", "processes", true, Process.class),
    PROJECT("project", "projects", true, Project.class),
    PROPERTY("property", "properties", false, Property.class),
    RULESET("ruleset", "rulesets", true, Ruleset.class),
    TASK("task", "tasks", true, Task.class),
    TEMPLATE("template", "template", true, Template.class),
    USER("user", "users", false, User.class),
    ROLE("role", "roles", false, Role.class),
    WORKFLOW("workflow", "workflows", true, Workflow.class),
    FILTER("filter", "filters", true, Filter.class),
    IMPORT_CONFIGURATION("importConfig.configuration", "importConfig.configurations", false, ImportConfiguration.class),
    MAPPING_FILE("mappingFile.file", "mappingFile.files", false, MappingFile.class),
    NONE("", "", false, BaseBean.class);

    private final String messageKeySingular;

    private final String messageKeyPlural;

    private final Class aClass;

    private final boolean indexable;

    /**
     * Constructor setting the message key of the object type, used to retrieve it's
     * translation from the messages resource bundle.
     *
     * @param messageKeySingular
     *            used for translating the object types name
     */
    ObjectType(String messageKeySingular, String messageKeyPlural, boolean indexable, Class aClass) {
        this.messageKeySingular = messageKeySingular;
        this.messageKeyPlural = messageKeyPlural;
        this.indexable = indexable;
        this.aClass = aClass;
    }

    /**
     * Retrieve and return the translation of the object type for singular object.
     *
     * @return singular translation of this object type
     */
    public String getTranslationSingular() {
        return Helper.getTranslation(messageKeySingular);
    }

    /**
     * Retrieve and return the translation of the object type for plural object.
     *
     * @return plural translation of this object type
     */
    public String getTranslationPlural() {
        return Helper.getTranslation(messageKeyPlural);
    }

    /**
     * Get indexable.
     *
     * @return value of indexable
     */
    public boolean isIndexable() {
        return indexable;
    }

    public Class getaClass() {
        return aClass;
    }

    /**
     * Get list of indexable object types.
     *
     * @return list of indexable object types.
     */
    public static List<ObjectType> getIndexableObjectTypes() {
        List<ObjectType> objectTypes = new ArrayList<>();
        for (ObjectType objectType : ObjectType.values()) {
            if (objectType.isIndexable()) {
                objectTypes.add(objectType);
            }
        }
        return objectTypes;
    }
}
