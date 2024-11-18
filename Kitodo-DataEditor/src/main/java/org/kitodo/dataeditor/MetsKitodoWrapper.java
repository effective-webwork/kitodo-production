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

package org.kitodo.dataeditor;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.lang3.StringUtils;
import org.kitodo.dataeditor.entities.DmdSec;
import org.kitodo.dataeditor.entities.FileSec;
import org.kitodo.dataeditor.entities.LogicalStructMapType;
import org.kitodo.dataeditor.entities.PhysicalStructMapType;
import org.kitodo.dataeditor.entities.StructLink;
import org.kitodo.dataeditor.handlers.MetsKitodoStructMapHandler;
import org.kitodo.dataformat.metskitodo.DivType;
import org.kitodo.dataformat.metskitodo.FileType;
import org.kitodo.dataformat.metskitodo.KitodoType;
import org.kitodo.dataformat.metskitodo.MdSecType;
import org.kitodo.dataformat.metskitodo.MetadataType;
import org.kitodo.dataformat.metskitodo.Mets;
import org.kitodo.dataformat.metskitodo.MetsType;
import org.kitodo.dataformat.metskitodo.StructMapType;

/**
 * This is a wrapper class for holding and manipulating the content of a
 * serialized mets-kitodo format xml file.
 */
public class MetsKitodoWrapper {
    private static final String LOG_ID_PREFIX = "LOG_";

    private Mets mets;
    private MetsKitodoObjectFactory objectFactory = new MetsKitodoObjectFactory();
    private LogicalStructMapType logicalStructMapType;
    private PhysicalStructMapType physicalStructMapType;

    /**
     * Gets the mets object.
     *
     * @return The mets object.
     */
    public Mets getMets() {
        return mets;
    }

    /**
     * Constructor which creates a Mets object with corresponding object factory and
     * also inserts the basic mets elements (FileSec with local file group,
     * StructLink, MetsHdr, physical and logical StructMap).
     *
     * @param documentType
     *            The type of the document which will be used for setting the
     *            logical root div type.
     */
    public MetsKitodoWrapper(String documentType) throws DatatypeConfigurationException, IOException {
        this.mets = objectFactory.createMets();
        createBasicMetsElements(this.mets);
        createLogicalRootDiv(this.mets, documentType);
    }

    private void createBasicMetsElements(Mets mets) throws DatatypeConfigurationException, IOException {
        if (Objects.isNull(mets.getFileSec())) {
            mets.setFileSec(objectFactory.createFileSec());
            MetsType.FileSec.FileGrp fileGroup = objectFactory.createMetsTypeFileSecFileGrpLocal();
            mets.getFileSec().getFileGrp().add(fileGroup);
        }
        if (Objects.isNull(mets.getStructLink())) {
            mets.setStructLink(objectFactory.createStructLink());
        }
        if (Objects.isNull(mets.getMetsHdr())) {
            mets.setMetsHdr(objectFactory.createKitodoMetsHeader());
        }
        if (mets.getStructMap().isEmpty()) {
            LogicalStructMapType logicalStructMapType = objectFactory.createLogicalStructMapType();
            mets.getStructMap().add(logicalStructMapType);
            this.logicalStructMapType = logicalStructMapType;

            PhysicalStructMapType physicalStructMapType = objectFactory.createPhysicalStructMapType();
            mets.getStructMap().add(physicalStructMapType);
            this.physicalStructMapType = physicalStructMapType;
        }
    }

    private void createLogicalRootDiv(Mets mets, String type) {
        MdSecType dmdSecOfLogicalRootDiv = objectFactory.createDmdSecByKitodoMetadata(objectFactory.createKitodoType(),
            "DMDLOG_ROOT");
        mets.getDmdSec().add(dmdSecOfLogicalRootDiv);
        getLogicalStructMap().setDiv(objectFactory.createRootDivTypeForLogicalStructMap(type, dmdSecOfLogicalRootDiv));
    }

