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

package org.kitodo.api.dataeditor;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.primefaces.model.TreeNode;
import org.w3c.dom.Document;



/**
 * Enables the user to read and write Metadata in an editor.
 */
public interface DataEditorInterface {

    /**
     * Opens an editor to read an xmlfile.
     *
     * @param xmlFileUri
     *            The URI to the xml file to read.
     * @param xsltFileUri
     *            The URI to the xsl file for transformation of old format goobi metadata files.
     */
    void readData(URI xmlFileUri, URI xsltFileUri) throws IOException;

    /**
     * Opens an editor to read an xmlfile.
     *
     * @param xmlFileUri
     *            The URI to the xml file to read.
     */
    void readData(URI xmlFileUri) throws IOException;

    /**
     * Opens an editor to edit an xmlfile.
     *
     * @param xmlFileUri
     *            The URI to the xml file to edit.
     * @param rulesetFileUri
     *            The URI to the rulesetFile.
     * @return true, if editing was successful, false otherwise.
     */
    boolean editData(URI xmlFileUri, URI rulesetFileUri);

    void createData(String documentType) throws IOException;

    String createKitodoDocument(Document kitodoMetadata);

    void insertMediaFilesAndAddStructLink(List<String> mediaFilePaths, String physicalDivId, String logId,
                                          Map<String, String> mimeTypeMapping, Map<String, String> suffixMapping) throws Exception;

    void linkMediaFromPrecedingElement(String elementId);

    String addNewStructElement(String logDivtype, String dossierId, String physicalDivId);

    boolean isLogElementPresent(String logId);

    boolean isPhysElementPresent(String physId);

    String getBoundBookElementPhysicalID();

    String getParentDossierId(String dossierId);

    String getPrecedingDocumentType(String documentId);

    String getUpdatedXmlString();

    void editHrefFLocat(String oldHref, String newHref, Set<String> uses);

    void addPartialJob(Object divType, String selectedProfil, String numberOfPages, String startPage, String manualImageProcessing, URI fileURI);

    void deletePartialJob(Object divType, URI fileURI);

    TreeNode getPhysicalStructMap();

    List<HashMap<String, String>> getMetadataOfDivType(Object divTypeObject) throws ClassCastException;

    String createImageProcessingParameterFileContent(String previewFolder, String mediaViewFolder);

    List<String> deletePartialJobImages(String partialJobId, Set<String> uses);

    void removePreviewImages(String fileGroupName);

    List<String> getPartialJobMedia(String partialJobId, Set<String> uses);

    List<String> getLogicalElementMedia(String elementId, Set<String> uses);

}
