package org.kitodo.production.services.relayserver.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

class ZIPHelper {
    private List<String> fileList;
    private String sourceDirectory;

    /**
     * Default constructor.
     * @param sourceDirectory String containing the path to the directory to be compressed
     */
    ZIPHelper(String sourceDirectory) {
        this.sourceDirectory = sourceDirectory;
        fileList = new ArrayList<>();
    }

    /**
     * Compress the current directory and write it to the specified directory.
     * @param zipFile output ZIP file location
     */
    void compressDirectory(String zipFile) {
        byte[] buffer = new byte[1024];

        try {
            FileOutputStream fos = new FileOutputStream(new File(zipFile));
            ZipOutputStream zos = new ZipOutputStream(fos);

            for (String file : this.fileList) {
                ZipEntry ze = new ZipEntry(file);
                zos.putNextEntry(ze);

                FileInputStream in = new FileInputStream(sourceDirectory + File.separator + file);

                int len;
                while ((len = in.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }

                in.close();
            }

            zos.closeEntry();
            zos.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Scan a directory and get all files and add the files into fileList.
     * @param node file or directory
     */
    void generateFileList(File node) {

        // add files to the file list
        if (node.isFile()) {
            fileList.add(generateZipEntry(node.getAbsoluteFile().toString()));
        }

        // generate file list subnode for source subdirectory
        if (node.isDirectory()) {
            String[] subNodes = node.list();
            if (Objects.nonNull(subNodes)) {
                for (String filename : subNodes) {
                    generateFileList(new File(node, filename));
                }
            }
        }

    }

    /**
     * Format the file path for zip.
     * @param file file path
     * @return Formatted file path
     */
    private String generateZipEntry(String file) {
        return file.substring(sourceDirectory.length() + 1, file.length());
    }
}
