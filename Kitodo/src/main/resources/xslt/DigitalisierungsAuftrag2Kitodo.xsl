<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:kitodo="http://meta.kitodo.org/v1/"
                xmlns:mets="http://www.loc.gov/METS/"
                xmlns:i="http://www.w3.org/2001/XMLSchema-instance">

    <xsl:output method="xml" indent="yes"/>

    <xsl:template match="/DigitalisierungsAuftrag">
        <mets:mets xmlns:mets="http://www.loc.gov/METS/">
            <mets:dmdSec ID="DMDLOG_ROOT">
                <mets:mdWrap MDTYPE="OTHER" OTHERMDTYPE="KITODO">
                    <mets:xmlData>
                        <kitodo:kitodo xmlns:kitodo="http://meta.kitodo.org/v1/" version="1.0">
                            <xsl:apply-templates select="/DigitalisierungsAuftrag/Ablieferung"/>
                        </kitodo:kitodo>
                    </mets:xmlData>
                </mets:mdWrap>
            </mets:dmdSec>
        </mets:mets>
    </xsl:template>

    <xsl:template match="Ablieferung">
        <kitodo:metadataGroup name="deliverable">
            <xsl:apply-templates select="/DigitalisierungsAuftrag/Auftragsdaten/Benutzungskopie"/>
            <xsl:apply-templates select="*"/>
            <!--<xsl:apply-templates select="/DigitalisierungsAuftrag/OrdnungsSystem/*"/>-->
            <kitodo:metadataGroup name="inventory">
                <xsl:apply-templates select="/DigitalisierungsAuftrag/OrdnungsSystem"/>
            </kitodo:metadataGroup>
        </kitodo:metadataGroup>
    </xsl:template>

    <xsl:template match="UntergeordnetesOrdnungsSystem[not(@i:nil='true')]">
        <kitodo:metadataGroup name="inventory_item">
            <xsl:apply-templates select="*[not(self::UntergeordnetesOrdnungsSystem[@i:nil='true'])]"/>
            <xsl:if test="./UntergeordnetesOrdnungsSystem[@i:nil='true'] or .[not(UntergeordnetesOrdnungsSystem)]">
                <kitodo:metadataGroup name="dossier">
                    <xsl:apply-templates select="/DigitalisierungsAuftrag/Dossier/*[not(self::Behaeltnisse)]"/>
                    <xsl:apply-templates select="/DigitalisierungsAuftrag/Dossier/Behaeltnisse/Behaeltnis/BehaeltnisCode"/>
                    <xsl:apply-templates select="/DigitalisierungsAuftrag/Dossier/Behaeltnisse/Behaeltnis/BehaeltnisTyp"/>
                    <xsl:apply-templates select="/DigitalisierungsAuftrag/Dossier/Behaeltnisse/Behaeltnis/InformationsTraeger"/>
                    <xsl:apply-templates select="/DigitalisierungsAuftrag/Dossier/Behaeltnisse/Behaeltnis/Standort"/>
                    <xsl:apply-templates select="/DigitalisierungsAuftrag/Auftragsdaten/BemerkungenBar"/>
                    <xsl:apply-templates select="/DigitalisierungsAuftrag/Auftragsdaten/BemerkungenKunde"/>
                </kitodo:metadataGroup>
            </xsl:if>
        </kitodo:metadataGroup>
    </xsl:template>

    <xsl:template match="OrdnungsSystem[UntergeordnetesOrdnungsSystem[not(@i:nil='true')]]">
        <xsl:apply-templates select="*"/>
    </xsl:template>

    <xsl:template match="OrdnungsSystem[not(UntergeordnetesOrdnungsSystem)] | OrdnungsSystem[UntergeordnetesOrdnungsSystem[@i:nil='true']]">
        <xsl:apply-templates select="*"/>
        <kitodo:metadataGroup name="inventory_item">
            <kitodo:metadata name="sgntr_cd">keine Angabe</kitodo:metadata>
            <kitodo:metadata name="vrzng_enht_titel">keine Angabe</kitodo:metadata>
            <kitodo:metadataGroup name="dossier">
                <xsl:apply-templates select="/DigitalisierungsAuftrag/Dossier/*[not(self::Behaeltnisse)]"/>
                <xsl:apply-templates select="/DigitalisierungsAuftrag/Dossier/Behaeltnisse/Behaeltnis/BehaeltnisCode"/>
                <xsl:apply-templates select="/DigitalisierungsAuftrag/Dossier/Behaeltnisse/Behaeltnis/BehaeltnisTyp"/>
                <xsl:apply-templates select="/DigitalisierungsAuftrag/Dossier/Behaeltnisse/Behaeltnis/InformationsTraeger"/>
                <xsl:apply-templates select="/DigitalisierungsAuftrag/Dossier/Behaeltnisse/Behaeltnis/Standort"/>
                <xsl:apply-templates select="/DigitalisierungsAuftrag/Auftragsdaten/BemerkungenBar"/>
                <xsl:apply-templates select="/DigitalisierungsAuftrag/Auftragsdaten/BemerkungenKunde"/>
            </kitodo:metadataGroup>
        </kitodo:metadataGroup>
    </xsl:template>

    <xsl:template match="Dossier/UntergeordneteVerzEinheiten/VerzEinheit[Stufe = 'Subdossier']">
        <kitodo:metadataGroup name="dossier">
            <xsl:apply-templates select="UntergeordneteVerzEinheiten"/>
            <xsl:apply-templates select="Aktenzeichen"/>
            <xsl:apply-templates select="Archivnummer"/>
            <xsl:apply-templates select="Darin"/>
            <xsl:apply-templates select="Entstehungszeitraum"/>
            <xsl:apply-templates select="Form"/>
            <xsl:apply-templates select="FrueheresAktenzeichen"/>
            <xsl:apply-templates select="Signatur"/>
            <xsl:apply-templates select="Titel"/>
            <xsl:apply-templates select="VerzEinheitId"/>
            <xsl:apply-templates select="Zusatzkomponente"/>
            <xsl:apply-templates select="Behaeltnisse/Behaeltnis/BehaeltnisCode"/>
            <xsl:apply-templates select="Behaeltnisse/Behaeltnis/BehaeltnisTyp"/>
            <xsl:apply-templates select="Behaeltnisse/Behaeltnis/InformationsTraeger"/>
            <xsl:apply-templates select="Behaeltnisse/Behaeltnis/Standort"/>
        </kitodo:metadataGroup>
    </xsl:template>

    <xsl:template match="UntergeordneteVerzEinheiten">
        <xsl:apply-templates select="VerzEinheit">
            <xsl:sort select="replace(tokenize(Signatur, '#')[last()], '[\D]', '')" data-type="number"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template match="VerzEinheit[Stufe = 'Dokument']">
        <kitodo:metadataGroup name="document">
            <xsl:apply-templates select="Aktenzeichen"/>
            <xsl:apply-templates select="Archivnummer"/>
            <xsl:apply-templates select="Darin"/>
            <xsl:apply-templates select="Entstehungszeitraum"/>
            <xsl:apply-templates select="Form"/>
            <xsl:apply-templates select="FrueheresAktenzeichen"/>
            <xsl:apply-templates select="Signatur"/>
            <xsl:apply-templates select="Titel"/>
            <xsl:apply-templates select="VerzEinheitId"/>
            <xsl:apply-templates select="Zusatzkomponente"/>
            <xsl:apply-templates select="Behaeltnisse/Behaeltnis/BehaeltnisCode"/>
            <xsl:apply-templates select="Behaeltnisse/Behaeltnis/BehaeltnisTyp"/>
            <xsl:apply-templates select="Behaeltnisse/Behaeltnis/InformationsTraeger"/>
            <xsl:apply-templates select="Behaeltnisse/Behaeltnis/Standort"/>
        </kitodo:metadataGroup>
    </xsl:template>

    <!-- ******** Metadata ********* -->

    <!-- deliverable -->

    <!-- Ablieferungsnummer -->
    <xsl:template name="Ablieferungsnummer" match="Ablieferungsnummer">
        <kitodo:metadata name="ablfr_nr">
            <xsl:value-of select="."/>
        </kitodo:metadata>
    </xsl:template>

    <!-- Abliefernde Stelle -->
    <xsl:template match="AblieferndeStelle">
        <kitodo:metadata name="ablfr_prtnr_id">
            <xsl:value-of select="."/>
        </kitodo:metadata>
    </xsl:template>

    <!-- Aktenbildner Name -->
    <xsl:template match="AktenbildnerName">
        <kitodo:metadata name="akte_blbnr_prtnr_id">
            <xsl:value-of select="."/>
        </kitodo:metadata>
    </xsl:template>


    <!-- Bestand/Serie (inventory) -->

    <xsl:template match="Name">
        <kitodo:metadata name="vrzng_enht_titel">
            <xsl:value-of select="."/>
        </kitodo:metadata>
    </xsl:template>

    <!-- Behältnis (container) -->

    <xsl:template match="BehaeltnisCode">
        <kitodo:metadata name="bhltn_cd">
            <xsl:value-of select="."/>
        </kitodo:metadata>
    </xsl:template>


    <!-- Dokument (document) -->

    <xsl:template match="Signatur">
        <kitodo:metadata name="sgntr_cd">
            <xsl:value-of select="."/>
        </kitodo:metadata>
    </xsl:template>

    <!-- Titel -->
    <xsl:template match="Titel">
        <kitodo:metadata name="vrzng_enht_titel">
            <xsl:value-of select="."/>
        </kitodo:metadata>
    </xsl:template>

    <!-- Schutzfrist -->
    <xsl:template match="InSchutzfrist">
        <kitodo:metadata name="in_schutzfrist">
            <xsl:value-of select="."/>
        </kitodo:metadata>
    </xsl:template>

    <xsl:template match="Entstehungszeitraum">
        <kitodo:metadata name="zt_raum_txt">
            <xsl:value-of select="."/>
        </kitodo:metadata>
    </xsl:template>

    <xsl:template match="Aktenzeichen[not(@i:nil='true')]">
        <kitodo:metadata name="aktnzchn">
            <xsl:value-of select="."/>
        </kitodo:metadata>
    </xsl:template>

    <xsl:template match="FrueheresAktenzeichen[not(@i:nil='true')]">
        <kitodo:metadata name="frhrs_aktnzchn">
            <xsl:value-of select="."/>
        </kitodo:metadata>
    </xsl:template>

    <xsl:template match="Zusatzkomponente[not(@i:nil='true')]">
        <kitodo:metadata name="zstz_mrkml">
            <xsl:value-of select="."/>
        </kitodo:metadata>
    </xsl:template>

    <xsl:template match="Darin">
        <kitodo:metadata name="darin_txt">
            <xsl:value-of select="."/>
        </kitodo:metadata>
    </xsl:template>

    <xsl:template match="Form">
        <kitodo:metadata name="form">
            <xsl:value-of select="."/>
        </kitodo:metadata>
    </xsl:template>

    <xsl:template match="Archivnummer[not(@i:nil='true')]">
        <kitodo:metadata name="archv_nr">
            <xsl:value-of select="."/>
        </kitodo:metadata>
    </xsl:template>

    <!-- ??????????????????????? -->
    <xsl:template match="VerzEinheitId">
        <kitodo:metadata name="vrzng_enht_id">
            <xsl:value-of select="."/>
        </kitodo:metadata>
    </xsl:template>

    <xsl:template match="BehaeltnisTyp">
        <kitodo:metadata name="gsft_obj_typ_nm">
            <xsl:value-of select="."/>
        </kitodo:metadata>
    </xsl:template>

    <xsl:template match="InformationsTraeger">
        <kitodo:metadata name="bhltn_info_trgr_nm">
            <xsl:value-of select="."/>
        </kitodo:metadata>
    </xsl:template>

    <xsl:template match="Standort">
        <kitodo:metadata name="stndrt">
            <xsl:value-of select="."/>
        </kitodo:metadata>
    </xsl:template>

    <xsl:template match="BemerkungenKunde">
        <kitodo:metadata name="bmrkng_knde">
            <xsl:value-of select="."/>
        </kitodo:metadata>
    </xsl:template>

    <xsl:template match="BemerkungenBar">
        <kitodo:metadata name="bmrkng_bar">
            <xsl:value-of select="."/>
        </kitodo:metadata>
    </xsl:template>

    <xsl:template match="AuftragsId">
        <kitodo:metadata name="auftrg_id">
            <xsl:value-of select="."/>
        </kitodo:metadata>
    </xsl:template>

    <xsl:template match="Bestelldatum">
        <kitodo:metadata name="bstll_dtm">
            <xsl:value-of select="."/>
        </kitodo:metadata>
    </xsl:template>

    <xsl:template match="BestelleinheitId">
        <kitodo:metadata name="bstll_id">
            <xsl:value-of select="."/>
        </kitodo:metadata>
    </xsl:template>

    <xsl:template match="Benutzungskopie">
        <kitodo:metadata name="benutzungskopie">
            <xsl:value-of select="."/>
        </kitodo:metadata>
    </xsl:template>

    <!-- ?????????????????????????? -->
    <xsl:template match="Digitalisierungsprofil">
        <kitodo:metadata name="profil">
            <xsl:value-of select="."/>
        </kitodo:metadata>
    </xsl:template>

    <xsl:template match="text()"/>

</xsl:stylesheet>