    public String createLogicalDiv(String type, String dossierId, String physicalDivId) {
        // create Metadata for DmDSec
        KitodoType kitodoMetadata = objectFactory.createKitodoType();
        MetadataType title = objectFactory.createMetadataType();
        title.setName("vrzng_enht_titel");
        switch (type.toLowerCase()) {
            case "umschlag":
                title.setValue("Umschlag");
                break;
            case "dokument":
                title.setValue("Dokument");
                break;
            case "dokument-fortsetzung":
                title.setValue("Dokument-Fortsetzung");
                break;
            case "unterlagen":
                title.setValue("Unterlagen");
                break;
        }
        kitodoMetadata.getMetadata().add(title);
        switch (type.toLowerCase()) {
            case "dokument":
                type = "document";
                break;
            case "dokument-fortsetzung":
                type = "document_installment";
                break;
            case "unterlagen":
                type = "papers";
                break;
            default:
                // No default action needed.
        }
        String dmdId = "DMDLOG_".concat(String.format("%04d", this.getMets().getDmdSec().size() - 1));
        MdSecType dmdSecOfLogicalDiv = objectFactory.createDmdSecByKitodoMetadata(kitodoMetadata, dmdId);
        this.mets.getDmdSec().add(dmdSecOfLogicalDiv);
        String logID = LOG_ID_PREFIX + String.format("%04d", getMaxId(logicalStructMapType.getDiv(), LOG_ID_PREFIX) + 1);
        DivType newLogDiv = objectFactory.createDivTypeforMdSecType(logID, type.toLowerCase(), dmdSecOfLogicalDiv);

        DivType dossierDiv = logicalStructMapType.getDiv();
        dossierDiv = findDivTypeById(dossierDiv, dossierId);

        // select first dossier or deepest element if dossierId could not be found
        if (!dossierDiv.getID().equals(dossierId)) {
            while (!dossierDiv.getTYPE().equals("dossier") && dossierDiv.getDiv().size() > 0) {
                dossierDiv = dossierDiv.getDiv().get(0);
            }
        }

        DivType physicalDiv = findDivTypeById(physicalStructMapType.getDiv(), physicalDivId);
        try {
            DivType precedingLogDiv = findPrecedingLogicalDiv(dossierDiv, physicalDivId);
            if (Objects.nonNull(precedingLogDiv) && dossierDiv.getDiv().indexOf(precedingLogDiv) < dossierDiv.getDiv().size() - 1) {
                dossierDiv.getDiv().add(dossierDiv.getDiv().indexOf(precedingLogDiv) + 1, newLogDiv);
            } else {
                DivType parentPhysicalDiv = findParentById(physicalStructMapType.getDiv(), physicalDivId);
                if ((Objects.nonNull(precedingLogDiv) && dossierDiv.getDiv().indexOf(precedingLogDiv) != dossierDiv.getDiv().size() - 1
                        || Objects.isNull(precedingLogDiv))
                        && Objects.nonNull(physicalDiv) && parentPhysicalDiv.getDiv().indexOf(physicalDiv) == 0) {
                    dossierDiv.getDiv().add(0, newLogDiv);
                } else {
                    dossierDiv.getDiv().add(newLogDiv);
                }
            }
        } catch (IllegalArgumentException e) {
            /*
                Catching this exception means we could not find a preceding logical div which also is a child of dossierDiv.
                This can occur when newLogDiv is the first element in a "subdossier". In this case dossierDiv represents the
                "subdossier" while the last page can only be found in another "subdossier" or "dossier" thus not being a child of
                dossierDiv.
             */
            dossierDiv.getDiv().add(0, newLogDiv);
        }
        linkDivsWithMetadata(newLogDiv, physicalDiv);
        return newLogDiv.getID();
    }

