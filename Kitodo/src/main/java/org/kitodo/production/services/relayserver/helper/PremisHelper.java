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

package org.kitodo.production.services.relayserver.helper;

import static org.kitodo.production.services.relayserver.helper.XMLHelper.loadXMLFile;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.Metadata;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.dataformat.PhysicalDivision;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.config.ConfigCore;
import org.kitodo.data.database.beans.Comment;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.enums.TaskStatus;
import org.kitodo.exceptions.PremisException;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyDocStructHelperInterface;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetadataHelper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetsModsDigitalDocumentHelper;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.relayserver.services.DocketsHandler;
import org.kitodo.production.services.relayserver.services.JobService;
import org.w3c.dom.Document;

public class PremisHelper {

    private static final String PREMIS_FILENAME = "Prozess_Digitalisierung_PREMIS.xml";
    private static final String DOSSIER = "dossier";
    private static final String DOSSIER_SIGNATURE = "sgntr_cd";
    private static final String DELIVERABLE = "deliverable";
    private static final String DELIVERER = "ablfr_prtnr_id";
    private static final String CREATE_SIP = "CreateSIP";
    private static final String CREATE_SIP_DE = "SIP erstellen";

    private static final Logger logger = LogManager.getLogger(PremisHelper.class);

    /**
     * Create initial PREMIS file for given process. This PREMIS file is augmented with 'event' elements while proceeding
     * through the workflow.
     *
     * @param process Process for which the PREMIS file is created
     * @throws ParserConfigurationException thrown if dossier signature or creator cannot be retrieved
     * @throws IOException thrown if PREMIS file cannot be saved
     */
    public static void createInitialPremisFile(Process process) throws ParserConfigurationException, IOException {
        String dossierSignature = retrieveDossierSignature(process);
        String jobCreator = retrieveJobCreator(process);
        Document premisDocument = XMLHelper.createPremisDocument(dossierSignature, jobCreator, process);
        savePremisDocument(process, premisDocument);
    }

    /**
     * Add 'event' element for given Task to PREMIS file of tasks process.
     *
     * @param task Task for which an 'event' element is created
     * @param processingTime Date of last processing time time stamp of task
     * @param comment potential correction Comment
     * @throws IOException thrown if PREMIS file cannot be saved
     * @throws PremisException if 'event' element cannot be created for given Task
     * @throws ParserConfigurationException thrown if missing PREMIS file cannot be created for process of given task
     */
    public static void addTaskEventToPremisFile(Task task, Date processingTime, Comment comment, Boolean correctionWorkflow) throws IOException, PremisException,
            ParserConfigurationException {
        if (beforeSendSIP(task.getProcess())) {
            Document premisDocument = getPremisDocument(task.getProcess());
            premisDocument = XMLHelper.addEventToPremis(premisDocument, task, comment, correctionWorkflow, processingTime);
            savePremisDocument(task.getProcess(), premisDocument);
        }
    }

    /**
     * Add 'event' element for given general Comment.
     *
     * @param comment general Comment for which an 'event' element is created
     * @throws IOException  thrown if PREMIS file cannot be saved
     * @throws PremisException if 'event' element cannot be created for given Comment
     * @throws ParserConfigurationException thrown if missing PREMIS file cannot be created for process of given comment
     */
    public static void addGeneralCommentEventToPremisFile(Comment comment) throws IOException, PremisException,
            ParserConfigurationException {
        if (beforeSendSIP(comment.getProcess())) {
            Document premisDocument = getPremisDocument(comment.getProcess());
            boolean correction = false;
            List<Task> currentTasks = ServiceManager.getProcessService().getCurrentTasks(comment.getProcess());
            if (!currentTasks.isEmpty()) {
                correction = currentTasks.stream().anyMatch(Task::isCorrection);
            }
            premisDocument = XMLHelper.addEventToPremis(premisDocument, null, comment, correction, null);
            savePremisDocument(comment.getProcess(), premisDocument);
        }
    }

