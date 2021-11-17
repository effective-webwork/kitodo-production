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

package org.kitodo.queryurlimport;

import org.w3c.dom.Element;

public class EadResponseHandler extends XmlResponseHandler {

    private static final String EAD_TITLE_PATH
            = "*[local-name()='did']/*[local-name()='unittitle' and @type='Einheitstitel']/text()";
    private static final String EAD_ID_PATH = "@id";

    @Override
    String getRecordTitle(Element record) {
        return getTextContent(record, EAD_TITLE_PATH);
    }

    @Override
    String getRecordID(Element record) {
        return getTextContent(record, EAD_ID_PATH);
    }
}
