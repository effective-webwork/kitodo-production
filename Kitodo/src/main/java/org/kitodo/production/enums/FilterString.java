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

import java.util.Locale;
import java.util.Objects;
import javax.faces.context.FacesContext;

public enum FilterString {

    TASK("step:", "schritt:"),
    TASKINWORK("stepinwork:", "schrittinarbeit:"),
    TASKLOCKED("steplocked:", "schrittgesperrt:"),
    TASKOPEN("stepopen:", "schrittoffen:"),
    TASKCURRENT("stepcurrent:", "schrittaktuell:"),
    TASKDONE("stepdone:", "schrittabgeschlossen:"),
    TASKDONETITLE("stepdonetitle:", "abgeschlossenerschritttitel:"),
    TASKDONEUSER("stepdoneuser:", "abgeschlossenerschrittbenutzer:"),
    PROJECT("project:", "projekt:"),
    ID("id:", "id:"),
    PARENTPROCESSID("parentprocessid:", "elternprozessid:"),
    PROCESS("process:", "prozess:"),
    BATCH("batch:", "gruppe:"),
    TASKAUTOMATIC("stepautomatic:", "schrittautomatisch:"),
    DURATION("duration", "laufzeit"),
    PROPERTY("property:","eigenschaft:");

    private final String filterEnglish;
    private final String filterGerman;

    /**
     * Constructor.
     *
     * @param filterEnglish
     *            English version of filter string
     * @param filterGerman
     *            German version of filter string
     */
    FilterString(String filterEnglish, String filterGerman) {
        this.filterEnglish = filterEnglish;
        this.filterGerman = filterGerman;
    }

    /**
     * Get English version of filter string.
     *
     * @return English version of filter string
     */
    public String getFilterEnglish() {
        return filterEnglish;
    }

    /**
     * Get German version of filter string.
     *
     * @return German version of filter string
     */
    public String getFilterGerman() {
        return filterGerman;
    }

    /**
     * Get filter string corresponding to Locale of current FacesContexts view root.
     *
     * @return filter string
     */
    public String getFilterString() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (Objects.nonNull(facesContext) && Objects.nonNull(facesContext.getViewRoot())) {
            if (Locale.GERMAN.equals(facesContext.getViewRoot().getLocale())) {
                return getFilterGerman();
            }
        }
        return getFilterEnglish();
    }
}