    private void linkDivsWithMetadata(DivType logDiv, DivType physDiv) {
        MetadataType linkedLogDiv = objectFactory.createMetadataType();
        linkedLogDiv.setName("generated_structure");
        linkedLogDiv.setValue(logDiv.getID());
        if (!physDiv.getDMDID().isEmpty()) {
            MdSecType mdSecType = (MdSecType) physDiv.getDMDID().get(0);
            for (DmdSec dmdSec : getDmdSecs()) {
                if (Objects.equals(mdSecType.getID(), dmdSec.getID())) {
                    List<MetadataType> metadata = dmdSec.getKitodoType().getMetadata();
                    metadata.add(linkedLogDiv);
                    return;
                }
            }
        }
    }

    /**
     * Find the the last logical Div for the physical Div preceding the specified one.
     * @param logParent logical Div where the new element should be added
     * @param physicalDivId id of current physical element
     * @return logical element representing the index after which the new elements should be added
     */
    private DivType findPrecedingLogicalDiv(DivType logParent, String physicalDivId) {
        DivType physicalDiv = findDivTypeById(physicalStructMapType.getDiv(), physicalDivId);
        DivType precedingLogDiv = findLastLogDivForPhysDiv(physicalDiv, logParent);
        if (Objects.nonNull(precedingLogDiv)) {
            return precedingLogDiv;
        }
        DivType parentPhysicalDiv = findParentById(physicalStructMapType.getDiv(), physicalDivId);
        if (Objects.nonNull(parentPhysicalDiv)) {
            int precedingPhysicalDivIndex = parentPhysicalDiv.getDiv().indexOf(physicalDiv) - 1;
            DivType precedingPhysDiv = precedingPhysicalDivIndex >= 0 ? parentPhysicalDiv.getDiv().get(precedingPhysicalDivIndex) : null;
            return findLastLogDivForPhysDiv(precedingPhysDiv, logParent);
        }
        return null;
    }

    private DivType findLastLogDivForPhysDiv(DivType physicalDiv, DivType logicalParent) {
        if (Objects.nonNull(physicalDiv) && Objects.nonNull(logicalParent)) {
            if (!physicalDiv.getDiv().isEmpty()) {
                DivType lastPage = physicalDiv.getDiv().get(physicalDiv.getDiv().size() - 1);
                List<String> logicalDivIds = getStructLink().getLogicalDivIdsByPhysicalDiv(lastPage);
                if (!logicalDivIds.isEmpty()) {
                    DivType lastLogDiv = findDivTypeById(logicalParent, logicalDivIds.get(logicalDivIds.size() - 1));
                    if (Objects.nonNull(lastLogDiv)) {
                        return findDirectChild(logicalParent, lastLogDiv);
                    }
                }
            }
        }
        return null;
    }

    private DivType findDirectChild(DivType parent, DivType descendant) {
        if (parent.getDiv().contains(descendant)) {
            return descendant;
        }
        for (DivType child : parent.getDiv()) {
            DivType possibleMatch = findDirectChildRecursive(child, descendant);
            if (Objects.nonNull(possibleMatch)) {
                return child;
            }
        }
        throw new IllegalArgumentException("DivTypes are not related");
    }

    private DivType findDirectChildRecursive(DivType parent, DivType descendant) {
        if (parent.getDiv().contains(descendant)) {
            return parent;
        }
        for (DivType child : parent.getDiv()) {
            DivType possibleMatch = findDirectChildRecursive(child, descendant);
            if (Objects.nonNull(possibleMatch)) {
                return parent;
            }
        }
        return null;
    }

    public DivType findDivTypeById(DivType divType, String id) {
        return treeStream(divType).filter(d -> Objects.equals(d.getID(), id)).findFirst().orElse(divType);
    }

    /**
     * Find the parent's id of a DivType which is identified by its id.
     * @param divType DivTpye to search all children for the given id
     * @param id Id of the known DivType
     * @return Id of the DivType's parent
     */
    public String findParentIdById(DivType divType, String id) {
        return findParentById(divType, id).getID();
    }

    /**
     * Find the parent of a DivType which is identified by its id.
     * @param divType DivType to search all children for the given id
     * @param id Id of the known DivType
     * @return parent of the DivType
     */
    private DivType findParentById(DivType divType, String id) {
        return treeStream(divType)
                .filter(div -> div.getDiv().stream().anyMatch(child -> Objects.equals(child.getID(), id)))
                .findFirst()
                .orElse(divType);
    }