    private static void savePremisDocument(Process process, Document premisDocument) throws IOException {
        if (XMLHelper.checkDoc(premisDocument)) {
            URI metaXMLURI = ServiceManager.getFileService().getMetadataFilePath(process);
            URI premisURI = URI.create(metaXMLURI.toString().replace("meta.xml", PREMIS_FILENAME));
            XMLHelper.saveXML(premisURI, XMLHelper.convertDocumentToString(premisDocument, true));
        } else {
            System.err.println("ERROR: found a text node = null!");
        }
    }

    private static String getMetadataValue(LinkedList<HashMap<String, String>> metadata, String metadataName) {
        for (HashMap<String, String> metadataMap : metadata) {
            if (metadataMap.containsKey(metadataName)) {
                return metadataMap.get(metadataName);
            }
        }
        return "";
    }

    private static String retrieveDossierSignature(Process process) throws IOException {
        LegacyMetsModsDigitalDocumentHelper meta = ServiceManager.getProcessService().readMetadataFile(process);
        LegacyDocStructHelperInterface logicalTopStruct = meta.getLogicalDocStruct();
        LinkedList<HashMap<String, String>> dossierMetadata = getRootDossierMetadata(logicalTopStruct);
        return getMetadataValue(dossierMetadata, DOSSIER_SIGNATURE);
    }

    private static String retrieveJobCreator(Process process) throws IOException {
        LegacyMetsModsDigitalDocumentHelper meta = ServiceManager.getProcessService().readMetadataFile(process);
        LegacyDocStructHelperInterface logicalTopStruct = meta.getLogicalDocStruct();
        LinkedList<HashMap<String, String>> deliverableMetadata = getDeliverableMetadata(logicalTopStruct);
        return getMetadataValue(deliverableMetadata, DELIVERER);
    }

    private static LegacyDocStructHelperInterface getRootDossier(LegacyDocStructHelperInterface topstruct) {
        return getRootElementOfType(topstruct, DOSSIER);
    }

    private static LegacyDocStructHelperInterface getRootDeliverable(LegacyDocStructHelperInterface topstruct) {
        return getRootElementOfType(topstruct, DELIVERABLE);
    }

    private static LegacyDocStructHelperInterface getRootElementOfType(LegacyDocStructHelperInterface docRoot, String type) {
        LegacyDocStructHelperInterface rootElement = null;
        for (LegacyDocStructHelperInterface currentDocStruct : docRoot.getAllChildren()) {
            if (currentDocStruct.getDocStructType().getName().equals(type)) {
                return currentDocStruct;
            } else {
                rootElement = getRootElementOfType(currentDocStruct, type);
            }
        }
        return rootElement;
    }

    /**
     * Retrieve and return metadata of root dossier element.
     * @return LinkedList containing metadata of root dossier
     */
    public static LinkedList<HashMap<String, String>> getRootDossierMetadata(LegacyDocStructHelperInterface docStructHelperInterface) {
        LegacyDocStructHelperInterface dossier = getRootDossier(docStructHelperInterface);
        return getMetadata(dossier);
    }

    private static LinkedList<HashMap<String, String>> getDeliverableMetadata(LegacyDocStructHelperInterface docStructHelperInterface) {
        if (docStructHelperInterface.getDocStructType().getName().equals(DELIVERABLE)) {
            return getMetadata(docStructHelperInterface);
        } else {
            LegacyDocStructHelperInterface deliverable = getRootDeliverable(docStructHelperInterface);
            return getMetadata(deliverable);
        }
    }

    private static LinkedList<HashMap<String, String>> getMetadata(LegacyDocStructHelperInterface structure) {
        LinkedList<HashMap<String, String>> metadataList = new LinkedList<>();
        if (Objects.nonNull(structure)) {
            for (LegacyMetadataHelper metadata : structure.getAllMetadata()) {
                HashMap<String, String> metadataMap = new HashMap<>();
                metadataMap.put(metadata.getMetadataType().getName(), metadata.getValue());
                metadataList.add(metadataMap);
            }
        }
        return metadataList;
    }

