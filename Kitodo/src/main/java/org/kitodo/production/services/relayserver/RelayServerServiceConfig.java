package org.kitodo.production.services.relayserver;

import java.io.File;

public class RelayServerServiceConfig {
    private static final String KITODO_PROPERTIES_FILE = RelayServerServiceConfig.class.getResource("/configuration").getPath() + File.separator + "kitodoConfiguration";
    private static final String DIGITIZATION_JOB_XSD_FILE = RelayServerServiceConfig.class.getResource("/configuration").getPath() + File.separator + "Digitalisierungsauftrag.xsd";
    private static final String DIGITIZATION_JOB_XSLT_FILE = RelayServerServiceConfig.class.getResource("/xslt").getPath() + File.separator + "DigitalisierungsAuftrag2Kitodo.xsl";
    private static final String KITODO_TO_GOOBI_XSLT_FILE = RelayServerServiceConfig.class.getResource("/xslt").getPath() + File.separator + "MetsKitodo_to_MetsModsGoobi.xsl";
    private static final String CONTAINERS_XSLT_FILE = RelayServerServiceConfig.class.getResource("/xslt").getPath() + File.separator + "containers.xsl";
    private static final String DIGITIZATION_JOB_VALID_XML_FILE = "relayserver/DigitalisierungsauftragValid.xml";
    private static final String DIGITIZATION_JOB_INVALID_XML_FILE = "relayserver/DigitalisierungsauftragInvalid.xml";

    public static String getKitodoPropertiesFile() {
        return KITODO_PROPERTIES_FILE;
    }

    public static String getDigitizationJobXsdFile() {
        return DIGITIZATION_JOB_XSD_FILE;
    }

    public static String getDigitizationJobXsltFile() {
        return DIGITIZATION_JOB_XSLT_FILE;
    }

    public static String getKitodoToGoobiXsltFile() {
        return KITODO_TO_GOOBI_XSLT_FILE;
    }

    public static String getContainersXsltFile() { return CONTAINERS_XSLT_FILE; }

    public static String getDigitizationJobValidXmlFile() {
        return DIGITIZATION_JOB_VALID_XML_FILE;
    }

    public static String getDigitizationJobInvalidXmlFile() {
        return DIGITIZATION_JOB_INVALID_XML_FILE;
    }
}