    /**
     * Get the type for the element preceding the element with the specified id.
     * @param elementId id of the element which predecessor should be found
     * @return type of the preceding element as java.lang.String
     */
    public String getPrecedingElementType(String elementId) {
        DivType previousElement = getPrecedingElement(elementId);
        return Objects.nonNull(previousElement) ? previousElement.getTYPE() : "";
    }

    /**
     * Get the type for the element preceding the element with the specified id.
     * @param elementId id of the element which predecessor should be found
     * @return type of the preceding element as java.lang.String
     */
    public DivType getPrecedingElement(String elementId) {
        DivType parent = findParentById(this.logicalStructMapType.getDiv(), elementId);
        DivType previousElement = null;
        if (Objects.nonNull(parent)) {
            for (DivType element : parent.getDiv()) {
                if (Objects.nonNull(element) && Objects.equals(element.getID(), elementId)) {
                    break;
                }
                previousElement = element;
            }
        }
        return previousElement;
    }

    /**
     * Constructor which creates Mets object by unmarshalling given xml file of
     * mets-kitodo format.
     *
     * @param xmlFile
     *            The xml file in mets-kitodo format as URI.
     * @param xsltFile
     *            The URI to the xsl file for transformation of old format goobi
     *            metadata files.
     */
    public MetsKitodoWrapper(URI xmlFile, URI xsltFile)
            throws JAXBException, TransformerException, IOException, DatatypeConfigurationException {
        this.mets = MetsKitodoReader.readAndValidateUriToMets(xmlFile, xsltFile);
        replaceStandardMetsElementsByCustomEntities(this.mets);
        createBasicMetsElements(this.mets);
    }

    /**
     * Constructor which creates Mets object by unmarshalling given xml file of
     * mets-kitodo format.
     *
     * @param xmlFile
     *            The xml file in mets-kitodo format as URI.
     */
    public MetsKitodoWrapper(URI xmlFile)
            throws JAXBException, IOException, DatatypeConfigurationException {
        this.mets = MetsKitodoReader.readUriToMets(xmlFile);
        replaceStandardMetsElementsByCustomEntities(this.mets);
        createBasicMetsElements(this.mets);
    }

    private void replaceStandardMetsElementsByCustomEntities(Mets mets) {
        if (Objects.nonNull(mets.getStructLink())) {
            mets.setStructLink(new StructLink(mets.getStructLink()));
        }
        if (Objects.nonNull(mets.getFileSec())) {
            mets.setFileSec(new FileSec(mets.getFileSec()));
        }
        if (!mets.getStructMap().isEmpty()) {
            Optional<StructMapType> optionalPhysicalStructMap = MetsKitodoStructMapHandler.getMetsStructMapByType(mets,
                "PHYSICAL");
            if (optionalPhysicalStructMap.isPresent()) {
                this.mets.getStructMap().remove(optionalPhysicalStructMap.get());
                this.physicalStructMapType = new PhysicalStructMapType(optionalPhysicalStructMap.get());
                this.mets.getStructMap().add(this.physicalStructMapType);
            }

            Optional<StructMapType> optionalLogicalStructMap = MetsKitodoStructMapHandler.getMetsStructMapByType(mets,
                "LOGICAL");
            if (optionalLogicalStructMap.isPresent()) {
                this.mets.getStructMap().remove(optionalLogicalStructMap.get());
                this.logicalStructMapType = new LogicalStructMapType(optionalLogicalStructMap.get());
                this.mets.getStructMap().add(this.logicalStructMapType);
            }
        }
        if (!mets.getDmdSec().isEmpty()) {
            List<DmdSec> newDmdSecElements = new ArrayList<>();
            for (MdSecType mdSecType : mets.getDmdSec()) {
                newDmdSecElements.add(new DmdSec(mdSecType));
            }
            this.mets.getDmdSec().clear();
            this.mets.getDmdSec().addAll(newDmdSecElements);
        }
    }