    private static boolean beforeSendSIP(Process process) {
        return process.getTasks().stream().anyMatch(t -> TaskStatus.LOCKED.equals(t.getProcessingStatus())
                && (CREATE_SIP.equals(t.getTitle()) || CREATE_SIP_DE.equals(t.getTitle())));
    }

    private static Document getPremisDocument(Process process) throws IOException, ParserConfigurationException,
            PremisException {
        URI metaXMLURI = ServiceManager.getFileService().getMetadataFilePath(process);
        URI premisURI = URI.create(metaXMLURI.toString().replace("meta.xml", PREMIS_FILENAME));
        String absolutePath = ConfigCore.getKitodoDataDirectory() + premisURI.getPath();
        // ensure premisDocument exists (may be null if 'old' process - from before PREMIS changes - is opened)!
        File premisFile = new File(absolutePath);
        if (!premisFile.exists()) {
            logger.info("PREMIS file for process '" + process.getTitle() + "' (Viaduc ID: "
                    + JobService.getViaducId(process) + ", Kitodo ID: " + process.getId()
                    + ") missing => create new initial PREMIS file and add events for all closed tasks!");
            createInitialPremisFile(process);
            Document premisDocument = loadXMLFile(absolutePath);
            // add events for closed tasks to new initial PREMIS document!
            XMLHelper.addEventsForExistingProcess(process, premisDocument);
            savePremisDocument(process, premisDocument);
        }
        return loadXMLFile(absolutePath);
    }

    public static HashMap<String, List<HashMap<String, Object>>> getTaskEventData(Process process)
            throws ParserConfigurationException, IOException, PremisException {
        HashMap<String, List<HashMap<String, Object>>> taskEventData = new HashMap<>();
        Document premisDocument = getPremisDocument(process);
        for (Task task : process.getTasks()) {
            taskEventData.put(task.getTitle(), XMLHelper.mapToEventPairs(premisDocument, task));
        }
        return taskEventData;
    }

    public static int calculateNumberOfScansForCurrentTaskRun(Task task, Date processingTime) throws IOException {
        Process process = task.getProcess();
        DocketsHandler docketsHandler = new DocketsHandler(process);
        URI metadataFileUri = ServiceManager.getProcessService().getMetadataFileUri(process);
        Workpiece workpiece = ServiceManager.getMetsService().loadWorkpiece(metadataFileUri);
        List<PhysicalDivision> partialJobs = workpiece.getAllPhysicalDivisions().stream()
                .filter(div -> "partialJob".equals(div.getType())).collect(Collectors.toList());
        String taskWorkflowCondition = Objects.nonNull(task.getWorkflowCondition()) ? task.getWorkflowCondition().getValue() : "";
        List<String> partialJobOrderLabels = new LinkedList<>();
        for (PhysicalDivision partialJob : partialJobs) {
            for (Metadata metadata : partialJob.getMetadata().stream().filter(MetadataEntry.class::isInstance).collect(Collectors.toList())) {
                if ("profile".equals(metadata.getKey()) && taskWorkflowCondition.contains(((MetadataEntry)metadata).getValue())) {
                    partialJobOrderLabels.add(partialJob.getOrderlabel());
                    break;
                }
            }
        }

        // filter by partial job order label
        List<String> scannedImages = new LinkedList<>();
        for (String imgPath : docketsHandler.getInputMedia(JobService.MEDIA_FILE_TYPES)) {
            String fileName = FilenameUtils.getBaseName(imgPath);
            String[] filenameParts = fileName.split("_");
            if (filenameParts.length >= 2 && partialJobOrderLabels.contains(filenameParts[0])) {
                scannedImages.add(imgPath);
            }
        }

        // NOTE: lastModified and processingTime both contain timestamps in GMT and thus should be comparable!
        // filter image files by timestamp and return number of those with timestamp between task start and now
        return Math.toIntExact(scannedImages.stream()
                .map(Paths::get)
                .map(Path::toFile)
                .filter(File::isFile)
                .map(File::lastModified)
                .filter(ts -> ts > processingTime.getTime())
                .count());
    }

}
