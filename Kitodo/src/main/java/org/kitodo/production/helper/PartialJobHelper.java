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

package org.kitodo.production.helper;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.kitodo.api.dataeditor.rulesetmanagement.MetadataViewWithValuesInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.RulesetManagementInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.SimpleMetadataViewInterface;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.User;
import org.kitodo.production.services.ServiceManager;

public class PartialJobHelper {

    public static Map<String, String> getDigitalisierungsprofile(Process process) throws IOException {
        // get user’s meta-data language
        User user = ServiceManager.getUserService().getAuthenticatedUser();
        String metadataLanguage = user != null ? user.getMetadataLanguage()
                : Helper.getRequestParameter("Accept-Language");
        List<Locale.LanguageRange> priorityList = Locale.LanguageRange.parse(metadataLanguage != null ? metadataLanguage : "de");

        // access ruleset
        RulesetManagementInterface rulesetManagement = ServiceManager.getRulesetManagementService()
                .getRulesetManagement();
        rulesetManagement.load(new File(ConfigCore.getParameter(ParameterCore.DIR_RULESETS) + process.getRuleset().getFile()));

        // open view on structural element 'partitialJob' with one entry 'profile'
        List<MetadataViewWithValuesInterface> metadataViewsWithValues = rulesetManagement
                .getStructuralElementView("partialJob", null, priorityList)
                .getSortedVisibleMetadata(Collections.emptySet(), Collections.singletonList("profile"));
        // get 'profile' from view
        SimpleMetadataViewInterface profile = metadataViewsWithValues.parallelStream()
                .map(MetadataViewWithValuesInterface::getMetadata).map(Optional::get)
                .filter(metadataViewInterface -> "profile".equals(metadataViewInterface.getId()))
                .map(SimpleMetadataViewInterface.class::cast).findAny().get();

        // get values
        return profile.getSelectItems(Collections.emptyList());
    }
}
