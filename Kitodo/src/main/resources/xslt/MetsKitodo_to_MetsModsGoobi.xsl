<?xml version="1.0" encoding="utf-8"?>
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
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:mets="http://www.loc.gov/METS/"
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:goobi="http://meta.goobi.org/v1.5.1/"
                xmlns:kitodo="http://meta.kitodo.org/v1/"
                xmlns:ext="http://exslt.org/common">
    <xsl:output method="xml" indent="yes" encoding="UTF-8"/>
    <xsl:strip-space elements="*"/>

    <!--This is for storing the result of first transformation-->

    <xsl:template match="/">
        <xsl:variable name="pass1Result">
            <xsl:apply-templates/>
        </xsl:variable>
        <xsl:apply-templates mode="pass2" select="ext:node-set($pass1Result)/*"/>
    </xsl:template>

    <!--Transformation pass 1-->

    <!-- This is an identity template - it copies everything that doesn't match another template -->
    <xsl:template match="@* | node()">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
    </xsl:template>

<!--    <xsl:template match="*" priority="1">
        <xsl:element name="{local-name()}">
            <xsl:namespace name="mets" select="'http://www.loc.gov/METS/'"/>
            <xsl:namespace name="mods" select="'http://www.loc.gov/mods/v3'"/>
            <xsl:namespace name="kitodo" select="'http://meta.kitodo.org/v1/'"/>
            <xsl:namespace name="goobi" select="'http://meta.goobi.org/v1.5.1/'"/>
            <xsl:namespace name="ext" select="'http://exslt.org/common'"/>
            <xsl:namespace name="xlink" select="'http://www.w3.org/1999/xlink'"/>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:element>
    </xsl:template>-->

    <!-- This replaces element kitodo with mods/extension/goobi -->
    <xsl:template match="kitodo:kitodo">
        <mods:mods>
            <mods:extension>
                <goobi:goobi xmlns:goobi="http://meta.goobi.org/v1.5.1/">
                    <xsl:copy-of select="node()"/>
                </goobi:goobi>
            </mods:extension>
        </mods:mods>
    </xsl:template>

    <!-- This replaces the mdWrap attribute mdtype -->
    <xsl:template match="mets:mdWrap">
        <mets:mdWrap MDTYPE="MODS">
            <xsl:apply-templates select="node()"/>
        </mets:mdWrap>
    </xsl:template>

    <!--Transformation pass 2-->

    <!-- This is an identity template - it copies everything that doesn't match another template -->
    <xsl:template match="@* | node()" mode="pass2">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()" mode="pass2"/>
        </xsl:copy>
    </xsl:template>

    <!-- This replaces namespace url of metadata elements -->
    <xsl:template match="kitodo:metadata" mode="pass2">
        <goobi:metadata>
            <xsl:apply-templates select="@* | node()" mode="pass2"/>
        </goobi:metadata>
    </xsl:template>
    <!--This replaces the metadata group element with the person metadata element -->
    <xsl:template match="kitodo:metadataGroup" mode="pass2">
        <goobi:metadata type="group">
            <xsl:attribute name="name">
                <xsl:value-of select="@name"/>
            </xsl:attribute>
            <xsl:for-each select="node()">
                <goobi:metadata>
                    <xsl:attribute name="name">
                        <xsl:value-of select="@name"/>
                    </xsl:attribute>
                    <xsl:value-of select="current()"/>
                </goobi:metadata>
            </xsl:for-each>
        </goobi:metadata>
    </xsl:template>
</xsl:stylesheet>
