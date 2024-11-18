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

package org.kitodo.api.docket;

public class StandardPartialJobDocketData {

    /** The id of the partial job. */
    private String partialJobId;

    /** The viaduc id of the process. */
    private String viaducId;

    /** The Dossier Signatur. */
    private String dossierSignatur;

    /** The number of the partial job (OrderLabel). */
    private String partialJobNr;

    /** The profil of digitization. */
    private String profil;

    /** in schutzfrist. */
    private String schutzfrist;

    /** Manual processing. */
    private String manualImageProcessing;

    private String numberOfPages;

    private String startPage;

    private String resolution;

    private String border;

    private String originalFormat;

    private String deliveryFormat;

    private String returnDate;

    private String formatAdditionalInfo;

    private String comments;

    private String creationDate;

    /** The title of the process. */
    private String processTitle;

    private String dateOfRequestDossierFromViaduc;
    private String imagesDir;

    public StandardPartialJobDocketData() {
    }

    /**
     * Get partialJobId.
     *
     * @return value of partialJobId
     */
    public String getPartialJobId() {
        return partialJobId;
    }

    /**
     * Set partialJobId.
     *
     * @param partialJobId as java.lang.String
     */
    public void setPartialJobId(String partialJobId) {
        this.partialJobId = partialJobId;
    }

    /**
     * Get viaducId.
     *
     * @return value of viaducId
     */
    public String getViaducId() {
        return viaducId;
    }

    /**
     * Set viaducId.
     *
     * @param viaducId as java.lang.String
     */
    public void setViaducId(String viaducId) {
        this.viaducId = viaducId;
    }

    /**
     * Get dossierSignatur.
     *
     * @return value of dossierSignatur
     */
    public String getDossierSignatur() {
        return dossierSignatur;
    }

    /**
     * Set dossierSignatur.
     *
     * @param dossierSignatur as java.lang.String
     */
    public void setDossierSignatur(String dossierSignatur) {
        this.dossierSignatur = dossierSignatur;
    }

    /**
     * Get partialJobNr.
     *
     * @return value of partialJobNr (OrderLabel)
     */
    public String getPartialJobNr() {
        return partialJobNr;
    }

    /**
     * Set partialJobNr. (OrderLabel).
     *
     * @param partialJobNr as java.lang.String
     */
    public void setPartialJobNr(String partialJobNr) {
        this.partialJobNr = partialJobNr;
    }

    /**
     * Get profil.
     *
     * @return value of profil
     */
    public String getProfil() {
        return profil;
    }

    /**
     * Set profil.
     *
     * @param profil as java.lang.String
     */
    public void setProfil(String profil) {
        this.profil = profil;
    }

    /**
     * Get schutzfrist.
     *
     * @return value of schutzfrist
     */
    public String getSchutzfrist() {
        return schutzfrist;
    }

    /**
     * Set schutzfrist.
     *
     * @param schutzfrist as java.lang.String
     */
    public void setSchutzfrist(String schutzfrist) {
        if (schutzfrist.equals("true")) {
            this.schutzfrist = "In Schutzfrist";
        } else {
            this.schutzfrist = "-";
        }
    }

    /**
     * Get manualImageProcessing.
     *
     * @return value of manualImageProcessing
     */
    public String getManualImageProcessing() {
        return manualImageProcessing;
    }

    /**
     * Set manualImageProcessing.
     *
     * @param manualImageProcessing as java.lang.String
     */
    public void setManualImageProcessing(String manualImageProcessing) {
        this.manualImageProcessing = manualImageProcessing;
    }

    /**
     * Get numberOfPages.
     *
     * @return value of numberOfPages
     */
    public String getNumberOfPages() {
        return numberOfPages;
    }

    /**
     * Set numberOfPages.
     *
     * @param numberOfPages as java.lang.String
     */
    public void setNumberOfPages(String numberOfPages) {
        this.numberOfPages = numberOfPages;
    }

