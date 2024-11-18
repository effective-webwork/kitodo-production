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

package org.kitodo.api.docket;

import java.util.ArrayList;
import java.util.List;

public class DmdSec {

    String id;
    List<String> behaeltnisse;
    List<Property> metadaten = new ArrayList<>();

    /**
     * Gets the id.
     *
     * @return The id.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id.
     *
     * @param id
     *            The id.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the Metadaten.
     *
     * @return The Metadaten.
     */
    public List<Property> getMetadaten() {
        return metadaten;
    }

    /**
     * Sets the Metadaten.
     *
     * @param metadaten
     *            The Metadaten.
     */
    public void setMetadaten(List<Property> metadaten) {
        this.metadaten = metadaten;
    }

    /**
     * Get behaeltnisse.
     *
     * @return value of behaeltnisse
     */
    public List<String> getBehaeltnisse() {
        return behaeltnisse;
    }

    /**
     * Set behaeltnisse.
     *
     * @param behaeltnisse as java.util.List<java.lang.String>
     */
    public void setBehaeltnisse(List<String> behaeltnisse) {
        this.behaeltnisse = behaeltnisse;
    }
}
