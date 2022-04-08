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

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

class ModsResponseHandler extends XmlResponseHandler {

    private static final String MODS_NAMESPACE = "http://www.loc.gov/mods/v3";
    private static final String MODS_TAG = "mods";
    private static final String MODS_RECORD_ID_TAG = "recordIdentifier";
    private static final String MODS_RECORD_TITLE_TAG = "title";

    @Override
    String getRecordID(Element record) {
        Element recordIdentifier = getXmlElement(record, MODS_RECORD_ID_TAG);
        return recordIdentifier.getTextContent().trim();
    }

    @Override
    String getRecordTitle(Element record) {
        Element modsElement = getXmlElement(record, MODS_TAG);
        if (modsElement == null) {
            if (record.getElementsByTagName(MODS_TAG).getLength() >= 1) {
                modsElement = (Element) record.getElementsByTagName(MODS_TAG).item(0);
                modsElement.getParentNode().replaceChild(buildModsElementNSRecursively(modsElement), modsElement);
            }
        }
        modsElement = getXmlElement(record, MODS_TAG);
        Element recordTitle = getXmlElement(modsElement, MODS_RECORD_TITLE_TAG);
        return recordTitle.getTextContent().trim();
    }

    private static Element getXmlElement(Element parentNode, String elementTag) {
        NodeList nodeList = parentNode.getElementsByTagNameNS(ModsResponseHandler.MODS_NAMESPACE, elementTag);
        return (Element) nodeList.item(0);
    }

    private Element buildModsElementNSRecursively(Element element) {
        Element newElementNS = copyElement(element);
        NodeList list = element.getChildNodes();
        for (int j = 0; j < list.getLength(); j++) {
            if (list.item(j) instanceof Element) {
                newElementNS.appendChild(buildModsElementNSRecursively((Element) list.item(j)));
            }
        }
        return newElementNS;
    }

    private Element copyElement(Element element) {
        Element newElement = element.getOwnerDocument().createElementNS(MODS_NAMESPACE, element.getTagName());
        newElement.setPrefix(MODS_TAG);
        newElement.setTextContent(element.getTextContent());
        for (int i = 0; i < element.getAttributes().getLength(); i++) {
            Node attr = element.getAttributes().item(i);
            try {
                newElement.setAttributeNS(attr.getNamespaceURI(), attr.getLocalName(), attr.getNodeValue());
            } catch (DOMException e) {
                newElement.setAttribute(attr.getLocalName(), attr.getNodeValue());
            }
        }
        return newElement;
    }
}