    /**
     * Get startPage.
     *
     * @return value of startPage
     */
    public String getStartPage() {
        return startPage;
    }

    /**
     * Set startPage.
     *
     * @param startPage as java.lang.String
     */
    public void setStartPage(String startPage) {
        this.startPage = startPage;
    }

    /**
     * Get resolution.
     *
     * @return value of resolution
     */
    public String getResolution() {
        return resolution;
    }

    /**
     * Set resolution.
     *
     * @param resolution as java.lang.String
     */
    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    /**
     * Get border.
     *
     * @return value of border
     */
    public String getBorder() {
        return border;
    }

    /**
     * Set border.
     *
     * @param border as java.lang.String
     */
    public void setBorder(String border) {
        this.border = border;
    }

    /**
     * Get originalFormat.
     *
     * @return value of originalFormat
     */
    public String getOriginalFormat() {
        return originalFormat;
    }

    /**
     * Set originalFormat.
     *
     * @param originalFormat as java.lang.String
     */
    public void setOriginalFormat(String originalFormat) {
        this.originalFormat = originalFormat;
    }

    /**
     * Get deliveryFormat.
     *
     * @return value of deliveryFormat
     */
    public String getDeliveryFormat() {
        return deliveryFormat;
    }

    /**
     * Set deliveryFormat.
     *
     * @param deliveryFormat as java.lang.String
     */
    public void setDeliveryFormat(String deliveryFormat) {
        this.deliveryFormat = deliveryFormat;
    }

    /**
     * Get returnDate.
     *
     * @return value of returnDate
     */
    public String getReturnDate() {
        return returnDate;
    }

    /**
     * Set returnDate.
     *
     * @param returnDate as java.lang.String
     */
    public void setReturnDate(String returnDate) {
        this.returnDate = returnDate;
    }

    /**
     * Get processTitle.
     *
     * @return value of processTitle
     */
    public String getProcessTitle() {
        return processTitle;
    }

    /**
     * Set processTitle.
     *
     * @param processTitle as java.lang.String
     */
    public void setProcessTitle(String processTitle) {
        this.processTitle = processTitle;
    }

    /**
     * Get dateOfRequestDossierFromViaduc.
     *
     * @return value of dateOfRequestDossierFromViaduc
     */
    public String getDateOfRequestDossierFromViaduc() {
        return dateOfRequestDossierFromViaduc;
    }

    /**
     * Set dateOfRequestDossierFromViaduc.
     *
     * @param dateOfRequestDossierFromViaduc as java.lang.String
     */
    public void setDateOfRequestDossierFromViaduc(String dateOfRequestDossierFromViaduc) {
        this.dateOfRequestDossierFromViaduc = dateOfRequestDossierFromViaduc;
    }

    /**
     * Get imagesDir.
     *
     * @return value of imagesDir
     */
    public String getImagesDir() {
        return imagesDir;
    }

    /**
     * Set imagesDir.
     *
     * @param imagesDir as java.lang.String
     */
    public void setImagesDir(String imagesDir) {
        this.imagesDir = imagesDir;
    }

    /**
     * Get comments.
     *
     * @return value of comments
     */
    public String getComments() {
        return comments;
    }

    /**
     * Set comments.
     *
     * @param comments as java.lang.String
     */
    public void setComments(String comments) {
        this.comments = comments;
    }

    /**
     * Get creationDate.
     *
     * @return value of creationDate
     */
    public String getCreationDate() {
        return creationDate;
    }

    /**
     * Set creationDate.
     *
     * @param creationDate as java.lang.String
     */
    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * Get formatAdditionalInfo.
     *
     * @return value of formatAdditionalInfo
     */
    public String getFormatAdditionalInfo() {
        return formatAdditionalInfo;
    }

    /**
     * Set formatAdditionalInfo.
     *
     * @param formatAdditionalInfo as java.lang.String
     */
    public void setFormatAdditionalInfo(String formatAdditionalInfo) {
        this.formatAdditionalInfo = formatAdditionalInfo;
    }
}
