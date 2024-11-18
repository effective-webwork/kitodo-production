package org.kitodo.production.services.relayserver.helper.validator;

import org.w3c.dom.ls.LSInput;

import java.io.InputStream;
import java.io.Reader;

public class LSInputImplementation implements LSInput {
    private Reader characterStream;
    private InputStream byteStream;
    private String stringData;
    private String systemId;
    private String publicId;
    private String baseURI;
    private String encoding;
    private boolean certifiedText;

    /**
     * Get characterStream.
     *
     * @return value of characterStream
     */
    @Override
    public Reader getCharacterStream() {
        return characterStream;
    }

    /**
     * Set characterStream.
     *
     * @param characterStream as java.io.Reader
     */
    @Override
    public void setCharacterStream(Reader characterStream) {
        this.characterStream = characterStream;
    }

    /**
     * Get byteStream.
     *
     * @return value of byteStream
     */
    @Override
    public InputStream getByteStream() {
        return byteStream;
    }

    /**
     * Set byteStream.
     *
     * @param byteStream as java.io.InputStream
     */
    @Override
    public void setByteStream(InputStream byteStream) {
        this.byteStream = byteStream;
    }

    /**
     * Get stringData.
     *
     * @return value of stringData
     */
    @Override
    public String getStringData() {
        return stringData;
    }

    /**
     * Set stringData.
     *
     * @param stringData as java.lang.String
     */
    @Override
    public void setStringData(String stringData) {
        this.stringData = stringData;
    }

    /**
     * Get systemId.
     *
     * @return value of systemId
     */
    @Override
    public String getSystemId() {
        return systemId;
    }

    /**
     * Set systemId.
     *
     * @param systemId as java.lang.String
     */
    @Override
    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    /**
     * Get publicId.
     *
     * @return value of publicId
     */
    @Override
    public String getPublicId() {
        return publicId;
    }

    /**
     * Set publicId.
     *
     * @param publicId as java.lang.String
     */
    @Override
    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    /**
     * Get baseURI.
     *
     * @return value of baseURI
     */
    @Override
    public String getBaseURI() {
        return baseURI;
    }

    /**
     * Set baseURI.
     *
     * @param baseURI as java.lang.String
     */
    @Override
    public void setBaseURI(String baseURI) {
        this.baseURI = baseURI;
    }

    /**
     * Get encoding.
     *
     * @return value of encoding
     */
    @Override
    public String getEncoding() {
        return encoding;
    }

    /**
     * Set encoding.
     *
     * @param encoding as java.lang.String
     */
    @Override
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * Get certifiedText.
     *
     * @return value of certifiedText
     */
    public boolean getCertifiedText() {
        return certifiedText;
    }

    /**
     * Set certifiedText.
     *
     * @param certifiedText as boolean
     */
    @Override
    public void setCertifiedText(boolean certifiedText) {
        this.certifiedText = certifiedText;
    }
}