    /**
     * Gets a list of MdSecType elements.
     *
     * @return The list if MdSecType objects.
     */
    public List<DmdSec> getDmdSecs() {
        return (List<DmdSec>)(List<?>) this.mets.getDmdSec();
    }

    /**
     * Inserts MediaFile objects into fileSec of the wrapped mets document and
     * creates corresponding physical structMap.
     *
     * @param files
     *            The list of MediaFile objects.
     * @return the list of added DivTypes.
     */
    public List<DivType> insertMediaFiles(List<MediaFile> files, String physicalDivId, Map<String, String> mimeTypeMapping)
            throws Exception {
        List<FileType> fileTypes = getFileSec().insertMediaFiles(files, mimeTypeMapping);
        // TODO implement logic to check if pagination is set to automatic or not
        return getPhysicalStructMap().createDivsByFileTypes(fileTypes, physicalDivId);
    }

    /**
     * Returns the physical StructMap of the wrapped mets document.
     *
     * @return The PhysicalStructMapType object.
     */
    public PhysicalStructMapType getPhysicalStructMap() {
        return this.physicalStructMapType;
    }

    /**
     * Returns the logical StructMap of the wrapped mets document.
     *
     * @return The LogicalStructMapType object.
     */
    public LogicalStructMapType getLogicalStructMap() {
        return this.logicalStructMapType;
    }

    /**
     * Returns the FileSec of the wrapped mets document.
     *
     * @return The FileSec object.
     */
    public FileSec getFileSec() {
        return (FileSec) this.mets.getFileSec();
    }

    /**
     * Returns the structLink of the wrapped mets document.
     *
     * @return The StructLink object.
     */
    public StructLink getStructLink() {
        return (StructLink) this.mets.getStructLink();
    }

    /**
     * Returns the first KitodoType object and its metadata of an DmdSec element
     * which is referenced by a given logical divType object.
     *
     * @param div
     *            The DivType object which is referencing the DmdSec by DMDID.
     * @return The KitodoType object.
     */
    public KitodoType getFirstKitodoTypeOfLogicalDiv(DivType div) {
        List<Object> objects = div.getDMDID();
        if (!objects.isEmpty()) {
            MdSecType mdSecType = (MdSecType) div.getDMDID().get(0);
            return JaxbXmlUtils.getKitodoTypeOfDmdSecElement(mdSecType);
        }
        throw new NoSuchElementException("Div element with id: " + div.getID() + " does not have metadata!");
    }

    /**
     * Returns a list of divs from physical structMap which are linked by a given
     * div from logical structMap.
     *
     * @param logicalDiv
     *            The logical div which links to physical divs.
     * @return A list of physical divs.
     */
    public List<DivType> getPhysicalDivsByLinkingLogicalDiv(DivType logicalDiv) {
        return getPhysicalStructMap().getDivsByIds(getStructLink().getPhysicalDivIdsByLogicalDiv(logicalDiv));
    }

    /**
     * Adds smLinks to structLink section for a given logical div by checking the
     * linked physical divs of the logical child divs.
     *
     * @param logicalDiv
     *            The logical div.
     */
    public void linkLogicalDivByInheritFromChildDivs(DivType logicalDiv) {
        List<DivType> physicalDivs = new ArrayList<>();
        for (DivType div : logicalDiv.getDiv()) {
            physicalDivs.addAll(getPhysicalDivsByLinkingLogicalDiv(div));
        }
        getStructLink().addSmLinks(logicalDiv, physicalDivs);
    }

    public void editHrefFLocat(String oldhref, String newHref, Set<String> uses) {

        List<FileType> fileTypeList = new ArrayList<>();
        uses.stream().map(use -> ((FileSec) this.mets.getFileSec()).getFileGroup(use).getFile()).forEach(fileTypeList::addAll);
        for (FileType file : fileTypeList) {
            if (!file.getFLocat().isEmpty() && file.getFLocat().get(0).getHref().equals(oldhref)) {
                file.getFLocat().get(0).setHref(newHref);
            }
        }
    }

