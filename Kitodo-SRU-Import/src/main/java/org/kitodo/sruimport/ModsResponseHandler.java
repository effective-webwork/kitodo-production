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

package org.kitodo.sruimport;

import org.w3c.dom.Element;

class ModsResponseHandler extends XmlResponseHandler {

    private static final String MODS_NAMESPACE = "http://www.loc.gov/mods/v3";
    private static final String MODS_TAG = "mods";
    private static final String MODS_RECORD_ID_TAG = "recordIdentifier";
    private static final String MODS_RECORD_TITLE_TAG = "title";

    @Override
    String getRecordID(Element record) {
        Element recordIdentifier = getXmlElement(record, MODS_RECORD_ID_TAG, MODS_NAMESPACE);
        return recordIdentifier.getTextContent().trim();
    }

    @Override
    String getRecordTitle(Element record) {
        Element modsElement = getXmlElement(record, MODS_TAG, MODS_NAMESPACE);
        Element recordTitle = getXmlElement(modsElement, MODS_RECORD_TITLE_TAG, MODS_NAMESPACE);
        return recordTitle.getTextContent().trim();
    }
}
