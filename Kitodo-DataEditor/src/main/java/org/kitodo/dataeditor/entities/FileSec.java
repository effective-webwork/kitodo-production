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

package org.kitodo.dataeditor.entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.kitodo.dataeditor.MediaFile;
import org.kitodo.dataeditor.MetsKitodoObjectFactory;
import org.kitodo.dataformat.metskitodo.FileType;
import org.kitodo.dataformat.metskitodo.MetsType;

public class FileSec extends MetsType.FileSec {

    private final MetsKitodoObjectFactory objectFactory = new MetsKitodoObjectFactory();

    /**
     * Constructor to copy the data from parent class.
     *
     * @param fileSec
     *            The MetsType.FileSec object.
     */
    public FileSec(MetsType.FileSec fileSec) {
        super.fileGrp = fileSec.getFileGrp();
        super.id = fileSec.getID();
    }

    /**
     * Inserts MediaFile objects into fileSec of mets object.
     *
     * @param mediaFiles The list of media files.
     * @return list of inserted FileType.
     */
    public List<FileType> insertMediaFiles(List<MediaFile> mediaFiles, Map<String, String> mimeTypeMapping) {
        List<FileType> fileTypes = new ArrayList<>();
        for (MediaFile mediaFile : mediaFiles) {
            fileTypes.add(insertFileToFileGroupOfMets(mediaFile, mimeTypeMapping.get(mediaFile.getMimeType())));
        }
        writeFileIds(new HashSet<>(mimeTypeMapping.values()));
        return fileTypes;
    }

    private FileType insertFileToFileGroupOfMets(MediaFile mediaFile, String use) {
        FileType.FLocat fLocat = objectFactory.createFileTypeFLocat();
        fLocat.setLOCTYPE(mediaFile.getLocationType().toString());
        fLocat.setHref(mediaFile.getFilePath().getPath());

        FileType fileType = objectFactory.createFileType();
        fileType.setMIMETYPE(mediaFile.getMimeType());
        fileType.getFLocat().add(fLocat);

        getFileGroup(use).getFile().add(fileType);
        return fileType;
    }

    /**
     * Get the file group with the specified USE-attribute value.
     *
     * @param use value of the USE-attribute to identify the file group with
     * @return file group (will be created if it does not exist)
     */
    public MetsType.FileSec.FileGrp getFileGroup(String use) {
        if (StringUtils.isBlank(use)) {
            use = "LOCAL";
        }
        for (MetsType.FileSec.FileGrp fileGrp : this.getFileGrp()) {
            if (fileGrp.getUSE().equals(use)) {
                return fileGrp;
            }
        }
        MetsType.FileSec.FileGrp fileGroup = objectFactory.createMetsTypeFileSecFileGrpLocal();
        fileGroup.setUSE(use);
        super.fileGrp.add(fileGroup);
        return fileGroup;
    }

    /**
     * Returns the local file group of given mets object as FileGrp object.
     *
     * @return The FileGrp object.
     */
    public MetsType.FileSec.FileGrp getLocalFileGroup() {
        if (this.getFileGrp().isEmpty()) {
            MetsType.FileSec.FileGrp fileGroup = objectFactory.createMetsTypeFileSecFileGrpLocal();
            super.fileGrp.add(fileGroup);
            return fileGroup;
        }
        for (MetsType.FileSec.FileGrp fileGrp : this.getFileGrp()) {
            if (fileGrp.getUSE().equals("LOCAL")) {
                return fileGrp;
            }
        }
        throw new NoSuchElementException("No local file group in mets object");
    }

    private void writeFileIds(Set<String> uses) {
        int counter = 1;
        for (String use : uses) {
            for (FileType file : getFileGroup(use).getFile()) {
                file.setID("FILE_" + String.format("%04d", counter));
                counter++;
            }
        }
    }

}
