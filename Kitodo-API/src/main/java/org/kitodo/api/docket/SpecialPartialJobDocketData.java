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

public class SpecialPartialJobDocketData {

    private StandardPartialJobDocketData standardPartialJobDocketData;

    private String titel;

    private String entstehungszeitraum;

    public SpecialPartialJobDocketData(StandardPartialJobDocketData standardPartialJobDocketData, String titel,
                                       String entstehungszeitraum) {
        this.standardPartialJobDocketData = standardPartialJobDocketData;
        this.titel = titel;
        this.entstehungszeitraum = entstehungszeitraum;
    }

    public SpecialPartialJobDocketData() {

    }

    /**
     * Get standardPartialJobDocketData.
     *
     * @return value of standardPartialJobDocketData
     */
    public StandardPartialJobDocketData getStandardPartialJobDocketData() {
        return standardPartialJobDocketData;
    }

    /**
     * Set standardPartialJobDocketData.
     *
     * @param standardPartialJobDocketData as org.kitodo.api.docket.StandardPartialJobDocketData
     */
    public void setStandardPartialJobDocketData(StandardPartialJobDocketData standardPartialJobDocketData) {
        this.standardPartialJobDocketData = standardPartialJobDocketData;
    }

    /**
     * Get titel.
     *
     * @return value of titel
     */
    public String getTitel() {
        return titel;
    }

    /**
     * Set titel.
     *
     * @param titel as java.lang.String
     */
    public void setTitel(String titel) {
        this.titel = titel;
    }

    /**
     * Get entstehungszeitraum.
     *
     * @return value of entstehungszeitraum
     */
    public String getEntstehungszeitraum() {
        return entstehungszeitraum;
    }

    /**
     * Set entstehungszeitraum.
     *
     * @param entstehungszeitraum as java.lang.String
     */
    public void setEntstehungszeitraum(String entstehungszeitraum) {
        this.entstehungszeitraum = entstehungszeitraum;
    }
}
