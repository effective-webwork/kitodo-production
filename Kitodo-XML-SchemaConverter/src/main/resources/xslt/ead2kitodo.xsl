<?xml version="1.0" encoding="UTF-8" ?>
<!--
 *
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 *
-->

<xsl:stylesheet version="2.0"
                xmlns:ead="urn:isbn:1-931666-22-9"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:mets="http://www.loc.gov/METS/"
                xmlns:kitodo="http://meta.kitodo.org/v1/">

    <xsl:output method="xml" indent="yes" encoding="utf-8"/>
    <xsl:strip-space elements="*"/>

    <xsl:template match="ead:ead">
        <mets:mdWrap>
            <mets:xmlData>
                <kitodo:kitodo>
                    <!-- ### DocType ### -->
                    <kitodo:metadata name="docType">
                        <!-- Use separate xslt files for different document types -->
                        <xsl:text>verzeichnungseinheit</xsl:text>
                    </kitodo:metadata>
                    <xsl:apply-templates select="@*|node()"/>
                </kitodo:kitodo>
            </mets:xmlData>
        </mets:mdWrap>
    </xsl:template>

    <!-- ### Bestands-ID ### -->
    <xsl:template match="//ead:c[@level='collection' and @id]">
        <kitodo:metadata name="collection_id">
            <xsl:value-of select="current()/@id"/>
        </kitodo:metadata>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>

    <!-- ### ID ### -->
    <xsl:template match="//ead:c[@level='file' and @id]">
        <kitodo:metadata name="id">
            <xsl:value-of select="current()/@id"/>
        </kitodo:metadata>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>

    <!-- ### Einheitstitel ### -->
    <xsl:template match="//ead:c[@level='file' and @id]/ead:did/ead:unittitle[@type='Einheitstitel']">
        <kitodo:metadata name="einheitstitel">
            <xsl:value-of select="normalize-space()"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- ### Signatur ### -->
    <xsl:template match="//ead:c[@level='file' and @id]/ead:did/ead:unitid[@type!='Altsignatur' and @type!='Inventarnummer']">
        <kitodo:metadata name="signatur">
            <xsl:value-of select="normalize-space()"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- ### Alte Signatur ### -->
    <xsl:template match="//ead:c[@level='file' and @id]/ead:did/ead:unitid[@type='Altsignatur']">
        <kitodo:metadata name="alte_signatur">
            <xsl:value-of select="normalize-space()"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- ### Inventarnummer ### -->
    <xsl:template match="//ead:c[@level='file' and @id]/ead:did/ead:unitid[@type='Inventarnummer']">
        <kitodo:metadata name="inventarnummer">
            <xsl:value-of select="normalize-space()"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- ### Enthält ### -->
    <xsl:template match="//ead:c[@level='file' and @id]/ead:did/ead:abstract[@type='Enth&#xE4;lt']">
        <kitodo:metadata name="enthaelt">
            <xsl:value-of select="normalize-space()"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- ### Inhaltliche Beschreibung ### -->
    <xsl:template match="//ead:c[@level='file' and @id]/ead:did/ead:abstract[@type='Inhaltliche Beschreibung']">
        <kitodo:metadata name="inhaltliche_beschreibung">
            <xsl:value-of select="normalize-space()"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- ### Provenienz ### -->
    <xsl:template match="//ead:c[@level='file' and @id]/ead:did/ead:origination">
        <kitodo:metadata name="provenienz">
            <xsl:value-of select="normalize-space()"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- ### Umfang ### -->
    <xsl:template match="//ead:c[@level='file']/ead:did/ead:physdesc/ead:extent">
        <kitodo:metadata name="umfang">
            <xsl:value-of select="normalize-space()"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- ### Objekttyp ### -->
    <xsl:template match="//ead:c[@level='file']/ead:did/ead:physdesc/ead:genreform[@normal]">
        <kitodo:metadata name="lk_objekttyp_id">
            <xsl:value-of select="normalize-space()"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- ### Sprache ### -->
    <xsl:template match="//ead:c[@level='file']/ead:did/ead:langmaterial/ead:language">
        <kitodo:metadata name="sprache">
            <xsl:value-of select="normalize-space()"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- ### Material/Technik ### -->
    <xsl:template match="//ead:c[@level='file']/ead:did/ead:materialspec">
        <kitodo:metadata name="material_technik">
            <xsl:value-of select="normalize-space()"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- ### Freigabe-Status (VE) ### -->
    <xsl:template match="//ead:c[@level='file']/ead:accessrestrict">
        <xsl:if test="./ead:head/text()='Zugangsbeschränkung' and ./ead:p">
            <kitodo:metadata name="lk_freigabe_id">
                <xsl:value-of select="normalize-space(./p/text())"/>
            </kitodo:metadata>
        </xsl:if>
    </xsl:template>

    <!-- ### Rechte (VE) & Lizenz ### -->
    <xsl:template match="//ead:c[@level='file']/ead:userestrict">
        <xsl:choose>
            <xsl:when test="./ead:head/text()='Lizenzen' and ./p">
                <!-- ### Lizenz ### -->
                <kitodo:metadata name="lk_lizenzen_id">
                    <xsl:value-of select="normalize-space(./ead:p/text())"/>
                </kitodo:metadata>
            </xsl:when>
            <xsl:otherwise>
                <!-- ### Rechte (VE) ### -->
                <kitodo:metadata name="lk_rechte_id">
                    <xsl:value-of select="normalize-space(./ead:p/text())"/>
                </kitodo:metadata>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- ### Maße/Format ### -->
    <xsl:template match="//ead:c[@level='file']/ead:dimensions">
        <kitodo:metadata name="masse_format">
            <xsl:value-of select="normalize-space()"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- ### General rule for "odd" tags, including exceptions ### -->
    <xsl:template match="//ead:c[@level='file']/ead:odd">
        <xsl:if test="./ead:head and ./ead:p">
            <xsl:variable name="metadataName" select="replace(replace(replace(replace(replace(replace(./ead:head/text(), 'Technische Daten / ', ''), 'ä', 'ae'), 'ö', 'oe'), 'ü', 'ue'), ' ', '_'), 'ß', 'ss')"/>
            <xsl:choose>
                <xsl:when test="$metadataName='Projekt/Inszenierung'">
                    <kitodo:metadata name="projekt">
                        <xsl:value-of select="normalize-space(./ead:p/text())"/>
                    </kitodo:metadata>
                </xsl:when>
                <xsl:otherwise>
                    <kitodo:metadata name="{concat(lower-case(substring($metadataName, 1, 1)), substring($metadataName, 2))}">
                        <xsl:value-of select="normalize-space(./ead:p/text())"/>
                    </kitodo:metadata>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:if>
    </xsl:template>


    <!-- ### Ort ### -->
    <xsl:template match="//ead:c[@level='file' and @id]/ead:index/ead:indexentry/ead:geogname">
        <kitodo:metadata name="ort">
            <xsl:value-of select="normalize-space()"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- ### Person (Rolle) ### -->
    <xsl:template match="//ead:c[@level='file' and @id]/ead:index/ead:indexentry/ead:persname[@role]">
        <kitodo:metadata name="lk_typ_id">
            <xsl:value-of select="normalize-space(@role)"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- ### GND (ID) ### -->
    <xsl:template match="//ead:c[@level='file' and @id]/ead:index/ead:indexentry/ead:persname[@source='GND' and @authfilenumber]">
        <kitodo:metadata name="lk_gnd_id">
            <xsl:value-of select="normalize-space(@authfilenumber)"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- ### GND (Name) ### -->
    <xsl:template match="//ead:c[@level='file' and @id]/ead:index/ead:indexentry/ead:persname[@source='GND']">
        <kitodo:metadata name="lk_gnd_name">
            <xsl:value-of select="normalize-space()"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- ### Datierung-verbal, Datierung von, Datierung bis ### -->
    <xsl:template match="//ead:c[@level='file']/ead:did/ead:unitdate[@normal]">
        <kitodo:metadata name="datierung">
            <xsl:value-of select="normalize-space()"/>
        </kitodo:metadata>
        <kitodo:metadata name="datierung_date_von">
            <xsl:value-of select="substring-before(normalize-space(@normal), '/')"/>
        </kitodo:metadata>
        <kitodo:metadata name="datierung_date_bis">
            <xsl:value-of select="substring-after(normalize-space(@normal), '')"/>
        </kitodo:metadata>
    </xsl:template>

    <!-- pass-through rule -->
    <xsl:template match="@*|node()">
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>
</xsl:stylesheet>