    public void addPartialJob(String profileValue, String pagesNumber, String startPage, String manualImageProcessing) {
        KitodoType partialJobMetadata = objectFactory.createKitodoType();
        MetadataType profil = objectFactory.createMetadataType();
        profil.setName("profile");
        profil.setValue(profileValue);
        partialJobMetadata.getMetadata().add(profil);
        MetadataType numberOfPages = objectFactory.createMetadataType();
        numberOfPages.setName("numberImages");
        numberOfPages.setValue(pagesNumber);
        partialJobMetadata.getMetadata().add(numberOfPages);
        MetadataType startPageNumber = objectFactory.createMetadataType();
        startPageNumber.setName("startPage");
        startPageNumber.setValue(startPage);
        partialJobMetadata.getMetadata().add(startPageNumber);
        MetadataType manualProcessing = objectFactory.createMetadataType();
        manualProcessing.setName("manualImageProcessing");
        manualProcessing.setValue(manualImageProcessing);
        partialJobMetadata.getMetadata().add(manualProcessing);

        String dmdId = "DMDPHYS_".concat(String.format("%04d", physicalStructMapType.getPhysicalStructMapSize()));
        MdSecType dmdSecOfPartialJobDiv = objectFactory.createDmdSecByKitodoMetadata(partialJobMetadata, dmdId);
        this.mets.getDmdSec().add(dmdSecOfPartialJobDiv);

        DivType boundBookDiv = physicalStructMapType.getDiv();
        String physID = "PHYS_".concat(String.format("%04d", physicalStructMapType.getPhysicalStructMapSize()));
        DivType partialJobDiv = objectFactory.createDivTypeforMdSecType(physID, "partialJob", dmdSecOfPartialJobDiv);
        boundBookDiv.getDiv().add(partialJobDiv);
    }

    public void editPartialJob(Object divTypeObject, String profileValue, String numberOfPages, String firstPage, String manualImageProcessing) {
        if (!(divTypeObject instanceof DivType)) {
            throw new ClassCastException("Argument has wrong class '" + divTypeObject.getClass().getName() + "' (DivType expected)");
        }
        DivType divType = (DivType) divTypeObject;

        if (divType.getDMDID().size() > 0) {
            MdSecType mdSecType = (MdSecType) divType.getDMDID().get(0);

            for (DmdSec dmdSec : getDmdSecs()) {
                if (!divType.getDMDID().isEmpty() && dmdSec.getID().equals(mdSecType.getID())) {
                    for (MetadataType metadataType : dmdSec.getKitodoType().getMetadata()) {
                        switch (metadataType.getName()) {
                            case "profile":
                                metadataType.setValue(profileValue);
                                break;
                            case "numberImages":
                                metadataType.setValue(numberOfPages);
                                break;
                            case "startPage":
                                metadataType.setValue(firstPage);
                                break;
                            case "manualImageProcessing":
                                metadataType.setValue(manualImageProcessing);
                                break;
                        }
                    }
                    break;
                }
            }
        }
    }

    private int getMaxId(DivType divType, String idPrefix) {
        return treeStream(divType)
                .filter(element -> element.getID().startsWith(idPrefix))
                .filter(element -> StringUtils.isNumeric(element.getID().replace(idPrefix, "")))
                .max(Comparator.comparingInt(o -> Integer.parseInt(o.getID().replace(idPrefix, ""))))
                .map(element -> Integer.parseInt(element.getID().replace(idPrefix, "")))
                .orElse(0);
    }

    /**
     * Generates a stream of nodes from structure tree.
     *
     * @param tree
     *            starting node
     * @return all nodes as stream
     */
    @SuppressWarnings("unchecked")
    public static <T extends DivType> Stream<T> treeStream(DivType tree) {
        return Stream.concat(Stream.of((T) tree), tree.getDiv().stream().flatMap(MetsKitodoWrapper::treeStream));
    }
}
