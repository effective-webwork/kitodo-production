package org.kitodo.imagemanagement;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.kitodo.config.KitodoConfig;

public class VecteurImageConverter {

    private static final Logger logger = LogManager.getLogger(VecteurImageConverter.class);

    // *** image-service configuration ***
    // server
    private static String imageServerHost = "http://localhost:9999";
    private static String imageServerPath = "/api/v1/process";
    // credentials
    private static String userName = "vecteur";
    private static String password = "CHANGEME";
    // query parameters
    private static String wmsVersionParameter = "wmsversion";
    private static String idParameter = "id";
    private static String fileParameter = "file";
    private static String profileParameter = "profile";
    private static String manualProcessingParameter = "manual";
    private static String creatorParameter = "creator";
    private static String signatureParameter = "signature";
    // manifest filepath
    private static String manifestFilepath = "";
    // timeout (in milliseconds)
    private static long timeout = 5000;

    private final int processID;
    private final String creator;
    private final String signature;
    private String wmsVersion;

    private static final Properties imageServiceProperties = new Properties();
    private final Client client;

    static {
        try {
            loadImageServiceConfiguration();
        } catch (URISyntaxException e) {
            logger.error("Invalid URI syntax of file 'imageServiceConfiguration': " + e.getMessage());
            e.printStackTrace();
        }
    }

    public VecteurImageConverter(int id, String processCreator, String processSignature) {
        this.processID = id;
        this.creator = processCreator;
        this.signature = processSignature;
        HttpAuthenticationFeature feature =
                HttpAuthenticationFeature.basic(userName, password);
        ClientBuilder clientBuilder = ClientBuilder.newBuilder()
                .connectTimeout(timeout, TimeUnit.MILLISECONDS)
                .readTimeout(timeout, TimeUnit.MILLISECONDS);
        this.client = clientBuilder.build();
        this.client.register(feature);
    }

    public void callImageServer(String parameterLine) throws RuntimeException {
        String[] parameters = parameterLine.split(",");
        if (parameters.length != 3) {
            throw new RuntimeException("Unexpected number of parameters in one line of imageProcessingParametersFile " +
                    "(should be 3 but was " + parameters.length + ")!");
        }
        String filename = parameters[0];
        WebTarget target = client.target(imageServerHost).path(imageServerPath)
                .queryParam(wmsVersionParameter, getWMSVersion())
                .queryParam(idParameter, this.processID)
                .queryParam(fileParameter, filename)
                .queryParam(profileParameter, parameters[1])
                .queryParam(manualProcessingParameter, parameters[2])
                .queryParam(creatorParameter, this.creator)
                .queryParam(signatureParameter, this.signature);
        Response response = target.request().get();
        int status = response.getStatus();
        response.close();
        if (status != 200) {
            throw new RuntimeException("Error: image-service returned status code " + status
                    + " for image " + filename + " of process " + this.processID + "!");
        }
    }

    private static void loadImageServiceConfiguration() throws URISyntaxException {
        URI configDir = Paths.get(KitodoConfig.getParameter("directory.config")).toUri();
        URI imageServiceConfigFileURI = configDir.resolve(new URI("imageServiceConfiguration"));

        try (InputStream imageServiceConfiguration = Files.newInputStream(Paths.get(imageServiceConfigFileURI))) {
            imageServiceProperties.load(imageServiceConfiguration);
            if (!imageServiceProperties.getProperty("image_service_id_parameter").isEmpty()) {
                idParameter = imageServiceProperties.getProperty("image_service_id_parameter");
            }
            if (!imageServiceProperties.getProperty("image_service_file_parameter").isEmpty()) {
                fileParameter = imageServiceProperties.getProperty("image_service_file_parameter");
            }
            if (!imageServiceProperties.getProperty("image_service_profile_parameter").isEmpty()) {
                profileParameter = imageServiceProperties.getProperty("image_service_profile_parameter");
            }
            if (!imageServiceProperties.getProperty("image_service_manual_parameter").isEmpty()) {
                manualProcessingParameter = imageServiceProperties.getProperty("image_service_manual_parameter");
            }
            if (!imageServiceProperties.getProperty("image_service_creator_parameter").isEmpty()) {
                creatorParameter = imageServiceProperties.getProperty("image_service_creator_parameter");
            }
            if (!imageServiceProperties.getProperty("image_service_signature_parameter").isEmpty()) {
                signatureParameter = imageServiceProperties.getProperty("image_service_signature_parameter");
            }
            if (!imageServiceProperties.getProperty("image_service_username").isEmpty()) {
                userName = imageServiceProperties.getProperty("image_service_username");
            }
            if (!imageServiceProperties.getProperty("image_service_password").isEmpty()) {
                password = imageServiceProperties.getProperty("image_service_password");
            }
            if (!imageServiceProperties.getProperty("image_service_host").isEmpty()) {
                imageServerHost = imageServiceProperties.getProperty("image_service_host");
            }
            if (!imageServiceProperties.getProperty("image_service_path").isEmpty()) {
                imageServerPath = imageServiceProperties.getProperty("image_service_path");
            }
            if (!imageServiceProperties.getProperty("image_service_version_parameter").isEmpty()) {
                wmsVersionParameter = imageServiceProperties.getProperty("image_service_version_parameter");
            }
            if (!imageServiceProperties.getProperty("image_service_manifest_filepath").isEmpty()) {
                manifestFilepath = imageServiceProperties.getProperty("image_service_manifest_filepath");
            }
            if (StringUtils.isNumeric(imageServiceProperties.getProperty("image_service_timeout"))) {
                timeout = Long.parseLong(imageServiceProperties.getProperty("image_service_timeout"));
            }
        } catch (IOException e) {
            logger.error("Configuration file 'imageServiceConfiguration' not found!");
            e.printStackTrace();
        }
    }

    private String getWMSVersion() {
        if (StringUtils.isEmpty(this.wmsVersion)) {
            if (StringUtils.isEmpty(manifestFilepath)) {
                logger.error("Path to manifest file is not configured: unable to read WMS version!");
            } else {
                try (InputStream is = new FileInputStream(manifestFilepath)) {
                    Manifest manifest = new Manifest(is);
                    Attributes attributes = manifest.getMainAttributes();
                    this.wmsVersion = attributes.getValue("Implementation-Version");
                } catch (IOException e) {
                    logger.error("Error loading manifest file: " + e.getMessage());
                    e.printStackTrace();
                    this.wmsVersion = "N/A";
                }
            }
        }
        return this.wmsVersion;
    }
}
