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

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.LinkedList;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.kitodo.api.externaldatamanagement.SearchInterfaceType;
import org.kitodo.api.externaldatamanagement.SearchResult;
import org.kitodo.api.externaldatamanagement.SingleHit;
import org.kitodo.exceptions.CatalogException;
import org.kitodo.exceptions.ConfigException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

abstract class XmlResponseHandler {

    private static final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    private static final XMLOutputter xmlOutputter = new XMLOutputter();
    private static final XPath xPath = XPathFactory.newInstance().newXPath();

    static {
        documentBuilderFactory.setNamespaceAware(true);
        try {
            documentBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        } catch (ParserConfigurationException parserConfigurationException) {
            throw new UndeclaredThrowableException(parserConfigurationException);
        }
        xmlOutputter.setFormat(Format.getPrettyFormat());
    }

    /**
     * Create and return SearchResult for given HttpResponse.
     * @param response HttpResponse for which a SearchResult is created
     * @return SearchResult created from given HttpResponse
     */
    SearchResult getSearchResult(HttpResponse response, SearchInterfaceType interfaceType) throws XPathExpressionException {
        SearchResult searchResult = new SearchResult();
        Document resultDocument = transformResponseToDocument(response);
        if (Objects.nonNull(resultDocument)) {
            searchResult.setHits(extractHits(resultDocument, interfaceType));
            if (Objects.nonNull(interfaceType.getNumberOfRecordsString())) {
                searchResult.setNumberOfHits(extractNumberOfRecords(resultDocument, interfaceType));
            } else {
                searchResult.setNumberOfHits(searchResult.getHits().size());
            }
            if (searchResult.getNumberOfHits() < 1) {
                checkResponseDocumentForError(resultDocument, interfaceType);
            }
        }
        return searchResult;
    }

    /**
     * Check if given XML document contains an error message as defined in the given SearchInterfaceTypes error message
     * XPath. If the document contains an error, a CatalogException is thrown with the corresponding error message.
     * @param document XML Document to check for error message
     * @param interfaceType SearchInterfaceType defining XPath where to check for error message in document
     */
    public static void checkResponseDocumentForError(Document document, SearchInterfaceType interfaceType) {
        if (Objects.nonNull(document)
                && Objects.nonNull(interfaceType)
                && Objects.nonNull(interfaceType.getErrorMessageXpath())) {
            String errorMessage = getTextContent(document.getDocumentElement(),
                    interfaceType.getErrorMessageXpath());
            if (StringUtils.isNotBlank(errorMessage)) {
                errorMessage = interfaceType.getTypeString().toUpperCase() + " interface error: '" + errorMessage + "'";
                throw new CatalogException(errorMessage);
            }
        }
    }

    /**
     * Transform given HttpResponse into Document and return it.
     * @param response HttpResponse that is transformed into a Document
     * @return Document into which given HttpResponse has been transformed
     */
    private static Document transformResponseToDocument(HttpResponse response) {
        HttpEntity entity = response.getEntity();
        if (Objects.nonNull(entity)) {
            try {
                return parseXML(entity.getContent());
            } catch (IOException e) {
                throw new ConfigException(e.getMessage());
            }
        }
        throw new ConfigException("Query response is null");
    }

    private static Document parseXML(InputStream xmlSteam) {
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            return documentBuilder.parse(new InputSource(xmlSteam));
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new ConfigException(e.getMessage());
        }
    }

    private LinkedList<SingleHit> extractHits(Document document, SearchInterfaceType type) throws XPathExpressionException {
        LinkedList<SingleHit> hits = new LinkedList<>();
        NodeList records = (NodeList) xPath.evaluate(type.getRecordString(), document, XPathConstants.NODESET);
        for (int i = 0; i < records.getLength(); i++) {
            Element recordElement = (Element) records.item(i);
            hits.add(new SingleHit(getRecordTitle(recordElement), getRecordID(recordElement)));
        }
        return hits;
    }

    private static int extractNumberOfRecords(Document document, SearchInterfaceType type) {
        NodeList numberOfRecordNodes = document.getElementsByTagNameNS(type.getNamespace(), type.getNumberOfRecordsString());
        assert numberOfRecordNodes.getLength() == 1;
        Element numberOfRecordsElement = (Element) numberOfRecordNodes.item(0);
        if (Objects.nonNull(numberOfRecordsElement)) {
            return Integer.parseInt(numberOfRecordsElement.getTextContent().trim());
        }
        return 0;
    }

    static String getTextContent(Element element, String xpathString) {
        try {
            return xPath.evaluate(xpathString, element);
        } catch (XPathExpressionException e) {
            return "";
        }
    }

    abstract String getRecordTitle(Element record);

    abstract String getRecordID(Element record);
}
