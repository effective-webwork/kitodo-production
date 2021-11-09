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

package org.kitodo.data.helper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.Metadata;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.MetadataGroup;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.api.dataformat.mets.MetsXmlElementAccessInterface;
import org.kitodo.config.KitodoConfig;
import org.kitodo.serviceloader.KitodoServiceLoader;

public class WorkpieceHelper {
    private static final Logger logger = LogManager.getLogger(WorkpieceHelper.class);

    private static MetsXmlElementAccessInterface createMetsXmlElementAccess() {
        return new KitodoServiceLoader<MetsXmlElementAccessInterface>(MetsXmlElementAccessInterface.class).loadModule();
    }

    /**
     * get Workpiece.
     * @param processBaseURI as URI
     * @return workpiece
     */
    public static Workpiece loadWorkpiece(URI processBaseURI) {
        try (InputStream inputStream = WorkpieceHelper.mapUriToKitodoDataDirectoryUri(processBaseURI).openStream()) {
            return createMetsXmlElementAccess().read(inputStream);
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage());
        }
        return new Workpiece();
    }

    private static URL mapUriToKitodoDataDirectoryUri(URI processBaseUri) throws IOException {
        URI uri = getMetadataFileUri(processBaseUri);
        String kitodoDataDirectory = KitodoConfig.getKitodoDataDirectory();
        if (uri == null) {
            return Paths.get(KitodoConfig.getKitodoDataDirectory()).toUri().toURL();
        } else {
            if (!uri.isAbsolute() && !uri.getRawPath().contains(kitodoDataDirectory)) {
                return Paths.get(KitodoConfig.getKitodoDataDirectory(), uri.getRawPath()).toUri().toURL();
            }
        }
        return uri.toURL();
    }

    private static URI getMetadataFileUri(URI workPathUri) {
        String workDirectoryPath = workPathUri.getPath();
        try {
            return new URI(workPathUri.getScheme(), workPathUri.getUserInfo(), workPathUri.getHost(),
                    workPathUri.getPort(),
                    workDirectoryPath.endsWith("/") ? workDirectoryPath.concat("meta.xml")
                            : workDirectoryPath + '/' + "meta.xml",
                    workPathUri.getQuery(), null);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    private static List<Metadata> getLogicalDivisionsMetadata(Workpiece workpiece) {
        return workpiece.getAllLogicalDivisions()
                .stream()
                .flatMap(logicalDivision -> logicalDivision.getMetadata().parallelStream())
                .filter(metadata -> !(metadata instanceof MetadataEntry)
                        || Objects.nonNull(((MetadataEntry) metadata).getValue())
                        && !((MetadataEntry) metadata).getValue().isEmpty())
                .filter(metadata -> !(metadata instanceof MetadataGroup) || Objects.nonNull(((MetadataGroup) metadata).getGroup())
                        && !((MetadataGroup) metadata).getGroup().isEmpty())
                .collect(Collectors.toList());
    }

    private static List<Metadata> getPhysicalDivisionsMetadata(Workpiece workpiece) {
        return workpiece.getAllPhysicalDivisions()
                .stream()
                .flatMap(physicalDivision -> physicalDivision.getMetadata().parallelStream())
                .filter(metadata -> !(metadata instanceof MetadataEntry)
                        || Objects.nonNull(((MetadataEntry) metadata).getValue())
                        && !((MetadataEntry) metadata).getValue().isEmpty())
                .filter(metadata -> !(metadata instanceof MetadataGroup) || Objects.nonNull(((MetadataGroup) metadata).getGroup())
                        && !((MetadataGroup) metadata).getGroup().isEmpty())
                .collect(Collectors.toList());
    }

    public static List<Metadata> getAllProcessMetadata(Workpiece workpiece) {
        List<Metadata> metadataList = getLogicalDivisionsMetadata(workpiece);
        metadataList.addAll(getPhysicalDivisionsMetadata(workpiece));
        return metadataList;
    }

}
