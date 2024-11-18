package org.kitodo.production.services.relayserver.helper;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.config.ConfigCore;
import org.kitodo.production.services.relayserver.RelayServerServiceConfig;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

public class XMLHelperTest {

    private static String validXML;
    private static String invalidXML;

    @BeforeClass
    public static void setUp() throws IOException {
        validXML = new String(Files.readAllBytes(Paths.get(ConfigCore.getKitodoConfigDirectory() + RelayServerServiceConfig.getDigitizationJobValidXmlFile())));
        invalidXML = new String(Files.readAllBytes(Paths.get(ConfigCore.getKitodoConfigDirectory() + RelayServerServiceConfig.getDigitizationJobInvalidXmlFile())));
    }

    @Test
    public void shouldValidateDigitizationJobXML() {
        boolean valid;

        try {
            XMLHelper.validateXMLAgainstSchema(validXML, RelayServerServiceConfig.getDigitizationJobXsdFile());
            valid = true;
        } catch (SAXException | IOException e) {
            valid = false;
        }

        assertTrue("Valid digitization job XML not validated successfully!", valid);
    }

    @Test
    public void shouldNotValidateDigitizationJobXML() {
        boolean valid;

        try {
            XMLHelper.validateXMLAgainstSchema(invalidXML, RelayServerServiceConfig.getDigitizationJobXsdFile());
            valid = true;
        } catch (SAXException | IOException e) {
            valid = false;
        }

        assertFalse("Invalid digitization job XML validated successfully!", valid);
    }

}
