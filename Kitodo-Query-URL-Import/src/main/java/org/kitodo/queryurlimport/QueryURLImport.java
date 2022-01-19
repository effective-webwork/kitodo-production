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

package org.kitodo.queryurlimport;

import static org.apache.http.HttpStatus.SC_OK;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.externaldatamanagement.ExternalDataImportInterface;
import org.kitodo.api.externaldatamanagement.SearchInterfaceType;
import org.kitodo.api.externaldatamanagement.SearchResult;
import org.kitodo.api.externaldatamanagement.SingleHit;
import org.kitodo.api.schemaconverter.DataRecord;
import org.kitodo.api.schemaconverter.FileFormat;
import org.kitodo.api.schemaconverter.MetadataFormat;
import org.kitodo.config.OPACConfig;
import org.kitodo.exceptions.CatalogException;
import org.kitodo.exceptions.ConfigException;
import org.kitodo.exceptions.NoRecordFoundException;
import org.kitodo.exceptions.ParameterNotFoundException;
import org.kitodo.exceptions.ResponseHandlerNotFoundException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class QueryURLImport implements ExternalDataImportInterface {

    private static final Logger logger = LogManager.getLogger(QueryURLImport.class);
    private static final String NAME_ATTRIBUTE = "[@name]";
    private static final String VALUE_ATTRIBUTE = "[@value]";
    private static final String LABEL_ATTRIBUTE = "[@label]";
    private static final String HOST_CONFIG = "host";
    private static final String SCHEME_CONFIG = "scheme";
    private static final String PATH_CONFIG = "path";
    private static final String PORT_CONFIG = "port";
    private static final String PARAM_TAG = "param";
    private static final String SEARCHFIELD_TAG = "searchField";
    private static final String RETURN_FORMAT_TAG = "returnFormat";
    private static final String METADATA_FORMAT_TAG = "metadataFormat";
    private static final String MODS_RECORD_TAG = "mods";
    private static final String HTTP_PROTOCOL = "http";
    private static final String HTTPS_PROTOCOL = "https";
    private static final String FTP_PROTOCOL = "ftp";
    private static final String equalsOperand = "=";
    private static final String AND = "&";

    private SearchInterfaceType interfaceType;
    private String protocol;
    private String host;
    private String path;
    private int port = -1;
    private String idParameter;
    private String idPrefix;
    private String fileFormat;
    private String metadataFormat;
    private String username;
    private String password;
    private final Charset encoding = StandardCharsets.UTF_8;

    private LinkedHashMap<String, String> parameters = new LinkedHashMap<>();
    private final HashMap<String, String> searchFieldMapping = new HashMap<>();

    private CloseableHttpClient httpClient = HttpClientBuilder.create().build();
    private final FTPClient ftpClient = new FTPClient();

    private static final HashMap<String, XmlResponseHandler> formatHandlers;

    static {
        formatHandlers = new HashMap<>();
        formatHandlers.put(MetadataFormat.MODS.name(), new ModsResponseHandler());
        formatHandlers.put(MetadataFormat.MARC.name(), new MarcResponseHandler());
        formatHandlers.put(MetadataFormat.PICA.name(), new PicaResponseHandler());
        formatHandlers.put(MetadataFormat.EAD.name(), new EadResponseHandler());
    }

    @Override
    public DataRecord getFullRecordById(String catalogId, String identifier) throws NoRecordFoundException {
        loadOPACConfiguration(catalogId);
        LinkedHashMap<String, String> queryParameters = new LinkedHashMap<>(parameters);
        try {
            if (SearchInterfaceType.FTP.equals(interfaceType)) {
                return performFTPQueryToRecord(catalogId, identifier);
            } else {
                URI queryURL = createQueryURI(queryParameters);
                return performQueryToRecord(queryURL.toString(), identifier);
            }
        } catch (URISyntaxException e) {
            throw new ConfigException(e.getLocalizedMessage());
        }
    }

    @Override
    public List<DataRecord> getMultipleFullRecordsFromQuery(String catalogId, String field, String value, int rows) {
        loadOPACConfiguration(catalogId);
        HashMap<String, String> searchFields = new HashMap<>();
        searchFields.put(field, value);
        if (searchFieldMapping.keySet().containsAll(searchFields.keySet())) {
            // Query parameters for HTTP request
            LinkedHashMap<String, String> queryParameters = new LinkedHashMap<>(parameters);
            // Search fields and terms of query
            LinkedHashMap<String, String> searchFieldMap = getSearchFieldMap(searchFields);

            try {
                URI queryURL = createQueryURI(queryParameters);
                String queryString = queryURL + AND;
                if (Objects.nonNull(interfaceType)) {
                    if (Objects.nonNull(interfaceType.getStartRecordString())
                            && Objects.nonNull(interfaceType.getDefaultStartValue())) {
                        queryString = queryString + interfaceType.getStartRecordString() + equalsOperand
                                + interfaceType.getDefaultStartValue() + AND;
                    }
                    if (Objects.nonNull(interfaceType.getMaxRecordsString())) {
                        queryString = queryString + interfaceType.getMaxRecordsString() + equalsOperand + rows + AND;
                    }
                    if (Objects.nonNull(interfaceType.getQueryString())) {
                        queryString = queryString + interfaceType.getQueryString() + equalsOperand;
                    }
                }
                queryString = queryString + createSearchFieldString(searchFieldMap);
                return performQueryToMultipleRecords(queryString);
            } catch (URISyntaxException | IOException | ParserConfigurationException | SAXException
                    | TransformerException e) {
                logger.error(e.getLocalizedMessage());
            }
        }
        return Collections.emptyList();
    }

    @Override
    public SearchResult search(String catalogId, String field, String term, int rows) {
        loadOPACConfiguration(catalogId);
        return search(catalogId, field, term, 1, rows);
    }

    @Override
    public SearchResult search(String catalogId, String key, String value, int start, int numberOfRecords) {
        loadOPACConfiguration(catalogId);
        switch (protocol) {
            case FTP_PROTOCOL:
                return performFtpRequest(value, catalogId, start, numberOfRecords);
            case HTTP_PROTOCOL:
            case HTTPS_PROTOCOL:
                if (searchFieldMapping.containsKey(key)) {
                    return performHTTPRequest(Collections.singletonMap(key, value), start, numberOfRecords);
                }
                return null;
            default:
                throw new CatalogException("Error: unknown protocol '" + protocol + "' configured for catalog '"
                        + catalogId + "' (supported protocols are http, https and ftp)!");
        }
    }

    @Override
    public Collection<SingleHit> getMultipleEntriesById(Collection<String> ids, String catalogId) {
        return Collections.emptyList();
    }

    private void reinitializeHttpClient() throws IOException {
        httpClient.close();
        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
            CredentialsProvider provider = new BasicCredentialsProvider();
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
            provider.setCredentials(AuthScope.ANY, credentials);
            httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build();
        } else {
            httpClient = HttpClientBuilder.create().build();
        }
    }

    private SearchResult performQuery(String queryURL) throws ResponseHandlerNotFoundException {
        try {
            this.reinitializeHttpClient();
            logger.debug("Requesting: {}", queryURL);
            HttpResponse response = httpClient.execute(new HttpGet(queryURL));
            int responseStatusCode = response.getStatusLine().getStatusCode();
            if (Objects.equals(responseStatusCode, SC_OK)) {
                if (formatHandlers.containsKey(metadataFormat)) {
                    return formatHandlers.get(metadataFormat).getSearchResult(response, interfaceType);
                } else {
                    throw new ResponseHandlerNotFoundException("No ResponseHandler found for metadata format "
                            + metadataFormat);
                }
            } else {
                throw new CatalogException(response.getStatusLine().getReasonPhrase() + " (Http status code "
                        + responseStatusCode + ")");
            }
        } catch (UnknownHostException e) {
            throw new CatalogException("Unknown host: " + e.getMessage());
        } catch (ClientProtocolException e) {
            throw new CatalogException("ClientProtocolException: " + e.getMessage());
        } catch (IOException | XPathExpressionException e) {
            throw new CatalogException(e.getLocalizedMessage());
        }
    }

    private DataRecord performFTPQueryToRecord(String catalog, String identifier) {
        if (StringUtils.isBlank(host) || StringUtils.isBlank(path)) {
            throw new CatalogException("Missing host or path configuration for FTP import in OPAC configuration "
                    + "for catalog '" + catalog + "'");
        }
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            throw new CatalogException("Incomplete credentials configured for FTP import in OPAC configuration "
                    + "for catalog '" + catalog + "'");
        }
        try {
            ftpLogin();
            InputStream inputStream = ftpClient.retrieveFileStream(path + "/" + identifier);
            String stringContent = IOUtils.toString(inputStream, Charset.defaultCharset());
            inputStream.close();
            DataRecord dataRecord = createRecordFromXMLElement(stringContent);
            if (!ftpClient.completePendingCommand()) {
                throw new CatalogException("Unable to import '" + identifier + "'!");
            }
            ftpLogout();
            return dataRecord;
        } catch (IOException e) {
            throw new CatalogException(e.getLocalizedMessage());
        } finally {
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                } catch (IOException e) {
                    logger.error(e.getMessage());
                }
            }
        }
    }

    private DataRecord performQueryToRecord(String queryURL, String identifier) throws NoRecordFoundException {
        String fullUrl = queryURL + AND;
        if (Objects.nonNull(interfaceType)) {
            if (Objects.nonNull(interfaceType.getMaxRecordsString())) {
                fullUrl = fullUrl + interfaceType.getMaxRecordsString() + equalsOperand + "1&";
            }
            if (Objects.nonNull(interfaceType.getQueryString())) {
                fullUrl = fullUrl + interfaceType.getQueryString() + equalsOperand;
            }
        }
        String prefix = Objects.nonNull(idPrefix) && !identifier.startsWith(idPrefix) ? idPrefix : "";
        String queryParameter = idParameter + equalsOperand + prefix + identifier;
        if (SearchInterfaceType.SRU.equals(interfaceType)) {
            fullUrl += encodeQueryParameter(queryParameter, encoding);
        } else {
            fullUrl += queryParameter;
        }
        try {
            this.reinitializeHttpClient();
            logger.debug("Requesting: {}", fullUrl);
            HttpResponse response = httpClient.execute(new HttpGet(fullUrl));
            int responseStatusCode = response.getStatusLine().getStatusCode();
            if (Objects.equals(responseStatusCode, SC_OK)) {
                HttpEntity httpEntity = response.getEntity();
                if (Objects.isNull(httpEntity)) {
                    throw new NoRecordFoundException("No record with ID '" + identifier + "' found!");
                }
                String xmlContent = IOUtils.toString(httpEntity.getContent(), Charset.defaultCharset());
                XmlResponseHandler.checkResponseDocumentForError(stringToDocument(xmlContent), interfaceType);
                return createRecordFromXMLElement(xmlContent);
            }
            throw new CatalogException(response.getStatusLine().getReasonPhrase() + " (Http status code "
                    + responseStatusCode + ")");
        } catch (IOException e) {
            throw new ConfigException(e.getLocalizedMessage());
        } catch (ParserConfigurationException | SAXException e) {
            throw new CatalogException(e.getMessage());
        }
    }

    /**
     * Encodes a parameter value as part of the query of a URL. Characters
     * outside of the readable ASCII range are converted into hexadecimal
     * representations, preceded by a percent sign (so-called “percent coding”).
     *
     * @param value
     *            value to encode
     * @param charset
     *            charset to use for encoding
     * @return encoded value
     */
    /*
     * The exception announced by {@code URLEncoder} cannot occur if a Charset
     * object is used.
     *
     * TODO: This method can be replaced by URLEncoder.encode(String, Charset)
     * in Java ≥ 10.
     */
    private static String encodeQueryParameter(String value, Charset charset) {
        try {
            return URLEncoder.encode(value, charset.toString());
        } catch (UnsupportedEncodingException e) {
            throw new Error("Virtual machine is missing charset " + charset + '!', e);
        }
    }

    private List<DataRecord> performQueryToMultipleRecords(String queryURL) throws IOException,
            ParserConfigurationException, SAXException, TransformerException {
        List<DataRecord> records = new LinkedList<>();
        HttpGet request = new HttpGet(queryURL);
        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
        requestConfigBuilder.setConnectionRequestTimeout(3000);
        requestConfigBuilder.setConnectTimeout(3000);
        request.setConfig(requestConfigBuilder.build());
        try {
            logger.debug("Requesting: {}", queryURL);
            HttpResponse response = httpClient.execute(request);
            int responseStatusCode = response.getStatusLine().getStatusCode();
            if (Objects.equals(responseStatusCode, SC_OK)) {
                String xmlContent = IOUtils.toString(response.getEntity().getContent(), Charset.defaultCharset());
                Document document = stringToDocument(xmlContent);
                NodeList recordNodes = document.getElementsByTagName(MODS_RECORD_TAG);
                for (int i = 0; i < recordNodes.getLength(); i++) {
                    records.add(createRecordFromXMLElement(nodeToString(recordNodes.item(i))));
                }
            } else {
                throw new CatalogException(response.getStatusLine().getReasonPhrase() + " (Http status code "
                        + responseStatusCode + ")");
            }
        } catch (ConnectTimeoutException e) {
            throw new CatalogException("Connection exception: OPAC did not respond within the configured time limit!");
        }
        return records;
    }

    private SearchResult performHTTPRequest(Map<String, String> searchParameters, int start, int numberOfRecords) {
        // Query parameters for search request
        LinkedHashMap<String, String> queryParameters = new LinkedHashMap<>(parameters);
        // Search fields and terms of query
        LinkedHashMap<String, String> searchFieldMap = getSearchFieldMap(searchParameters);
        try {
            URI queryURL = createQueryURI(queryParameters);
            String queryString = queryURL + AND;
            if (Objects.nonNull(interfaceType)) {
                if (start > 0 && Objects.nonNull(interfaceType.getStartRecordString())) {
                    queryString += interfaceType.getStartRecordString() + equalsOperand + start + AND;
                }
                if (Objects.nonNull(interfaceType.getMaxRecordsString())) {
                    queryString = queryString + interfaceType.getMaxRecordsString() + equalsOperand + numberOfRecords
                            + AND;
                }
                if (Objects.nonNull(interfaceType.getQueryString())) {
                    queryString = queryString + interfaceType.getQueryString() + equalsOperand;
                }
            }
            return performQuery(queryString + createSearchFieldString(searchFieldMap));
        } catch (URISyntaxException | ResponseHandlerNotFoundException e) {
            throw new CatalogException(e.getLocalizedMessage());
        }
    }

    private SearchResult performFtpRequest(String filenamePart, String catalog, int startIndex, int rows) {
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            throw new CatalogException("Incomplete credentials configured for FTP import in OPAC configuration for "
                    + "catalog '" + catalog + "'");
        }
        SearchResult searchResult = new SearchResult();
        FTPFileFilter searchFilter = file -> file.isFile() && file.getName().contains(filenamePart);
        try {
            ftpLogin();
            FTPFile[] files = ftpClient.listFiles(path, searchFilter);
            searchResult.setNumberOfHits(files.length);
            LinkedList<SingleHit> hits = new LinkedList<>();
            for (int i = startIndex; i < Math.min(startIndex + rows, files.length); i++) {
                hits.add(new SingleHit(files[i].getName(), files[i].getName()));
            }
            searchResult.setHits(hits);
            ftpLogout();
        } catch (IOException e) {
            throw new CatalogException(e.getMessage());
        } finally {
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                } catch (IOException e) {
                    logger.error(e.getMessage());
                }
            }
        }
        return searchResult;
    }

    private DataRecord createRecordFromXMLElement(String xmlContent) {
        DataRecord record = new DataRecord();
        record.setMetadataFormat(MetadataFormat.getMetadataFormat(metadataFormat));
        record.setFileFormat(FileFormat.getFileFormat(fileFormat));
        record.setOriginalData(xmlContent);
        return record;
    }

    private URI createQueryURI(LinkedHashMap<String, String> searchFields) throws URISyntaxException {
        return new URI(protocol, null, host, port, path, createQueryParameterString(searchFields), null);
    }

    private String createQueryParameterString(LinkedHashMap<String, String> searchFields) {
        List<BasicNameValuePair> nameValuePairList = searchFields.entrySet().stream()
                .map(entry -> new BasicNameValuePair(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        return URLEncodedUtils.format(nameValuePairList, StandardCharsets.UTF_8);
    }

    private String createSearchFieldString(LinkedHashMap<String, String> searchFields) {
        List<String> searchOperands = searchFields.entrySet().stream()
                .map(entry -> entry.getKey() + equalsOperand + entry.getValue())
                .collect(Collectors.toList());
        String searchString = String.join(" AND ", searchOperands);
        if (SearchInterfaceType.SRU.equals(interfaceType)) {
            return encodeQueryParameter(searchString, encoding);
        } else {
            return searchString;
        }
    }

    private void loadOPACConfiguration(String opacName) {
        try {
            // XML configuration of OPAC
            loadServerConfiguration(OPACConfig.getOPACConfiguration(opacName));

            interfaceType = OPACConfig.getInterfaceType(opacName);
            idParameter = OPACConfig.getIdentifierParameter(opacName);
            idPrefix = OPACConfig.getIdentifierPrefix(opacName);
            fileFormat = OPACConfig.getConfigValue(opacName, RETURN_FORMAT_TAG);
            metadataFormat = OPACConfig.getConfigValue(opacName, METADATA_FORMAT_TAG);
            try {
                username = OPACConfig.getUsername(opacName);
                password = OPACConfig.getPassword(opacName);
            } catch (ConfigException | IllegalArgumentException e) {
                // ftpUserName and ftpPassword are only required for FTP servers
                if (SearchInterfaceType.FTP.equals(interfaceType)) {
                    throw new CatalogException("Missing mandatory credential configuration for FTP OPAC '" + opacName + "'");
                }
            }

            HierarchicalConfiguration searchFields = OPACConfig.getSearchFields(opacName);

            for (HierarchicalConfiguration searchField : searchFields.configurationsAt(SEARCHFIELD_TAG)) {
                searchFieldMapping.put(searchField.getString(LABEL_ATTRIBUTE), searchField.getString(VALUE_ATTRIBUTE));
            }

            HierarchicalConfiguration urlParameters = OPACConfig.getUrlParameters(opacName);

            parameters = new LinkedHashMap<>();
            for (HierarchicalConfiguration queryParam : urlParameters.configurationsAt(PARAM_TAG)) {
                parameters.put(queryParam.getString(NAME_ATTRIBUTE), queryParam.getString(VALUE_ATTRIBUTE));
            }
        } catch (IllegalArgumentException | ParameterNotFoundException e) {
            logger.error(e.getLocalizedMessage());
        }
    }

    private void loadServerConfiguration(HierarchicalConfiguration opacConfig) {
        for (HierarchicalConfiguration queryConfigParam : opacConfig.configurationsAt(PARAM_TAG)) {
            switch (queryConfigParam.getString(NAME_ATTRIBUTE)) {
                case SCHEME_CONFIG:
                    protocol = queryConfigParam.getString(VALUE_ATTRIBUTE);
                    break;
                case HOST_CONFIG:
                    host = queryConfigParam.getString(VALUE_ATTRIBUTE);
                    break;
                case PATH_CONFIG:
                    path = queryConfigParam.getString(VALUE_ATTRIBUTE);
                    break;
                case PORT_CONFIG:
                    port = queryConfigParam.getInt(VALUE_ATTRIBUTE);
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + queryConfigParam.getString(NAME_ATTRIBUTE));
            }
        }
    }

    private Document stringToDocument(String xmlContent) throws ParserConfigurationException, IOException,
            SAXException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        return documentBuilder.parse(new InputSource(new StringReader(xmlContent)));
    }

    private String nodeToString(Node node) throws TransformerException {
        StringWriter writer = new StringWriter();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(new DOMSource(node), new StreamResult(writer));
        return writer.toString();
    }

    private LinkedHashMap<String, String> getSearchFieldMap(Map<String, String> searchFields) {
        LinkedHashMap<String, String> searchFieldMap = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : searchFields.entrySet()) {
            String searchField = searchFieldMapping.get(entry.getKey());
            if (StringUtils.isNotBlank(idPrefix) && StringUtils.isNotBlank(idParameter)
                    && idParameter.equals(searchField) && !entry.getValue().startsWith(idPrefix)) {
                searchFieldMap.put(searchField, idPrefix + entry.getValue());
            } else {
                searchFieldMap.put(searchField, entry.getValue());
            }
        }
        return searchFieldMap;
    }

    private void ftpLogin() throws IOException {
        if (port != -1) {
            ftpClient.connect(host, port);
        } else {
            ftpClient.connect(host);
        }
        boolean loginSuccessful = ftpClient.login(username, password);
        if (!loginSuccessful) {
            String replyString = ftpClient.getReplyString();
            int replyCode = ftpClient.getReplyCode();
            ftpClient.logout();
            ftpClient.disconnect();
            throw new CatalogException("FTP server login failed: " + replyString + " (" + replyCode + ")");
        }
    }

    private void ftpLogout() throws IOException {
        ftpClient.logout();
        ftpClient.disconnect();
    }
}
