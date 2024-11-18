package org.kitodo.production.services.relayserver.helper.validator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.production.services.relayserver.services.SIPHandler;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

import java.io.*;
import java.util.Objects;

public class ResourceResolver implements LSResourceResolver {

    private String basePath;
    private static final Logger logger = LogManager.getLogger(SIPHandler.class);

    public ResourceResolver(String basePath) {
        this.basePath = basePath;
    }

    @Override
    public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
        LSInputImplementation input = new LSInputImplementation();
        InputStream inputStream = null;

        try {
            File initialFile = new File(buildPath(systemId));
            inputStream = new FileInputStream(initialFile);
        } catch (FileNotFoundException e) {
            logger.error("Error reading file: " + buildPath(systemId));
        }

        Objects.requireNonNull(inputStream, String.format("Could not find the specified xsd file: %s \nat path: %s", systemId, buildPath(systemId)));

        input.setPublicId(publicId);
        input.setSystemId(systemId);
        input.setBaseURI(baseURI);
        input.setCharacterStream(new InputStreamReader(inputStream));

        return input;
    }

    private String buildPath(String systemId) {
        if (Objects.isNull(basePath)) {
            return systemId;
        }
        basePath = basePath.endsWith(File.separator) ? basePath : basePath + File.separator;
        basePath = basePath.startsWith(File.separator) ? basePath : File.separator + basePath;
        return basePath + systemId;
    }
}
