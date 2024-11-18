-- liquibase formatted sql

--changeset kitodo:1
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `benutzer` (
  `BenutzerID` int(11) NOT NULL AUTO_INCREMENT,
  `Vorname` varchar(255) DEFAULT NULL,
  `Nachname` varchar(255) DEFAULT NULL,
  `login` varchar(255) DEFAULT NULL,
  `ldaplogin` varchar(255) DEFAULT NULL,
  `passwort` varchar(255) DEFAULT NULL,
  `IstAktiv` tinyint(1) DEFAULT NULL,
  `isVisible` varchar(255) DEFAULT NULL,
  `Standort` varchar(255) DEFAULT NULL,
  `metadatensprache` varchar(255) DEFAULT NULL,
  `css` varchar(255) DEFAULT NULL,
  `mitMassendownload` tinyint(1) DEFAULT NULL,
  `confVorgangsdatumAnzeigen` tinyint(1) DEFAULT NULL,
  `Tabellengroesse` int(11) DEFAULT NULL,
  `sessiontimeout` int(11) DEFAULT NULL,
  `ldapgruppenID` int(11) DEFAULT NULL,
  PRIMARY KEY (`BenutzerID`),
  KEY `FK_LdapgruppenID` (`ldapgruppenID`)
) DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `benutzereigenschaften` (
  `benutzereigenschaftenID` int(11) NOT NULL AUTO_INCREMENT,
  `Titel` varchar(255) DEFAULT NULL,
  `Wert` varchar(255) DEFAULT NULL,
  `IstObligatorisch` tinyint(1) DEFAULT NULL,
  `DatentypenID` int(11) DEFAULT NULL,
  `Auswahl` varchar(255) DEFAULT NULL,
  `creationDate` datetime DEFAULT NULL,
  `BenutzerID` int(11) DEFAULT NULL,
  PRIMARY KEY (`benutzereigenschaftenID`),
  KEY `FK_BenutzerID` (`BenutzerID`)
) DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `benutzergruppen` (
  `BenutzergruppenID` int(11) NOT NULL AUTO_INCREMENT,
  `titel` varchar(255) DEFAULT NULL,
  `berechtigung` int(11) DEFAULT NULL,
  PRIMARY KEY (`BenutzergruppenID`)
) DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `benutzergruppenmitgliedschaft` (
  `BenutzerGruppenID` int(11) NOT NULL,
  `BenutzerID` int(11) NOT NULL,
  PRIMARY KEY (`BenutzerID`,`BenutzerGruppenID`),
  KEY `FK_BenutzerGruppenID` (`BenutzerGruppenID`),
  KEY `FK_BenutzerID` (`BenutzerID`)
) DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dockets` (
  `docketID` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `file` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`docketID`)
) DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `history` (
  `historyid` int(11) NOT NULL AUTO_INCREMENT,
  `numericvalue` double DEFAULT NULL,
  `stringvalue` varchar(255) DEFAULT NULL,
  `type` int(11) DEFAULT NULL,
  `date` datetime DEFAULT NULL,
  `processID` int(11) DEFAULT NULL,
  PRIMARY KEY (`historyid`),
  KEY `FK_ProzesseID` (`processID`)
) DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ldapgruppen` (
  `ldapgruppenID` int(11) NOT NULL AUTO_INCREMENT,
  `titel` varchar(255) DEFAULT NULL,
  `homeDirectory` varchar(255) DEFAULT NULL,
  `gidNumber` varchar(255) DEFAULT NULL,
  `userDN` varchar(255) DEFAULT NULL,
  `objectClasses` varchar(255) DEFAULT NULL,
  `sambaSID` varchar(255) DEFAULT NULL,
  `sn` varchar(255) DEFAULT NULL,
  `uid` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `displayName` varchar(255) DEFAULT NULL,
  `gecos` varchar(255) DEFAULT NULL,
  `loginShell` varchar(255) DEFAULT NULL,
  `sambaAcctFlags` varchar(255) DEFAULT NULL,
  `sambaLogonScript` varchar(255) DEFAULT NULL,
  `sambaPrimaryGroupSID` varchar(255) DEFAULT NULL,
  `sambaPwdMustChange` varchar(255) DEFAULT NULL,
  `sambaPasswordHistory` varchar(255) DEFAULT NULL,
  `sambaLogonHours` varchar(255) DEFAULT NULL,
  `sambaKickoffTime` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ldapgruppenID`)
) DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `metadatenkonfigurationen` (
  `MetadatenKonfigurationID` int(11) NOT NULL AUTO_INCREMENT,
  `Titel` varchar(255) DEFAULT NULL,
  `Datei` varchar(255) DEFAULT NULL,
  `orderMetadataByRuleset` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`MetadatenKonfigurationID`)
) DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `projectfilegroups` (
  `ProjectFileGroupID` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `path` varchar(255) DEFAULT NULL,
  `mimetype` varchar(255) DEFAULT NULL,
  `suffix` varchar(255) DEFAULT NULL,
  `folder` varchar(255) DEFAULT NULL,
 `previewImage` tinyint(1) DEFAULT NULL,
  `ProjekteID` int(11) DEFAULT NULL,
  PRIMARY KEY (`ProjectFileGroupID`),
  KEY `FK_ProjekteID` (`ProjekteID`)
) DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `projektbenutzer` (
  `BenutzerID` int(11) NOT NULL,
  `ProjekteID` int(11) NOT NULL,
  PRIMARY KEY (`BenutzerID`,`ProjekteID`),
  KEY `FK_ProjekteID` (`ProjekteID`),
  KEY `FK_BenutzerID` (`BenutzerID`)
) DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `projekte` (
  `ProjekteID` int(11) NOT NULL AUTO_INCREMENT,
  `Titel` varchar(255) DEFAULT NULL,
  `useDmsImport` tinyint(1) DEFAULT NULL,
  `dmsImportTimeOut` int(11) DEFAULT NULL,
  `dmsImportRootPath` varchar(255) DEFAULT NULL,
  `dmsImportImagesPath` varchar(255) DEFAULT NULL,
  `dmsImportSuccessPath` varchar(255) DEFAULT NULL,
  `dmsImportErrorPath` varchar(255) DEFAULT NULL,
  `dmsImportCreateProcessFolder` tinyint(1) DEFAULT NULL,
  `fileFormatInternal` varchar(255) DEFAULT NULL,
  `fileFormatDmsExport` varchar(255) DEFAULT NULL,
  `metsRightsOwner` varchar(255) DEFAULT NULL,
  `metsRightsOwnerLogo` varchar(255) DEFAULT NULL,
  `metsRightsOwnerSite` varchar(255) DEFAULT NULL,
  `metsRightsOwnerMail` varchar(255) DEFAULT NULL,
  `metsDigiprovReference` varchar(255) DEFAULT NULL,
  `metsDigiprovPresentation` varchar(255) DEFAULT NULL,
  `metsDigiprovReferenceAnchor` varchar(255) DEFAULT NULL,
  `metsDigiprovPresentationAnchor` varchar(255) DEFAULT NULL,
  `metsPointerPath` varchar(255) DEFAULT NULL,
  `metsPointerPathAnchor` varchar(255) DEFAULT NULL,
  `metsPurl` varchar(255) DEFAULT NULL,
  `metsContentIDs` varchar(255) DEFAULT NULL,
  `startDate` datetime DEFAULT NULL,
  `endDate` datetime DEFAULT NULL,
  `numberOfPages` int(11) DEFAULT NULL,
  `numberOfVolumes` int(11) DEFAULT NULL,
  `projectIsArchived` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`ProjekteID`)
) DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `prozesse` (
  `ProzesseID` int(11) NOT NULL AUTO_INCREMENT,
  `Titel` varchar(255) DEFAULT NULL,
  `ausgabename` varchar(255) DEFAULT NULL,
  `IstTemplate` tinyint(1) DEFAULT NULL,
  `swappedOut` tinyint(1) DEFAULT NULL,
  `inAuswahllisteAnzeigen` tinyint(1) DEFAULT NULL,
  `sortHelperStatus` varchar(255) DEFAULT NULL,
  `sortHelperImages` int(11) DEFAULT NULL,
  `sortHelperArticles` int(11) DEFAULT NULL,
  `sortHelperDocstructs` int(11) DEFAULT NULL,
  `sortHelperMetadata` int(11) DEFAULT NULL,
  `erstellungsdatum` datetime DEFAULT NULL,
  `wikifield` longtext,
  `ProjekteID` int(11) DEFAULT NULL,
  `MetadatenKonfigurationID` int(11) DEFAULT NULL,
  `docketID` int(11) DEFAULT NULL,
  PRIMARY KEY (`ProzesseID`),
  KEY `FK_ProjekteID` (`ProjekteID`),
  KEY `FK_MetadatenKonfigurationID` (`MetadatenKonfigurationID`),
  KEY `FK_DocketID` (`docketID`)
) DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `prozesseeigenschaften` (
  `prozesseeigenschaftenID` int(11) NOT NULL AUTO_INCREMENT,
  `Titel` varchar(255) DEFAULT NULL,
  `Wert` longtext,
  `IstObligatorisch` tinyint(1) DEFAULT NULL,
  `DatentypenID` int(11) DEFAULT NULL,
  `Auswahl` varchar(255) DEFAULT NULL,
  `creationDate` datetime DEFAULT NULL,
  `container` int(11) DEFAULT NULL,
  `prozesseID` int(11) DEFAULT NULL,
  PRIMARY KEY (`prozesseeigenschaftenID`),
  KEY `FK_ProzesseID` (`prozesseID`)
) DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `schritte` (
  `SchritteID` int(11) NOT NULL AUTO_INCREMENT,
  `Titel` varchar(255) DEFAULT NULL,
  `Prioritaet` int(11) DEFAULT NULL,
  `Reihenfolge` int(11) DEFAULT NULL,
  `Bearbeitungsstatus` int(11) DEFAULT NULL,
  `edittype` int(11) DEFAULT NULL,
  `BearbeitungsZeitpunkt` datetime DEFAULT NULL,
  `BearbeitungsBeginn` datetime DEFAULT NULL,
  `BearbeitungsEnde` datetime DEFAULT NULL,
  `homeverzeichnisNutzen` smallint(6) DEFAULT NULL,
  `typMetadaten` tinyint(1) DEFAULT NULL,
  `typAutomatisch` tinyint(1) DEFAULT NULL,
  `typImportFileUpload` tinyint(1) DEFAULT NULL,
  `typExportRus` tinyint(1) DEFAULT NULL,
  `typImagesLesen` tinyint(1) DEFAULT NULL,
  `typImagesSchreiben` tinyint(1) DEFAULT NULL,
  `typExportDMS` tinyint(1) DEFAULT NULL,
  `typBeimAnnehmenModul` tinyint(1) DEFAULT NULL,
  `typBeimAnnehmenAbschliessen` tinyint(1) DEFAULT NULL,
  `typBeimAnnehmenModulUndAbschliessen` tinyint(1) DEFAULT NULL,
  `typScriptStep` tinyint(1) DEFAULT NULL,
  `scriptName1` varchar(255) DEFAULT NULL,
  `typAutomatischScriptpfad` varchar(255) DEFAULT NULL,
  `scriptName2` varchar(255) DEFAULT NULL,
  `typAutomatischScriptpfad2` varchar(255) DEFAULT NULL,
  `scriptName3` varchar(255) DEFAULT NULL,
  `typAutomatischScriptpfad3` varchar(255) DEFAULT NULL,
  `scriptName4` varchar(255) DEFAULT NULL,
  `typAutomatischScriptpfad4` varchar(255) DEFAULT NULL,
  `scriptName5` varchar(255) DEFAULT NULL,
  `typAutomatischScriptpfad5` varchar(255) DEFAULT NULL,
  `typBeimAbschliessenVerifizieren` tinyint(1) DEFAULT NULL,
  `typModulName` varchar(255) DEFAULT NULL,
  `batchStep` tinyint(1) DEFAULT NULL,
  `stepPlugin` varchar(255) DEFAULT NULL,
  `validationPlugin` varchar(255) DEFAULT NULL,
  `BearbeitungsBenutzerID` int(11) DEFAULT NULL,
  `ProzesseID` int(11) DEFAULT NULL,
  PRIMARY KEY (`SchritteID`),
  KEY `FK_ProzesseID` (`ProzesseID`),
  KEY `FK_BearbeitungsBenutzerID` (`BearbeitungsBenutzerID`)
) DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `schritteberechtigtebenutzer` (
  `BenutzerID` int(11) NOT NULL,
  `schritteID` int(11) NOT NULL,
  PRIMARY KEY (`schritteID`,`BenutzerID`),
  KEY `FK_SchritteID` (`schritteID`),
  KEY `FK_BenutzerID` (`BenutzerID`)
) DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `schritteberechtigtegruppen` (
  `BenutzerGruppenID` int(11) NOT NULL,
  `schritteID` int(11) NOT NULL,
  PRIMARY KEY (`schritteID`,`BenutzerGruppenID`),
  KEY `FK_SchritteID` (`schritteID`),
  KEY `FK_BenutzerGruppenID` (`BenutzerGruppenID`)
) DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `vorlagen` (
  `VorlagenID` int(11) NOT NULL AUTO_INCREMENT,
  `Herkunft` varchar(255) DEFAULT NULL,
  `ProzesseID` int(11) DEFAULT NULL,
  PRIMARY KEY (`VorlagenID`),
  KEY `FK_ProzesseID` (`ProzesseID`)
) DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `vorlageneigenschaften` (
  `vorlageneigenschaftenID` int(11) NOT NULL AUTO_INCREMENT,
  `Titel` varchar(255) DEFAULT NULL,
  `Wert` longtext,
  `IstObligatorisch` tinyint(1) DEFAULT NULL,
  `DatentypenID` int(11) DEFAULT NULL,
  `Auswahl` varchar(255) DEFAULT NULL,
  `creationDate` datetime DEFAULT NULL,
  `container` int(11) DEFAULT NULL,
  `vorlagenID` int(11) DEFAULT NULL,
  PRIMARY KEY (`vorlageneigenschaftenID`),
  KEY `FK_VorlagenID` (`vorlagenID`)
) DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `werkstuecke` (
  `WerkstueckeID` int(11) NOT NULL AUTO_INCREMENT,
  `ProzesseID` int(11) DEFAULT NULL,
  PRIMARY KEY (`WerkstueckeID`),
  KEY `FK_ProzesseID` (`ProzesseID`)
) DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `werkstueckeeigenschaften` (
  `werkstueckeeigenschaftenID` int(11) NOT NULL AUTO_INCREMENT,
  `Titel` varchar(255) DEFAULT NULL,
  `Wert` longtext,
  `IstObligatorisch` tinyint(1) DEFAULT NULL,
  `DatentypenID` int(11) DEFAULT NULL,
  `Auswahl` varchar(255) DEFAULT NULL,
  `creationDate` datetime DEFAULT NULL,
  `container` int(11) DEFAULT NULL,
  `werkstueckeID` int(11) DEFAULT NULL,
  PRIMARY KEY (`werkstueckeeigenschaftenID`),
  KEY `FK_WerkstueckeID` (`werkstueckeID`)
) DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `batchesprozesse` (
  `ProzesseID` int(11) NOT NULL,
  `BatchID` int(11) NOT NULL,
  PRIMARY KEY (`ProzesseID`,`BatchID`),
  KEY `FK_ProzesseID` (`ProzesseID`),
  KEY `FK_BatchID` (`BatchID`)
) DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `batches` (
  `BatchID` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) DEFAULT NULL,
  `type` varchar(9) DEFAULT NULL,
  PRIMARY KEY (`BatchID`)
) DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--changeset kitodo:2
/*1. Rename tables*/
RENAME TABLE
  batches TO batch,
  batchesprozesse TO batch_x_process,
  benutzer TO user,
  benutzereigenschaften TO userProperty,
  benutzergruppen TO userGroup,
  benutzergruppenmitgliedschaft TO user_x_userGroup,
  dockets TO docket,
  ldapgruppen TO ldapGroup,
  metadatenkonfigurationen TO ruleset,
  projectfilegroups TO projectFileGroup,
  projektbenutzer TO project_x_user,
  projekte TO project,
  prozesse TO process,
  prozesseeigenschaften TO processProperty,
  schritte TO task,
  schritteberechtigtebenutzer TO task_x_user,
  schritteberechtigtegruppen TO task_x_userGroup,
  vorlagen TO template,
  vorlageneigenschaften TO templateProperty,
  werkstuecke TO workpiece,
  werkstueckeeigenschaften TO workpieceProperty;
/* 2. Check if table exists, if yes, remove it */
DROP TABLE IF EXISTS schritteeigenschaften;

--changeset kitodo:37
/*Migration: Renaming columns to English*/
/*1. Rename columns in tables*/
ALTER TABLE batch
  CHANGE BatchID id INT(11) NOT NULL AUTO_INCREMENT;

ALTER TABLE batch_x_process
  CHANGE BatchID batch_id INT(11) NOT NULL,
  CHANGE ProzesseID process_id INT(11) NOT NULL;

ALTER TABLE docket
  CHANGE docketID id INT(11) NOT NULL AUTO_INCREMENT;

ALTER TABLE history
  CHANGE historyid id INT(11) NOT NULL AUTO_INCREMENT,
  CHANGE stringvalue stringValue VARCHAR(255),
  CHANGE processID process_id INT(11);

ALTER TABLE ldapGroup
  CHANGE ldapgruppenID id INT(11) NOT NULL AUTO_INCREMENT,
  CHANGE titel title VARCHAR(255),
  CHANGE userDN userDn VARCHAR(255),
  CHANGE sambaSID sambaSid VARCHAR(255),
  CHANGE sambaPrimaryGroupSID sambaPrimaryGroupSid VARCHAR(255),
  CHANGE sambaPwdMustChange sambaPasswordMustChange VARCHAR(255);

ALTER TABLE process
  CHANGE ProzesseID id INT(11) NOT NULL AUTO_INCREMENT,
  CHANGE Titel title VARCHAR(255),
  CHANGE ausgabename outputName VARCHAR(255),
  CHANGE IstTemplate isTemplate TINYINT(1),
  CHANGE inAuswahllisteAnzeigen isChoiceListShown TINYINT(1),
  CHANGE erstellungsdatum creationDate DATETIME,
  CHANGE wikifield wikiField LONGTEXT,
  CHANGE ProjekteID project_id INT(11),
  CHANGE MetadatenKonfigurationID ruleset_id INT(11),
  CHANGE docketID docket_id INT(11);

ALTER TABLE processProperty
  CHANGE prozesseeigenschaftenID id INT(11) NOT NULL AUTO_INCREMENT,
  CHANGE Titel title VARCHAR(255),
  CHANGE Wert value LONGTEXT,
  CHANGE IstObligatorisch isObligatory TINYINT(1),
  CHANGE DatentypenID dataType INT(11),
  CHANGE Auswahl choice VARCHAR(255),
  CHANGE prozesseID process_id INT(11);

ALTER TABLE project
  CHANGE ProjekteID id INT(11) NOT NULL AUTO_INCREMENT,
  CHANGE Titel title VARCHAR(255),
  CHANGE metsContentIDs metsContentId VARCHAR(255);

ALTER TABLE project_x_user
  CHANGE BenutzerID user_id INT(11) NOT NULL,
  CHANGE ProjekteID project_id INT(11) NOT NULL;

ALTER TABLE projectFileGroup
  CHANGE ProjectFileGroupID id INT(11) NOT NULL AUTO_INCREMENT,
  CHANGE mimetype mimeType VARCHAR(255),
  CHANGE ProjekteID project_id INT(11);

ALTER TABLE ruleset
  CHANGE MetadatenKonfigurationID id INT(11) NOT NULL AUTO_INCREMENT,
  CHANGE Titel title VARCHAR(255),
  CHANGE Datei file VARCHAR(255);

ALTER TABLE task
  CHANGE SchritteID id INT(11) NOT NULL AUTO_INCREMENT,
  CHANGE Titel title VARCHAR(255),
  CHANGE Prioritaet priority INT(11),
  CHANGE Reihenfolge ordering INT(11),
  CHANGE Bearbeitungsstatus processingStatus INT(11),
  CHANGE edittype editType INT(11),
  CHANGE BearbeitungsZeitpunkt processingTime DATETIME,
  CHANGE BearbeitungsBeginn processingBegin DATETIME,
  CHANGE BearbeitungsEnde processingEnd DATETIME,
  CHANGE homeverzeichnisNutzen homeDirectory SMALLINT(6),
  CHANGE typMetadaten typeMetadata TINYINT(1),
  CHANGE typAutomatisch typeAutomatic TINYINT(1),
  CHANGE typImportFileUpload typeImportFileUpload TINYINT(1),
  CHANGE typExportRus typeExportRussian TINYINT(1),
  CHANGE typImagesLesen typeImagesRead TINYINT(1),
  CHANGE typImagesSchreiben typeImagesWrite TINYINT(1),
  CHANGE typExportDMS typeExportDms TINYINT(1),
  CHANGE typBeimAnnehmenModul typeAcceptModule TINYINT(1),
  CHANGE typBeimAnnehmenAbschliessen typeAcceptClose TINYINT(1),
  CHANGE typBeimAnnehmenModulUndAbschliessen typeAcceptModuleAndClose TINYINT(1),
  CHANGE typScriptStep typeScriptStep TINYINT(1),
  CHANGE typAutomatischScriptpfad typeAutomaticScriptPath VARCHAR(255),
  CHANGE typAutomatischScriptpfad2 typeAutomaticScriptPath2 VARCHAR(255),
  CHANGE typAutomatischScriptpfad3 typeAutomaticScriptPath3 VARCHAR(255),
  CHANGE typAutomatischScriptpfad4 typeAutomaticScriptPath4 VARCHAR(255),
  CHANGE typAutomatischScriptpfad5 typeAutomaticScriptPath5 VARCHAR(255),
  CHANGE typBeimAbschliessenVerifizieren typeCloseVerify TINYINT(1),
  CHANGE typModulName typeModuleName VARCHAR(255),
  CHANGE BearbeitungsBenutzerID user_id INT(11)
    COMMENT 'This field contains information about user, which works on this task.',
  CHANGE ProzesseID process_id INT(11);

ALTER TABLE task_x_user
  CHANGE BenutzerID user_id INT(11) NOT NULL
    COMMENT 'This field contains information about users, which are allowed to work on this task.',
  CHANGE schritteID task_id INT(11) NOT NULL;

ALTER TABLE task_x_userGroup
  CHANGE BenutzerGruppenID userGroup_id INT(11) NOT NULL
    COMMENT 'This field contains information about user''s groups, which are allowed to work on this task.',
  CHANGE schritteID task_id INT(11);

ALTER TABLE template
  CHANGE VorlagenID id INT(11) NOT NULL AUTO_INCREMENT,
  CHANGE Herkunft origin VARCHAR(255),
  CHANGE ProzesseID process_id INT(11);

ALTER TABLE templateProperty
  CHANGE vorlageneigenschaftenID id INT(11) NOT NULL AUTO_INCREMENT,
  CHANGE Titel title VARCHAR(255),
  CHANGE Wert value LONGTEXT,
  CHANGE IstObligatorisch isObligatory TINYINT(1),
  CHANGE DatentypenID dataType INT(11),
  CHANGE Auswahl choice VARCHAR(255),
  CHANGE vorlagenID template_id INT(11);

ALTER TABLE user
  CHANGE BenutzerID id INT(11) NOT NULL AUTO_INCREMENT,
  CHANGE Vorname name VARCHAR(255),
  CHANGE Nachname surname VARCHAR(255),
  CHANGE ldaplogin ldapLogin VARCHAR(255),
  CHANGE passwort password VARCHAR(255),
  CHANGE IstAktiv isActive TINYINT(1),
  CHANGE Standort location VARCHAR(255),
  CHANGE metadatensprache metadataLanguage VARCHAR(255),
  CHANGE mitMassendownload withMassDownload TINYINT(1),
  CHANGE confVorgangsdatumAnzeigen configProductionDateShow TINYINT(1),
  CHANGE Tabellengroesse tableSize INT(11),
  CHANGE sessiontimeout sessionTimeout INT(11),
  CHANGE ldapgruppenID ldapGroup_id INT(11);

ALTER TABLE user_x_userGroup
  CHANGE BenutzerID user_id INT(11) NOT NULL AUTO_INCREMENT,
  CHANGE BenutzerGruppenID userGroup_id INT(11);

ALTER TABLE userGroup
  CHANGE BenutzergruppenID id INT(11) NOT NULL AUTO_INCREMENT,
  CHANGE titel title VARCHAR(255),
  CHANGE berechtigung permission INT(11);

ALTER TABLE userProperty
  CHANGE benutzereigenschaftenID id INT(11) NOT NULL AUTO_INCREMENT,
  CHANGE Titel title VARCHAR(255),
  CHANGE Wert value VARCHAR(255),
  CHANGE IstObligatorisch isObligatory TINYINT(1),
  CHANGE DatentypenID dataType INT(11),
  CHANGE Auswahl choice VARCHAR(255),
  CHANGE BenutzerID user_id INT(11);

ALTER TABLE workpiece
  CHANGE WerkstueckeID id INT(11) NOT NULL,
  CHANGE ProzesseID process_id INT(11) NOT NULL;

ALTER TABLE workpieceProperty
  CHANGE werkstueckeeigenschaftenID id INT(11) NOT NULL AUTO_INCREMENT,
  CHANGE Titel title VARCHAR(255),
  CHANGE Wert value LONGTEXT,
  CHANGE IstObligatorisch isObligatory TINYINT(1),
  CHANGE DatentypenID dataType INT(11),
  CHANGE Auswahl choice VARCHAR(255),
  CHANGE werkstueckeID workpiece_id INT(11);

  --changeset kitodo:48
/* Migration: Remove dangling data */
/* 1. Check if process exists, if no, remove references to it in other tables */
DELETE bp FROM batch_x_process AS bp LEFT JOIN process AS p ON p.id = bp.process_id WHERE p.id IS NULL;
DELETE h FROM history AS h LEFT JOIN process AS p ON p.id = h.process_id WHERE p.id IS NULL;
DELETE t FROM task AS t LEFT JOIN process AS p ON p.id = t.process_id WHERE p.id IS NULL;
DELETE t FROM template AS t LEFT JOIN process AS p ON p.id = t.process_id WHERE p.id IS NULL;
DELETE w FROM workpiece AS w LEFT JOIN process AS p ON p.id = w.process_id WHERE p.id IS NULL;

/* 2. Check if user exists, if no, change user_id in task table to NULL */
UPDATE task SET user_id = NULL
WHERE (user_id) NOT IN (SELECT id FROM user);

--changeset kitodo:59
/* Migration: Add foreign keys */
/* 1. Add foreign keys */
ALTER TABLE batch_x_process add constraint `FK_batch_x_process_batch_id`
foreign key (batch_id) REFERENCES batch(id);

ALTER TABLE batch_x_process add constraint `FK_batch_x_process_process_id`
foreign key (process_id) REFERENCES process(id);

ALTER TABLE history add constraint `FK_history_process_id`
foreign key (process_id) REFERENCES process(id);

ALTER TABLE process add constraint `FK_process_project_id`
foreign key (project_id) REFERENCES project(id);

ALTER TABLE process add constraint `FK_process_ruleset_id`
foreign key (ruleset_id) REFERENCES ruleset(id);

ALTER TABLE process add constraint `FK_process_docket_id`
foreign key (docket_id) REFERENCES docket(id);

ALTER TABLE processProperty add constraint `FK_processProperty_process_id`
foreign key (process_id) REFERENCES process(id);

ALTER TABLE project_x_user add constraint `FK_project_x_user_project_id`
foreign key (project_id) REFERENCES project(id);

ALTER TABLE project_x_user add constraint `FK_project_x_user_user_id`
foreign key (user_id) REFERENCES user(id);

ALTER TABLE projectFileGroup add constraint `FK_projectFileGroup_project_id`
foreign key (project_id) REFERENCES project(id);

ALTER TABLE task add constraint `FK_task_user_id`
foreign key (user_id) REFERENCES user(id);

ALTER TABLE task add constraint `FK_task_process_id`
foreign key (process_id) REFERENCES process(id);

ALTER TABLE task_x_user add constraint `FK_task_x_user_task_id`
foreign key (task_id) REFERENCES task(id);

ALTER TABLE task_x_user add constraint `FK_task_x_user_user_id`
foreign key (user_id) REFERENCES user(id);

ALTER TABLE task_x_userGroup add constraint `FK_task_x_userGroup_task_id`
foreign key (task_id) REFERENCES task(id);

ALTER TABLE task_x_userGroup add constraint `FK_task_x_userGroup_userGroup_id`
foreign key (userGroup_id) REFERENCES userGroup(id);

ALTER TABLE template add constraint `FK_template_process_id`
foreign key (process_id) REFERENCES process(id);

ALTER TABLE templateProperty add constraint `FK_templateProperty_template_id`
foreign key (template_id) REFERENCES template(id);

ALTER TABLE user add constraint `FK_user_ldapGroup_id`
foreign key (ldapGroup_id) REFERENCES ldapGroup(id);

ALTER TABLE user_x_userGroup add constraint `FK_user_x_userGroup_user_id`
foreign key (user_id) REFERENCES user(id);

ALTER TABLE user_x_userGroup add constraint `FK_user_x_userGroup_userGroup_id`
foreign key (userGroup_id) REFERENCES userGroup(id);

ALTER TABLE userProperty add constraint `FK_userProperty_user_id`
foreign key (user_id) REFERENCES user(id);

ALTER TABLE workpiece add constraint `FK_workpiece_process_id`
foreign key (process_id) REFERENCES process(id);

ALTER TABLE workpieceProperty add constraint `FK_workpieceProperty_workpiece_id`
foreign key (workpiece_id) REFERENCES workpiece(id);

--changeset kitodo:70
/* Migration: Adjust database to Data Management Module */
/* 1. Rename boolean columns' names */
ALTER TABLE process
  CHANGE isTemplate template TINYINT(1),
  CHANGE isChoiceListShown inChoiceListShown TINYINT(1);

ALTER TABLE processProperty
  CHANGE isObligatory obligatory TINYINT(1);

ALTER TABLE templateProperty
  CHANGE isObligatory obligatory TINYINT(1);

ALTER TABLE user
  CHANGE isActive active TINYINT(1),
  CHANGE isVisible visible varchar(255);

ALTER TABLE userProperty
  CHANGE isObligatory obligatory TINYINT(1);

ALTER TABLE workpieceProperty
  CHANGE isObligatory obligatory TINYINT(1);

/* 2. Rename boolean columns' names */
SET FOREIGN_KEY_CHECKS = 0;
ALTER TABLE workpiece
  CHANGE id id INT(11) NOT NULL AUTO_INCREMENT;
SET FOREIGN_KEY_CHECKS = 1;

--changeset kitodo:81
ALTER TABLE docket
  CHANGE name title varchar(255) DEFAULT NULL;

--changeset kitodo:92
/* Migration: Store properties only in one table: Property */
/* 1. Remove auto_increment and primary key from property tables */
/*        sequence ids */
/*        reactivate auto_increment and primary key */
/*        hack: insert value and delete it, so LAST_INSERT_ID is up to date. */
/*
SET @rank=0;

/* for workpieceproperty */
ALTER TABLE workpieceProperty
CHANGE id id INT(11) NOT NULL;
ALTER TABLE workpieceProperty
DROP PRIMARY KEY;

UPDATE workpieceProperty
SET id=@rank:=@rank+1;
ALTER TABLE workpieceProperty
CHANGE id id INT(11) NOT NULL AUTO_INCREMENT,
ADD PRIMARY KEY(id);

/* for userproperty */
ALTER TABLE userProperty
CHANGE id id INT(11) NOT NULL;
ALTER TABLE userProperty
DROP PRIMARY KEY;

UPDATE userProperty
SET id=@rank:=@rank+1;
ALTER TABLE userProperty
CHANGE id id INT(11) NOT NULL AUTO_INCREMENT,
ADD PRIMARY KEY(id);

/* for templateproperty */
ALTER TABLE templateProperty
CHANGE id id INT(11) NOT NULL;
ALTER TABLE templateProperty
DROP PRIMARY KEY;

UPDATE templateProperty
SET id=@rank:=@rank+1;
ALTER TABLE templateProperty
CHANGE id id INT(11) NOT NULL AUTO_INCREMENT,
ADD PRIMARY KEY(id);

/* for processproperty */
ALTER TABLE processProperty
CHANGE id id INT(11) NOT NULL;
ALTER TABLE processProperty
DROP PRIMARY KEY;

UPDATE processProperty
SET id=@rank:=@rank+1;
ALTER TABLE processProperty
CHANGE id id INT(11) NOT NULL AUTO_INCREMENT,
ADD PRIMARY KEY(id);

/* 2. Create cross Tables, insert id and foreign keys from property tables */
CREATE TABLE process_x_property (
  process_id  INT(11) NOT NULL,
  property_id INT(11) NOT NULL
);

INSERT INTO process_x_property (property_id,process_id)
SELECT id, process_id
FROM processProperty;

CREATE TABLE template_x_property (
  template_id INT(11) NOT NULL,
  property_id INT(11) NOT NULL
);

INSERT INTO template_x_property (property_id,template_id)
SELECT id, template_id
FROM templateProperty;

CREATE TABLE user_x_property (
  user_id     INT(11) NOT NULL,
  property_id INT(11) NOT NULL
);

INSERT INTO user_x_property (property_id,user_id)
SELECT id, user_id
FROM userProperty;

CREATE TABLE workpiece_x_property (
  workpiece_id INT(11) NOT NULL,
  property_id  INT(11) NOT NULL
);

INSERT INTO workpiece_x_property (property_id,workpiece_id)
SELECT id, workpiece_id
FROM workpieceProperty;

/* 3. Create property table */
CREATE TABLE property (
  id int(11) NOT NULL,
  title varchar(255) DEFAULT NULL,
  value longtext DEFAULT NULL,
  obligatory tinyint(1) DEFAULT NULL,
  dataType int(11) DEFAULT NULL,
  choice varchar(255) DEFAULT NULL,
  creationDate datetime DEFAULT NULL,
  container int(11) DEFAULT NULL,
  PRIMARY KEY (id)
);

/* 4. Insert values into property table from old [...]property tables */
INSERT INTO property (id, title, value, obligatory, dataType, choice, creationDate, container)
       SELECT id, title, value, obligatory, dataType, choice, creationDate, container
       FROM processProperty;
INSERT INTO property (id, title, value, obligatory, dataType, choice, creationDate, container)
       SELECT id, title, value, obligatory, dataType, choice, creationDate, container
       FROM templateProperty;
INSERT INTO property (id, title, value, obligatory, dataType, choice, creationDate)
       SELECT id, title, value, obligatory, dataType, choice, creationDate
       FROM userProperty;
INSERT INTO property (id, title, value, obligatory, dataType, choice, creationDate, container)
       SELECT id, title, value, obligatory, dataType, choice, creationDate, container
       FROM workpieceProperty;

/* 5. Introduce auto_increment to property table */
ALTER TABLE property
CHANGE id id INT(11) NOT NULL AUTO_INCREMENT;

/* 6. Delete old [...]property tables */
DROP TABLE processProperty;
DROP TABLE templateProperty;
DROP TABLE userProperty;
DROP TABLE workpieceProperty;

/* 7. Add foreign keys to cross tables */
ALTER TABLE process_x_property ENGINE=InnoDB;
ALTER TABLE process_x_property
   ADD CONSTRAINT `FK_process_x_property_process_id`
 FOREIGN KEY (process_id) REFERENCES process (id);
ALTER TABLE process_x_property
   ADD CONSTRAINT `FK_process_x_property_property_id`
 FOREIGN KEY (property_id) REFERENCES property (id);


ALTER TABLE template_x_property ENGINE=InnoDB;
ALTER TABLE template_x_property
   ADD CONSTRAINT `FK_template_x_property_template_id`
 FOREIGN KEY (template_id) REFERENCES template (id);
ALTER TABLE template_x_property
   ADD CONSTRAINT `FK_template_x_property_property_id`
 FOREIGN KEY (property_id) REFERENCES property (id);

ALTER TABLE user_x_property ENGINE=InnoDB;
 ALTER TABLE user_x_property
   ADD CONSTRAINT `FK_user_x_property_user_id`
 FOREIGN KEY (user_id) REFERENCES user (id);
 ALTER TABLE user_x_property
   ADD CONSTRAINT `FK_user_x_property_property_id`
 FOREIGN KEY (property_id) REFERENCES property (id);

ALTER TABLE workpiece_x_property ENGINE=InnoDB;
ALTER TABLE workpiece_x_property
   ADD CONSTRAINT `FK_workpiece_x_property_workpiece_id`
 FOREIGN KEY (workpiece_id) REFERENCES workpiece (id);
ALTER TABLE workpiece_x_property
   ADD CONSTRAINT `FK_workpiece_x_property_property_id`
 FOREIGN KEY (property_id) REFERENCES property (id);

--changeset kitodo:104
/* Migration: Add column indexed to tables from which data is indexed in ElasticSearch */
/* 1. Add columns */
ALTER TABLE batch ADD indexAction VARCHAR(6);
ALTER TABLE docket ADD indexAction VARCHAR(6);
ALTER TABLE history ADD indexAction VARCHAR(6);
ALTER TABLE process ADD indexAction VARCHAR(6);
ALTER TABLE project ADD indexAction VARCHAR(6);
ALTER TABLE property ADD indexAction VARCHAR(6);
ALTER TABLE ruleset ADD indexAction VARCHAR(6);
ALTER TABLE task ADD indexAction VARCHAR(6);
ALTER TABLE template ADD indexAction VARCHAR(6);
ALTER TABLE user ADD indexAction VARCHAR(6);
ALTER TABLE userGroup ADD indexAction VARCHAR(6);
ALTER TABLE workpiece ADD indexAction VARCHAR(6);

/* 2. Add columns */
UPDATE batch SET indexAction = 'INDEX';
UPDATE docket SET indexAction = 'INDEX';
UPDATE history SET indexAction = 'INDEX';
UPDATE process SET indexAction = 'INDEX';
UPDATE project SET indexAction = 'INDEX';
UPDATE property SET indexAction = 'INDEX';
UPDATE ruleset SET indexAction = 'INDEX';
UPDATE task SET indexAction = 'INDEX';
UPDATE template SET indexAction = 'INDEX';
UPDATE user SET indexAction = 'INDEX';
UPDATE userGroup SET indexAction = 'INDEX';
UPDATE workpiece SET indexAction = 'INDEX';

--changeset kitodo:115
ALTER TABLE process ADD processBaseUri tinyblob;
ALTER TABLE process DROP COLUMN swappedOut;

--changeset kitodo:128
/* Migration: Move user property to filter table */
/* 1. Create filter table */
CREATE TABLE filter (
  id INT(11) NOT NULL,
  value longtext DEFAULT NULL,
  creationDate datetime DEFAULT NULL,
  indexAction VARCHAR(6),
  user_id INT(11),
  PRIMARY KEY (id)
);

/* 2. Move filters from property table to filter table */
INSERT INTO filter (id, value, creationDate)
       SELECT id, value, creationDate
       FROM property
       WHERE title = '_filter';

/* 3. Update table with id */
UPDATE filter AS f, user_x_property AS p SET f.user_id = p.user_id WHERE p.property_id = f.id;

/* 4. Drop foreign keys from user_x_property table */
ALTER TABLE user_x_property DROP FOREIGN KEY `FK_user_x_property_user_id`;
ALTER TABLE user_x_property DROP FOREIGN KEY `FK_user_x_property_property_id`;

/* 5. Drop user_x_property table */
DROP TABLE user_x_property;

/* 6. Introduce auto_increment to filter table */
ALTER TABLE filter
  CHANGE id id INT(11) NOT NULL AUTO_INCREMENT;

/* 7. Add foreign keys to user table */
ALTER TABLE filter ENGINE=InnoDB;
ALTER TABLE filter
  ADD CONSTRAINT `FK_filter_x_user_id`
FOREIGN KEY (user_id) REFERENCES user (id);

/* 8. Delete all filters from property table */
DELETE FROM property WHERE title = '_filter';

--changeset kitodo:129
/* Migration: Change column visible in user table from varchar to tinyint */
/* 1. Update column visible to store correct tinyint values */

UPDATE user SET visible = '1' WHERE trim(visible) = 'deleted';
UPDATE user SET visible = '0' WHERE visible != '1';
UPDATE user SET visible = '0' WHERE visible IS NULL;

/* 2. Change column visible to store correct tinyint values */

ALTER TABLE user
  CHANGE visible deleted TINYINT(1) NOT NULL;

--changeset kitodo:28
/*Migration: Remove script columns from task table*/
/*1. Remove columns */
ALTER TABLE task
  DROP COLUMN scriptName2,
  DROP COLUMN typeAutomaticScriptPath2,
  DROP COLUMN scriptName3,
  DROP COLUMN typeAutomaticScriptPath3,
  DROP COLUMN scriptName4,
  DROP COLUMN typeAutomaticScriptPath4,
  DROP COLUMN scriptName5,
  DROP COLUMN typeAutomaticScriptPath5;

/*2. Rename column scriptName1*/
ALTER TABLE task
  CHANGE scriptName1 scriptName VARCHAR(255);

--changeset kitodo:29
/* Convert the char set to uft8mb4 on all existing tables and their columns.*/
/* This does not change the global setting for database.*/
ALTER TABLE batch CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE batch_x_process CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE docket CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE filter CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE history CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE ldapGroup CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE process CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE process_x_property CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE project CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE project_x_user CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE projectFileGroup CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE property CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE ruleset CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE task CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE task_x_user CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE task_x_userGroup CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE userGroup CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE workpiece CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE workpiece_x_property CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE template CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE template_x_property CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE user CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE user_x_userGroup CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

--changeset kitodo:30
/*Sets the used CSS-file for every user to null.*/
UPDATE user SET css = NULL;

--changeset kitodo:31
/*Remove module related columns.*/
ALTER TABLE task DROP COLUMN typeAcceptModule;
ALTER TABLE task DROP COLUMN typeAcceptModuleAndClose;
ALTER TABLE task DROP COLUMN typeModuleName;

--changeset kitodo:32
/*Remove plugins' related columns.*/
ALTER TABLE task DROP COLUMN stepPlugin;
ALTER TABLE task DROP COLUMN validationPlugin;

--changeset kitodo:33
/*Adjust task table to use scriptPath / scriptName to determine if task has a script.*/
/*1. Rename typeAutomaticScriptPath column to scriptPath in task table.*/
ALTER TABLE task CHANGE typeAutomaticScriptPath scriptPath VARCHAR(255);

/*2. Remove typeScriptStep column in task table.*/
ALTER TABLE task DROP COLUMN typeScriptStep;

--changeset kitodo:34
/*Add authorizations table*/
CREATE TABLE authorization (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(255) DEFAULT NULL,
  `indexAction` VARCHAR(6) DEFAULT NULL,
  PRIMARY KEY (`id`))
  DEFAULT CHARACTER SET = utf8mb4 COLLATE utf8mb4_unicode_ci;

/*Insert admin authorization*/
INSERT INTO authorization (`title`, `indexAction`) VALUES ('admin', 'INDEX');

/*Add table userGroup_x_authorization*/
CREATE TABLE userGroup_x_authorization (
  `userGroup_id` INT(11) NOT NULL,
  `authorization_id` INT(11) DEFAULT NULL)
  DEFAULT CHARACTER SET = utf8mb4 COLLATE utf8mb4_unicode_ci;

/*Insert ids of existing userGroups which have permission = 1 (admin permissions)*/
INSERT INTO userGroup_x_authorization (userGroup_id)
  SELECT id FROM userGroup WHERE permission=1;

/*Set authorization ids to 1 (inserted admin authorization)*/
UPDATE userGroup_x_authorization SET authorization_id = 1;

/*Set authorization_id column to not null*/
ALTER TABLE userGroup_x_authorization MODIFY COLUMN authorization_id INT(11) NOT NULL;

/*Add primary keys*/
ALTER TABLE userGroup_x_authorization ADD PRIMARY KEY (`authorization_id`,`userGroup_id`);

/*Add foreign keys*/
ALTER TABLE userGroup_x_authorization ADD CONSTRAINT `FK_userGroup_x_authorization_userGroup_id`
foreign key (userGroup_id) REFERENCES userGroup(id);

ALTER TABLE userGroup_x_authorization ADD CONSTRAINT `FK_userGroup_x_authorization_authorization_id`
foreign key (authorization_id) REFERENCES authorization(id);

--changeset kitodo:35
/*Migration: Changing the columns for process id in template table to not null*/
ALTER TABLE template MODIFY process_id INT(11) NOT NULL;

--changeset kitodo:36
/* Migration: Changing the columns for authorization title, userGroup title and project title to not null.*/
/*            Changing the columns for user login, authorization title, userGroup title and project title to unique.*/
/*            The setting unique query will not work on mysql versions below 5.7.7*/

ALTER TABLE user ADD UNIQUE (login);
/*user login is not set to NOT NUll because the NULL state is used at self destruct method*()*/

ALTER TABLE authorization ADD UNIQUE (title);
ALTER TABLE authorization MODIFY COLUMN title VARCHAR(255) NOT NULL;

ALTER TABLE userGroup ADD UNIQUE (title);
ALTER TABLE userGroup MODIFY COLUMN title VARCHAR(255) NOT NULL;

ALTER TABLE project ADD UNIQUE (title);
ALTER TABLE project MODIFY COLUMN title VARCHAR(255) NOT NULL;

--changeset kitodo:38
/*1. Drop foreign keys*/
ALTER TABLE template_x_property DROP FOREIGN KEY `FK_template_x_property_template_id`;
ALTER TABLE template_x_property DROP FOREIGN KEY `FK_template_x_property_property_id`;

ALTER TABLE workpiece_x_property DROP FOREIGN KEY `FK_workpiece_x_property_workpiece_id`;
ALTER TABLE workpiece_x_property DROP FOREIGN KEY `FK_workpiece_x_property_property_id`;

/*2. Update tables with new column names*/
ALTER TABLE template_x_property
  CHANGE template_id process_id INT(11) NOT NULL;
ALTER TABLE workpiece_x_property
  CHANGE workpiece_id process_id INT(11) NOT NULL;

/*3. Update data in cross tables*/

UPDATE template_x_property JOIN template ON template_x_property.process_id = template.id
  SET template_x_property.process_id = template.process_id;
UPDATE workpiece_x_property JOIN workpiece ON workpiece_x_property.process_id = workpiece.id
  SET workpiece_x_property.process_id = workpiece.process_id;

/*4. Delete obsolete tables*/

DROP TABLE template;
DROP TABLE workpiece;

/*5. Restore foreign keys*/

ALTER TABLE template_x_property
   ADD CONSTRAINT `FK_template_x_property_template_id`
 FOREIGN KEY (process_id) REFERENCES process (id);
ALTER TABLE template_x_property
   ADD CONSTRAINT `FK_template_x_property_property_id`
 FOREIGN KEY (property_id) REFERENCES property (id);

ALTER TABLE workpiece_x_property
   ADD CONSTRAINT `FK_workpiece_x_property_workpiece_id`
 FOREIGN KEY (process_id) REFERENCES process (id);
ALTER TABLE workpiece_x_property
   ADD CONSTRAINT `FK_workpiece_x_property_property_id`
 FOREIGN KEY (property_id) REFERENCES property (id);

--changeset kitodo:39
/*Remove plugins' related columns.*/
ALTER TABLE property DROP COLUMN container;

--changeset kitodo:40
CREATE TABLE ldapServer (
  `id` INT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(255) DEFAULT NULL,
  `url` VARCHAR(255) DEFAULT NULL,
  `managerLogin` VARCHAR(255) DEFAULT NULL,
  `managerPassword` VARCHAR(255) DEFAULT NULL,
  `nextFreeUnixIdPattern` VARCHAR(255) DEFAULT NULL,
  `useSsl` TINYINT(1) NOT NULL DEFAULT 0,
  `readOnly` TINYINT(1) NOT NULL DEFAULT 0,
  `passwordEncryption` INT NOT NULL DEFAULT 0,
  `rootCertificate` VARCHAR(255) DEFAULT NULL,
  `pdcCertificate` VARCHAR(255) DEFAULT NULL,
  `keystore` VARCHAR(255) DEFAULT NULL,
  `keystorePassword` VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (`id`))
  DEFAULT CHARACTER SET = utf8mb4 COLLATE utf8mb4_unicode_ci;

ALTER TABLE ldapGroup
ADD COLUMN `ldapserver_id` INT(11) NULL DEFAULT NULL;

ALTER TABLE ldapGroup ADD CONSTRAINT `FK_ldapGroup_ldapServer_id`
foreign key (ldapserver_id) REFERENCES ldapServer(id);

--changeset kitodo:41
/* Rename authorization to authority */
ALTER TABLE authorization RENAME TO authority;

ALTER TABLE userGroup_x_authorization
  DROP FOREIGN KEY FK_userGroup_x_authorization_authorization_id,
  DROP FOREIGN KEY FK_userGroup_x_authorization_userGroup_id;

ALTER TABLE userGroup_x_authorization
  CHANGE COLUMN authorization_id authority_id INT(11) NOT NULL ,
  DROP INDEX FK_userGroup_x_authorization_userGroup_id ,
  ADD INDEX FK_userGroup_x_authority_userGroup_id (userGroup_id ASC),
  RENAME TO  userGroup_x_authority;

ALTER TABLE userGroup_x_authority
  ADD CONSTRAINT FK_userGroup_x_authority_authority_id
  FOREIGN KEY (authority_id)
  REFERENCES authority (id),
  ADD CONSTRAINT FK_userGroup_x_authority_userGroup_id
  FOREIGN KEY (userGroup_id)
  REFERENCES userGroup (id);

/*Add client table*/

CREATE TABLE client (
  id INT NOT NULL AUTO_INCREMENT,
  name VARCHAR(255) NULL,
  indexAction VARCHAR(6) DEFAULT NULL,
  PRIMARY KEY (id))
  DEFAULT CHARACTER SET = utf8mb4 COLLATE utf8mb4_unicode_ci;

ALTER TABLE project
  ADD COLUMN client_id INT(11) NULL DEFAULT NULL;

ALTER TABLE project ADD CONSTRAINT FK_project_client_id
  foreign key (client_id) REFERENCES client(id);

/*Add authority relation tables*/

CREATE TABLE userGroup_x_client_x_authority (
  id INT NOT NULL AUTO_INCREMENT,
  userGroup_id INT NOT NULL,
  client_id INT NOT NULL,
  authority_id INT NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY unique_index(`userGroup_id`, `client_id`,`authority_id`))
  DEFAULT CHARACTER SET = utf8mb4 COLLATE utf8mb4_unicode_ci;

ALTER TABLE userGroup_x_client_x_authority add constraint `FK_userGroup_x_client_x_authority_userGroup_id`
foreign key (userGroup_id) REFERENCES userGroup(id);

ALTER TABLE userGroup_x_client_x_authority add constraint `FK_userGroup_x_client_x_authority_client_id`
foreign key (client_id) REFERENCES client(id);

ALTER TABLE userGroup_x_client_x_authority add constraint `FK_userGroup_x_client_x_authority_authority_id`
foreign key (authority_id) REFERENCES authority(id);


CREATE TABLE userGroup_x_project_x_authority (
  id INT NOT NULL AUTO_INCREMENT,
  userGroup_id INT NOT NULL,
  project_id INT NOT NULL,
  authority_id INT NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY unique_index(`userGroup_id`, `project_id`,`authority_id`))
  DEFAULT CHARACTER SET = utf8mb4 COLLATE utf8mb4_unicode_ci;

ALTER TABLE userGroup_x_project_x_authority add constraint `FK_userGroup_x_project_x_authority_userGroup_id`
foreign key (userGroup_id) REFERENCES userGroup(id);

ALTER TABLE userGroup_x_project_x_authority add constraint `FK_userGroup_x_project_x_authority_project_id`
foreign key (project_id) REFERENCES project(id);

ALTER TABLE userGroup_x_project_x_authority add constraint `FK_userGroup_x_project_x_authority_authority_id`
foreign key (authority_id) REFERENCES authority(id);

--changeset kitodo:42
/* Add assignable columns to authority table*/
ALTER TABLE authority
  ADD COLUMN globalAssignable tinyint(1) DEFAULT 1,
  ADD COLUMN clientAssignable tinyint(1) DEFAULT 1,
  ADD COLUMN projectAssignable tinyint(1) DEFAULT 1;

--changeset kitodo:43
/* Remove history table*/
DROP TABLE history;

--changeset kitodo:44
/*Update admin authority that it can only assigned globally*/

UPDATE authority SET clientAssignable='0',projectAssignable='0' WHERE title='admin';

/*Add authorities to secure the access to the entities*/

/* Client*/
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewAllClients', '1', '0', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewClient', '1', '1', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('editClient', '1', '1', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('deleteClient', '1', '0', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('addClient', '1', '0', '0');

/* Project*/
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewProject', '1', '1', '1');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewAllProjects', '1', '1', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('editProject', '1', '1', '1');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('deleteProject', '1', '1', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('addProject', '1', '1', '0');

/* Docket*/
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewAllDockets', '1', '0', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewDocket', '1', '0', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('addDocket', '1', '0', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('editDocket', '1', '0', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('deleteDocket', '1', '0', '0');

/* Process*/
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewAllProcesses', '1', '1', '1');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewProcess', '1', '1', '1');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('addProcess', '1', '1', '1');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('editProcess', '1', '1', '1');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('deleteProcess', '1', '1', '1');

/* Task */
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewAllTasks', '1', '1', '1');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewTask', '1', '1', '1');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('addTask', '1', '1', '1');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('editTask', '1', '1', '1');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('deleteTask', '1', '1', '1');

/* UserGroup */
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewAllUserGroups', '1', '1', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewUserGroup', '1', '1', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('addUserGroup', '1', '1', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('editUserGroup', '1', '1', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('deleteUserGroup', '1', '1', '0');

/* User */
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewAllUsers', '1', '1', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewUser', '1', '1', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('addUser', '1', '1', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('editUser', '1', '1', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('deleteUser', '1', '1', '0');

/* Assign admin authority to all user groups which has permission = 1 */
/* admin authority was inserted at V2_17 */

INSERT IGNORE INTO userGroup_x_authority (userGroup_id,authority_id)
SELECT userGroup.id,1 FROM userGroup WHERE permission = '1';

--changeset kitodo:45
/* Replace projectIsArchived column with active column and flip its value.*/
/* 1. Rename projectIsArchived column to active.*/
ALTER TABLE project CHANGE projectIsArchived active TINYINT(1);

/* 2. Flip the value of the entries.*/
UPDATE project SET active = NOT active;

--changeset kitodo:46
ALTER TABLE userGroup DROP COLUMN permission;

/* Ruleset */
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewAllRulesets', '1', '0', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewRuleset', '1', '0', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('addRuleset', '1', '0', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('editRuleset', '1', '0', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('deleteRuleset', '1', '0', '0');

--changeset kitodo:47
/* Migration: Split processes and templates */

/* 1. Add template table */
CREATE TABLE template (
  id INT(11) NOT NULL AUTO_INCREMENT,
  title VARCHAR (255) DEFAULT NULL,
  outputName VARCHAR (255) DEFAULT NULL,
  creationDate datetime DEFAULT NULL,
  inChoiceListShown tinyint(1) DEFAULT NULL,
  sortHelperStatus VARCHAR(255) DEFAULT NULL,
  wikiField longtext,
  project_id INT(11) DEFAULT NULL,
  ruleset_id INT(11) DEFAULT NULL,
  docket_id INT(11) DEFAULT NULL,
  indexAction VARCHAR(6),
  old_id INT(11) NOT NULL,
  PRIMARY KEY (`id`)
) DEFAULT CHARACTER SET = utf8mb4 COLLATE utf8mb4_unicode_ci;

/* 2. Copy templates to template table */
INSERT INTO template (title, outputName, creationDate, inChoiceListShown, sortHelperStatus, wikiField, project_id, ruleset_id, docket_id, old_id)
SELECT title, outputName, creationDate, inChoiceListShown, sortHelperStatus, wikiField, project_id, ruleset_id, docket_id, id
FROM process WHERE template = 1;

/* 3. Update process and task table - add template_id column*/
ALTER TABLE process ADD template_id INT(11) DEFAULT NULL;
ALTER TABLE task ADD template_id INT(11) DEFAULT NULL;

/* 4. Switch off safe updates*/
SET SQL_SAFE_UPDATES = 0;

/* 5. Update process table - add template_id column*/
UPDATE process AS p
  INNER JOIN process_x_property AS pxp ON p.id = pxp.process_id
  INNER JOIN property AS pp ON pxp.property_id = pp.id
  INNER JOIN template AS t ON t.old_id = pp.value
SET p.template_id = t.id WHERE pp.title = 'TemplateID';

/* 6. Remove foreign key process - property */
ALTER TABLE process_x_property DROP FOREIGN KEY `FK_process_x_property_process_id`;
ALTER TABLE process_x_property DROP FOREIGN KEY `FK_process_x_property_property_id`;

/* 7. Remove entries from process_x_property table */
DELETE pxp FROM process_x_property AS pxp
  INNER JOIN template AS t ON pxp.process_id = t.old_id
WHERE pxp.process_id = t.old_id;

/* 8. Remove foreign key task - process */
ALTER TABLE task DROP FOREIGN KEY FK_task_process_id;

/* 9. Replace templates ids in task table */
UPDATE task AS t
  INNER JOIN template AS temp ON t.process_id = temp.old_id
SET t.template_id = temp.id,
  t.process_id = NULL
WHERE t.process_id = temp.old_id;

/* 10. Delete processes from batches that are templates */
/* If someone ever linked that together, which was possible in the front-end, */
/* this is a mistake in the data. Batches are not intended to ever contain */
/* templates, and those batches cannot ever have been useful for anything. */
DELETE batch_x_process FROM batch_x_process
  LEFT JOIN process ON batch_x_process.process_id = process.id
  WHERE process.template = 1;

/* 11. Remove templates from process table */
DELETE FROM process
WHERE template = 1;

/* 12. Switch on safe updates */
SET SQL_SAFE_UPDATES = 1;

/* 13. Drop column with old ids */
ALTER TABLE template DROP old_id;

/* 14. Add foreign keys */
ALTER TABLE task ADD CONSTRAINT `FK_task_process_id`
FOREIGN KEY (process_id) REFERENCES process(id);

ALTER TABLE task ADD CONSTRAINT `FK_task_template_id`
FOREIGN KEY (template_id) REFERENCES template(id);

ALTER TABLE template ADD CONSTRAINT `FK_template_project_id`
FOREIGN KEY (project_id) REFERENCES project(id);

ALTER TABLE template ADD CONSTRAINT `FK_template_ruleset_id`
FOREIGN KEY (ruleset_id) REFERENCES ruleset(id);

ALTER TABLE template ADD CONSTRAINT `FK_template_docket_id`
FOREIGN KEY (docket_id) REFERENCES docket(id);

ALTER TABLE process ADD CONSTRAINT `FK_process_template_id`
FOREIGN KEY (template_id) REFERENCES template(id);

ALTER TABLE process_x_property ADD CONSTRAINT `FK_process_x_property_process_id`
FOREIGN KEY (process_id) REFERENCES process (id);

ALTER TABLE process_x_property ADD CONSTRAINT `FK_process_x_property_property_id`
FOREIGN KEY (property_id) REFERENCES property (id);

--changeset kitodo:49
/* Migration: Remove template column from process table */
ALTER TABLE process DROP template;

--changeset kitodo:50
ALTER TABLE user DROP COLUMN sessionTimeout;

--changeset kitodo:51
/* Migration: Add workflow template/*-- 1. Create table workflow */
CREATE TABLE workflow (
  id INT(11) NOT NULL AUTO_INCREMENT,
  title VARCHAR (255) DEFAULT NULL,
  fileName VARCHAR (255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) DEFAULT CHARACTER SET = utf8mb4 COLLATE utf8mb4_unicode_ci;

/* 2. Add column related to workflow to template table */
ALTER TABLE template ADD workflow_id INT(11) DEFAULT NULL;

/* 3. Add column related to workflow to task table */
ALTER TABLE task ADD workflowCondition VARCHAR (255) DEFAULT NULL;

/* 4. Add foreign key */
ALTER TABLE template add constraint `FK_template_workflow_id`
foreign key (workflow_id) REFERENCES workflow(id);

--changeset kitodo:52
ALTER TABLE task ADD COLUMN workflowId VARCHAR(255) DEFAULT NULL;

--changeset kitodo:53
/* Change ruleset authorities added in V2_28 */
UPDATE authority SET clientAssignable='1' WHERE title='viewAllRulesets';
UPDATE authority SET clientAssignable='1' WHERE title='viewRuleset';
UPDATE authority SET clientAssignable='1' WHERE title='addRuleset';
UPDATE authority SET clientAssignable='1' WHERE title='editRuleset';
UPDATE authority SET clientAssignable='1' WHERE title='deleteRuleset';

/* Change docket authorities added in V2_26 */
UPDATE authority SET clientAssignable='1' WHERE title='viewAllDockets';
UPDATE authority SET clientAssignable='1' WHERE title='viewDocket';
UPDATE authority SET clientAssignable='1' WHERE title='addDocket';
UPDATE authority SET clientAssignable='1' WHERE title='editDocket';
UPDATE authority SET clientAssignable='1' WHERE title='deleteDocket';

/* LdapGroup */
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewAllLdapGroups', '1', '0', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewLdapGroup', '1', '0', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('addLdapGroup', '1', '0', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('editLdapGroup', '1', '0', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('deleteLdapGroup', '1', '0', '0');

/* LdapServer */
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewAllLdapServers', '1', '0', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewLdapServer', '1', '0', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('addLdapServer', '1', '0', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('editLdapServer', '1', '0', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('deleteLdapServer', '1', '0', '0');

/* Interaction with Index */
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewIndex', '1', '0', '0');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('editIndex', '1', '0', '0');

/* Interaction with Process */
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('editProcessMetaData', '1', '1', '1');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('editProcessStructureData', '1', '1', '1');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('editProcessPagination', '1', '1', '1');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('editProcessImages', '1', '1', '1');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewProcessMetaData', '1', '1', '1');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewProcessStructureData', '1', '1', '1');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewProcessPagination', '1', '1', '1');
INSERT INTO authority (title, globalAssignable, clientAssignable, projectAssignable)
VALUES ('viewProcessImages', '1', '1', '1');

--changeset kitodo:54
ALTER TABLE workflow ADD COLUMN archived TINYINT(1) DEFAULT 0;
ALTER TABLE workflow ADD COLUMN ready TINYINT(1) DEFAULT 0;

--changeset kitodo:55
ALTER TABLE docket ADD COLUMN active TINYINT(1) DEFAULT 1;
ALTER TABLE ruleset ADD COLUMN active TINYINT(1) DEFAULT 1;
ALTER TABLE workflow CHANGE archived active TINYINT(1) DEFAULT 1;

--changeset kitodo:56
/* Migration: Add column indexed to workflow table from which data is indexed in ElasticSearch */
ALTER TABLE workflow ADD indexAction VARCHAR(6);

--changeset kitodo:57
/* Migration: Add column client_id to docket and ruleset tables */
/* 1. Add columns */
ALTER TABLE docket ADD client_id INT(11);
ALTER TABLE ruleset ADD client_id INT(11);

/* 2. Add foreign keys */ 
ALTER TABLE docket add constraint `FK_docket_client_id`
foreign key (client_id) REFERENCES client(id);

ALTER TABLE ruleset add constraint `FK_ruleset_client_id`
foreign key (client_id) REFERENCES client(id);

--changeset kitodo:58
/* Migration: Add column language to user table */
/* 1. Add columns */
ALTER TABLE user ADD COLUMN language VARCHAR (255) DEFAULT 'de';

--changeset kitodo:60
/* Create columns (boolean) copyFolder, (boolean) createFolder, */
/*     (double) derivative, (int) dpi, (double) imageScale, (int) imageSize, */
/*     (enum {ALL, EXISTING, NO, PREVIEW_IMAGE}) linkingMode */

ALTER TABLE projectFileGroup
  ADD copyFolder   tinyint(1)  NOT NULL DEFAULT 1
        COMMENT 'whether the folder is copied during export',
  ADD createFolder tinyint(1)  NOT NULL DEFAULT 1
        COMMENT 'whether the folder is created with a new process',
  ADD derivative   double               DEFAULT NULL
        COMMENT 'the percentage of scaling for createDerivative()',
  ADD dpi          int(11)              DEFAULT NULL
        COMMENT 'the new DPI for changeDpi()',
  ADD imageScale   double               DEFAULT NULL
        COMMENT 'the percentage of scaling for getScaledWebImage()',
  ADD imageSize    int(11)              DEFAULT NULL
        COMMENT 'the new width in pixels for getSizedWebImage()',
  ADD linkingMode  varchar(13) NOT NULL DEFAULT 'ALL'
        COMMENT 'how to link the contents in a METS fileGrp',
  ADD CONSTRAINT CK_folder_linkingMode
        CHECK (linkingMode IN ('ALL', 'EXISTING', 'NO', 'PREVIEW_IMAGE'));


/* Make sure there are no NULL values in string fields. This should not be the */
/*     case, but may be on databases with a long version history. */

UPDATE projectFileGroup SET name = '' WHERE id > 0 AND name IS NULL;
UPDATE projectFileGroup SET path = '' WHERE id > 0 AND path IS NULL;
UPDATE projectFileGroup SET folder = '' WHERE id > 0 AND folder IS NULL;


/* Set 'linkingMode' column to 'EXISTING' where 'folder' is not empty */

UPDATE projectFileGroup SET linkingMode = 'EXISTING'
  WHERE id > 0 AND folder <> '';


/* Set 'linkingMode' to 'PREVIEW_IMAGE' where 'previewImage' = 1 */

UPDATE projectFileGroup SET linkingMode = 'PREVIEW_IMAGE'
  WHERE id > 0 AND previewImage = 1;


/* Rename columns 'name' => 'fileGroup', 'path' => 'urlStructure', */
/*     'folder' => 'path' (no column with same name as table) */

ALTER TABLE projectFileGroup
  CHANGE name   fileGroup    varchar(255) NOT NULL DEFAULT ''
        COMMENT 'USE attribute for METS fileGroup',
  CHANGE path   urlStructure varchar(255) NOT NULL DEFAULT ''
        COMMENT 'Path where the folder is published on a web server',
  CHANGE folder path         varchar(255) NOT NULL DEFAULT ''
        COMMENT 'Path to the folder relative to the process directory, may contain variables';


/* Rename table 'projectfilegroup' into 'folder' */

ALTER TABLE projectFileGroup RENAME TO folder;


/* Fill in path column */
/* In this example, we use the Linux (and Java default) file separator and the */
/*     _tif suffix for the source images folder. You may want to adjust these */
/*     values before migrating your system. */

UPDATE folder SET path = 'images/(processtitle)_tif'
  WHERE id > 0 AND fileGroup = 'LOCAL' AND path = '';

UPDATE folder SET path = 'pdf'
  WHERE id > 0 AND fileGroup = 'DOWNLOAD' AND path = '';

UPDATE folder SET path = 'ocr/alto'
  WHERE id > 0 AND fileGroup = 'FULLTEXT' AND path = '';

/* all remaining cases */
UPDATE folder SET path = CONCAT('jpgs/', LOWER(fileGroup))
  WHERE id > 0 AND path = '';


/* Delete suffix in all cases it is equal to the configured file extension. */

UPDATE folder
  SET suffix = ''
  WHERE id > 0 AND (
    mimeType = 'image/jpeg' AND suffix = 'jpg' OR
    mimeType = 'application/pdf' AND suffix = 'pdf' OR
    (mimeType = 'text/xml' OR mimeType = 'application/alto+xml') 
      AND suffix = 'xml' OR
    mimeType = 'image/tiff' AND suffix = 'tif' OR
    mimeType = 'image/png' AND suffix = 'png' OR
    mimeType = 'image/jp2' AND suffix = 'jp2' OR
    mimeType = 'image/bmp' AND suffix = 'bmp' OR
    mimeType = 'image/gif' AND suffix = 'gif'
  );


/* In the remaining cases, replace the configured file extension by the */
/* replcement character if the suffix ends with it. */

UPDATE folder
  SET suffix = concat(substring(suffix, 1, char_length(suffix) - 4), '.*')
  WHERE id > 0 AND (
    mimeType = 'image/jpeg' AND right(suffix, 4) = '.jpg' OR
    mimeType = 'application/pdf' AND suffix = '.pdf' OR
    (mimeType = 'text/xml' OR mimeType = 'application/alto+xml') 
      AND right(suffix, 4) = '.xml' OR
    mimeType = 'image/tiff' AND right(suffix, 4) = '.tif' OR
    mimeType = 'image/png' AND right(suffix, 4) = '.png' OR
    mimeType = 'image/jp2' AND right(suffix, 4) = '.jp2' OR
    mimeType = 'image/bmp' AND right(suffix, 4) = '.bmp' OR
    mimeType = 'image/gif' AND right(suffix, 4) = '.gif'
  );
  
  
/* To attach the special case suffixes to the path column, make sure the path   */
/*     ends with the file separator. In this example, we use the Linux (and     */
/*     Java default) file separator. You may want to adjust these values before */
/*     migrating your system. */

UPDATE folder
  SET path = concat(path, '/')
  WHERE id > 0 AND suffix <> '' AND right(path, 1) <> '/';


/* Attach the special case suffixes to the path column. */

UPDATE folder
  SET path = concat(path, concat('*.', suffix))
  WHERE id > 0 AND suffix <> '';


/* Delete columns 'suffix' and 'previewImage'. 'previewImage' is now part of */
/*     'linkingMode'; 'suffix' depends on 'mimeType' and needs no extra storage. */

ALTER TABLE folder
  DROP previewImage,
  DROP suffix;


/* Rename foreign key constraints */

ALTER TABLE folder
  DROP FOREIGN KEY `FK_projectFileGroup_project_id`,
  ADD CONSTRAINT `FK_folder_project_id` FOREIGN KEY (project_id) REFERENCES project (id);

--changeset kitodo:61
ALTER TABLE template ADD COLUMN active TINYINT(1) DEFAULT 1;

--changeset kitodo:62
/* 1. Create cross table */
CREATE TABLE project_x_template (
  project_id  INT(11) NOT NULL,
  template_id INT(11) NOT NULL
) ENGINE=InnoDB;

/* 2. Insert id and foreign keys from project tables */
INSERT INTO project_x_template (project_id, template_id)
SELECT project_id, id
FROM template;

/* 3. Drop foreign keys from project table */
ALTER TABLE template DROP FOREIGN KEY `FK_template_project_id`;

/* 4. Remove process column */
ALTER TABLE template DROP COLUMN project_id;

/* 5. Add foreign keys to cross table */
ALTER TABLE project_x_template
   ADD CONSTRAINT `FK_project_x_template_project_id`
 FOREIGN KEY (project_id) REFERENCES project (id);
ALTER TABLE project_x_template
   ADD CONSTRAINT `FK_project_x_template_template_id`
 FOREIGN KEY (template_id) REFERENCES template (id);

--changeset kitodo:63
/* 1. Add a dummy client */
INSERT INTO client (`name`, `indexAction`) VALUES ('Client_ChangeMe', 'INDEX');

/* 2. Switch off safe updates */
SET SQL_SAFE_UPDATES = 0;

/* 3. Assign the client to every project which has no client */
UPDATE project projectTable, (SELECT * FROM client WHERE name = 'Client_ChangeMe') dummyClient
SET projectTable.client_id = dummyClient.id WHERE projectTable.client_id IS NULL;

/* 4. Switch on safe updates */
SET SQL_SAFE_UPDATES = 1;

/* 5. Set the client_id column of project table to NOT NULL */
ALTER TABLE project CHANGE COLUMN `client_id` `client_id` INT(11) NOT NULL;

--changeset kitodo:64
/* 1. Removing tables and columns of former authority relation concept */
DROP TABLE userGroup_x_project_x_authority;
DROP TABLE userGroup_x_client_x_authority;

ALTER TABLE authority
DROP COLUMN projectAssignable,
DROP COLUMN clientAssignable,
DROP COLUMN globalAssignable;

/* 2. Add table user_x_client */
CREATE TABLE client_x_user (
  `client_id` INT(11) DEFAULT NULL,
  `user_id` INT(11) NOT NULL)
  ENGINE=InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE utf8mb4_unicode_ci;

/* 3. Add foreign keys */
ALTER TABLE client_x_user add constraint `FK_client_x_user_user_id`
foreign key (user_id) REFERENCES user(id);

ALTER TABLE client_x_user add constraint `FK_client_x_user_client_id`
foreign key (client_id) REFERENCES client(id);

/* 4. Switch off safe updates */
SET SQL_SAFE_UPDATES = 0;

/* 5. Assign the dummy client to every user */
INSERT INTO client_x_user (client_id, user_id) SELECT null, id FROM user;

UPDATE client_x_user client_x_userTable, (SELECT * FROM client WHERE name = 'Client_ChangeMe') dummyClient
SET client_x_userTable.client_id = dummyClient.id WHERE client_x_userTable.client_id IS NULL;

/* 6. Set client_id column to not null */
ALTER TABLE client_x_user MODIFY COLUMN client_id INT(11) NOT NULL;

/* 7. Add '_globalAssignable' to every existing authority entry */
UPDATE authority set title=concat(title,'_globalAssignable');

/* 8. Switch on safe updates */
SET SQL_SAFE_UPDATES = 1;

/* 9. Add authorities to replace the client and project assignable columns */
/* Client */
INSERT INTO authority (title) VALUES ('viewClient_clientAssignable');
INSERT INTO authority (title) VALUES ('editClient_clientAssignable');

/* Project */
INSERT INTO authority (title) VALUES ('viewProject_clientAssignable');
INSERT INTO authority (title) VALUES ('viewAllProjects_clientAssignable');
INSERT INTO authority (title) VALUES ('editProject_clientAssignable');
INSERT INTO authority (title) VALUES ('deleteProject_clientAssignable');
INSERT INTO authority (title) VALUES ('addProject_clientAssignable');

INSERT INTO authority (title) VALUES ('viewProject_projectAssignable');
INSERT INTO authority (title) VALUES ('editProject_projectAssignable');

/* Docket */
INSERT INTO authority (title) VALUES ('viewDocket_clientAssignable');
INSERT INTO authority (title) VALUES ('viewAllDockets_clientAssignable');
INSERT INTO authority (title) VALUES ('editDocket_clientAssignable');
INSERT INTO authority (title) VALUES ('deleteDocket_clientAssignable');
INSERT INTO authority (title) VALUES ('addDocket_clientAssignable');

/* Ruleset */
INSERT INTO authority (title) VALUES ('viewRuleset_clientAssignable');
INSERT INTO authority (title) VALUES ('viewAllRulesets_clientAssignable');
INSERT INTO authority (title) VALUES ('editRuleset_clientAssignable');
INSERT INTO authority (title) VALUES ('deleteRuleset_clientAssignable');
INSERT INTO authority (title) VALUES ('addRuleset_clientAssignable');

/* Process */
INSERT INTO authority (title) VALUES ('viewProcess_clientAssignable');
INSERT INTO authority (title) VALUES ('viewAllProcesses_clientAssignable');
INSERT INTO authority (title) VALUES ('editProcess_clientAssignable');
INSERT INTO authority (title) VALUES ('deleteProcess_clientAssignable');
INSERT INTO authority (title) VALUES ('addProcess_clientAssignable');

INSERT INTO authority (title) VALUES ('viewProcess_projectAssignable');
INSERT INTO authority (title) VALUES ('viewAllProcesses_projectAssignable');
INSERT INTO authority (title) VALUES ('editProcess_projectAssignable');
INSERT INTO authority (title) VALUES ('deleteProcess_projectAssignable');
INSERT INTO authority (title) VALUES ('addProcess_projectAssignable');

INSERT INTO authority (title) VALUES ('editProcessMetaData_clientAssignable');
INSERT INTO authority (title) VALUES ('editProcessStructureData_clientAssignable');
INSERT INTO authority (title) VALUES ('editProcessPagination_clientAssignable');
INSERT INTO authority (title) VALUES ('editProcessImages_clientAssignable');
INSERT INTO authority (title) VALUES ('viewProcessMetaData_clientAssignable');
INSERT INTO authority (title) VALUES ('viewProcessStructureData_clientAssignable');
INSERT INTO authority (title) VALUES ('viewProcessPagination_clientAssignable');
INSERT INTO authority (title) VALUES ('viewProcessImages_clientAssignable');

INSERT INTO authority (title) VALUES ('editProcessMetaData_projectAssignable');
INSERT INTO authority (title) VALUES ('editProcessStructureData_projectAssignable');
INSERT INTO authority (title) VALUES ('editProcessPagination_projectAssignable');
INSERT INTO authority (title) VALUES ('editProcessImages_projectAssignable');
INSERT INTO authority (title) VALUES ('viewProcessMetaData_projectAssignable');
INSERT INTO authority (title) VALUES ('viewProcessStructureData_projectAssignable');
INSERT INTO authority (title) VALUES ('viewProcessPagination_projectAssignable');
INSERT INTO authority (title) VALUES ('viewProcessImages_projectAssignable');

/* Task */
INSERT INTO authority (title) VALUES ('viewTask_clientAssignable');
INSERT INTO authority (title) VALUES ('viewAllTasks_clientAssignable');
INSERT INTO authority (title) VALUES ('editTask_clientAssignable');
INSERT INTO authority (title) VALUES ('deleteTask_clientAssignable');
INSERT INTO authority (title) VALUES ('addTask_clientAssignable');

INSERT INTO authority (title) VALUES ('viewTask_projectAssignable');
INSERT INTO authority (title) VALUES ('viewAllTasks_projectAssignable');
INSERT INTO authority (title) VALUES ('editTask_projectAssignable');
INSERT INTO authority (title) VALUES ('deleteTask_projectAssignable');
INSERT INTO authority (title) VALUES ('addTask_projectAssignable');

/* UserGroup */
INSERT INTO authority (title) VALUES ('viewUserGroup_clientAssignable');
INSERT INTO authority (title) VALUES ('viewAllUserGroups_clientAssignable');
INSERT INTO authority (title) VALUES ('editUserGroup_clientAssignable');
INSERT INTO authority (title) VALUES ('deleteUserGroup_clientAssignable');
INSERT INTO authority (title) VALUES ('addUserGroup_clientAssignable');

/* User */
INSERT INTO authority (title) VALUES ('viewUser_clientAssignable');
INSERT INTO authority (title) VALUES ('viewAllUsers_clientAssignable');
INSERT INTO authority (title) VALUES ('editUser_clientAssignable');
INSERT INTO authority (title) VALUES ('deleteUser_clientAssignable');
INSERT INTO authority (title) VALUES ('addUser_clientAssignable');

--changeset kitodo:65
INSERT INTO folder (fileGroup, urlStructure, mimeType, path, project_id, copyFolder, linkingMode)
  SELECT 'LOCAL'                     as fileGroup,
         ''                          as urlStructure,
         'image/tiff'                as mimeType,
         'images/(processtitle)_tif' as path,
         project.id                  as project_id,
         0                           as copyFolder,
         'NO'                        as linkingMode
  FROM project
  LEFT JOIN folder ON (folder.project_id = project.id AND folder.fileGroup = 'LOCAL')
  WHERE folder.id IS NULL;

INSERT INTO folder (fileGroup, urlStructure, mimeType, path, project_id, copyFolder, linkingMode)
  SELECT 'MAX'        as fileGroup,
         'http://www.example.com/content/$(meta.CatalogIDDigital)/jpgs/max/' as urlStructure,
         'image/jpeg' as mimeType,
         'jpgs/max'   as path,
         project.id   as project_id,
         0            as copyFolder,
         'NO'         as linkingMode
  FROM project
  LEFT JOIN folder ON (folder.project_id = project.id AND folder.fileGroup = 'MAX')
  WHERE folder.id IS NULL;

INSERT INTO folder (fileGroup, urlStructure, mimeType, path, project_id, copyFolder, linkingMode)
  SELECT 'THUMBS'      as fileGroup,
         'http://www.example.com/content/$(meta.CatalogIDDigital)/jpgs/thumbs/' as urlStructure,
         'image/jpeg'  as mimeType,
         'jpgs/thumbs' as path,
         project.id    as project_id,
         0             as copyFolder,
         'NO'          as linkingMode
  FROM project
  LEFT JOIN folder ON (folder.project_id = project.id AND folder.fileGroup = 'THUMBS')
  WHERE folder.id IS NULL;


/* Create columns (Folder) generatorSource, (Folder) mediaView, */
/*     (Folder) preview */

ALTER TABLE project
  ADD generatorSource_folder_id int(11) DEFAULT NULL
        COMMENT 'folder with templates to create derived media from',
  ADD CONSTRAINT CK_project_generatorSource_folder_id
        FOREIGN KEY (generatorSource_folder_id) REFERENCES folder (id),
  ADD mediaView_folder_id int(11) DEFAULT NULL
        COMMENT 'media to use for single medium view',
  ADD CONSTRAINT CK_project_mediaView_folder_id
        FOREIGN KEY (mediaView_folder_id) REFERENCES folder (id),
  ADD preview_folder_id int(11) DEFAULT NULL
        COMMENT 'media to use for gallery preview',
  ADD CONSTRAINT CK_project_preview_folder_id
        FOREIGN KEY (preview_folder_id) REFERENCES folder (id);


/* Set 'generatorSource' column to corresponding 'LOCAL' file group */
SET SQL_SAFE_UPDATES=0;
UPDATE project JOIN folder ON folder.project_id = project.id
  SET project.generatorSource_folder_id = folder.id
  WHERE project.id > 0 AND folder.fileGroup = 'LOCAL';


/* Set 'mediaView' column to corresponding 'MAX' file group */
UPDATE project JOIN folder ON folder.project_id = project.id
  SET project.mediaView_folder_id = folder.id
  WHERE project.id > 0 AND folder.fileGroup = 'MAX';


/* Set 'preview' column to corresponding 'THUMBS' file group */

UPDATE project JOIN folder ON folder.project_id = project.id
  SET project.preview_folder_id = folder.id
  WHERE project.id > 0 AND folder.fileGroup = 'THUMBS';
SET SQL_SAFE_UPDATES=1;

--changeset kitodo:66
/* Migration: Increasing the maximum length of column scriptPath in table task. */
/*            This allows longer paths and parameters to be saved. */
ALTER TABLE task MODIFY COLUMN scriptPath VARCHAR(500)

--changeset kitodo:67
/* Migration: Remove column outputName. */
ALTER TABLE process DROP COLUMN outputName;
ALTER TABLE template DROP COLUMN outputName;

--changeset kitodo:68
/* Migration: Add example workflow - equal in tasks list to template. */
INSERT INTO workflow (`title`, `fileName`, `active`, `ready`, `indexAction`)
VALUES ('Example_Workflow', 'Example_Workflow', 1, 1, 'INDEX');

--changeset kitodo:69
/* Migration: Remove column outputName. */
ALTER TABLE user DROP COLUMN css

--changeset kitodo:71
/* Add rights for workflow */
INSERT INTO authority (title) VALUES ('viewWorkflow_globalAssignable');
INSERT INTO authority (title) VALUES ('viewAllWorkflows_globalAssignable');
INSERT INTO authority (title) VALUES ('editWorkflow_globalAssignable');
INSERT INTO authority (title) VALUES ('deleteWorkflow_globalAssignable');

INSERT INTO authority (title) VALUES ('viewWorkflow_clientAssignable');
INSERT INTO authority (title) VALUES ('viewAllWorkflows_clientAssignable');
INSERT INTO authority (title) VALUES ('editWorkflow_clientAssignable');
INSERT INTO authority (title) VALUES ('deleteWorkflow_clientAssignable');

INSERT INTO authority (title) VALUES ('viewWorkflow_projectAssignable');
INSERT INTO authority (title) VALUES ('viewAllWorkflows_projectAssignable');
INSERT INTO authority (title) VALUES ('editWorkflow_projectAssignable');
INSERT INTO authority (title) VALUES ('deleteWorkflow_projectAssignable');

--changeset kitodo:72
/* Migration: Create table contentFolders_task_x_folder */
/* 1. Create table contentFolders_task_x_folder */

CREATE TABLE contentFolders_task_x_folder (
  task_id   int(11) NOT NULL
     COMMENT 'Task that triggers the generation of the contents of the folder',
  folder_id int(11) NOT NULL
     COMMENT 'Folder whose contents are to be generated in that task',
  PRIMARY KEY ( task_id, folder_id ),
  KEY FK_task_id   ( task_id ),
  KEY FK_folder_id ( folder_id ),
  CONSTRAINT FK_contentFolders_task_x_folder_task_id
    FOREIGN KEY ( task_id ) REFERENCES task ( id ),
  CONSTRAINT FK_contentFolders_task_x_folder_folder_id
    FOREIGN KEY ( folder_id ) REFERENCES folder ( id )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--changeset kitodo:73
/* Migration: Remove columns from task table. */
ALTER TABLE task DROP COLUMN typeImportFileUpload;
ALTER TABLE task DROP COLUMN typeExportRussian;

--changeset kitodo:74
/* Add missing authorities for entities */

/* 1. Add workflow */
INSERT INTO authority (title) VALUES ('addWorkflow_globalAssignable');
INSERT INTO authority (title) VALUES ('addWorkflow_clientAssignable');

/* 2. View all authorities */
INSERT INTO authority (title) VALUES ('viewAllAuthorities_globalAssignable');

/* 3. Add authorities for batch */
INSERT INTO authority (title) VALUES ('viewBatch_globalAssignable');
INSERT INTO authority (title) VALUES ('viewAllBatches_globalAssignable');
INSERT INTO authority (title) VALUES ('addBatch_globalAssignable');
INSERT INTO authority (title) VALUES ('editBatch_globalAssignable');
INSERT INTO authority (title) VALUES ('deleteBatch_globalAssignable');

INSERT INTO authority (title) VALUES ('viewBatch_clientAssignable');
INSERT INTO authority (title) VALUES ('viewAllBatches_clientAssignable');
INSERT INTO authority (title) VALUES ('addBatch_clientAssignable');
INSERT INTO authority (title) VALUES ('editBatch_clientAssignable');
INSERT INTO authority (title) VALUES ('deleteBatch_clientAssignable');

/* 4. Add authorities for template */
INSERT INTO authority (title) VALUES ('viewTemplate_globalAssignable');
INSERT INTO authority (title) VALUES ('viewAllTemplates_globalAssignable');
INSERT INTO authority (title) VALUES ('addTemplate_globalAssignable');
INSERT INTO authority (title) VALUES ('editTemplate_globalAssignable');
INSERT INTO authority (title) VALUES ('deleteTemplate_globalAssignable');

INSERT INTO authority (title) VALUES ('viewTemplate_clientAssignable');
INSERT INTO authority (title) VALUES ('viewAllTemplates_clientAssignable');
INSERT INTO authority (title) VALUES ('addTemplate_clientAssignable');
INSERT INTO authority (title) VALUES ('editTemplate_clientAssignable');
INSERT INTO authority (title) VALUES ('deleteTemplate_clientAssignable');

/* 5. Add authorities for workflow interactions */
INSERT INTO authority (title) VALUES ('performTask_globalAssignable');
INSERT INTO authority (title) VALUES ('assignTasks_globalAssignable');
INSERT INTO authority (title) VALUES ('overrideTasks_globalAssignable');
INSERT INTO authority (title) VALUES ('superviseTasks_globalAssignable');

INSERT INTO authority (title) VALUES ('performTask_clientAssignable');
INSERT INTO authority (title) VALUES ('assignTasks_clientAssignable');
INSERT INTO authority (title) VALUES ('overrideTasks_clientAssignable');
INSERT INTO authority (title) VALUES ('superviseTasks_clientAssignable');

--changeset kitodo:75
/* 1. Update the dummy LDAP group */
SET SQL_SAFE_UPDATES = 0;

UPDATE ldapGroup SET homeDirectory = '/usr/local/kitodo/users/{login}',
title = 'Local LDAP',
gidNumber = '100',
userDN = 'cn={login},ou=users,dc=nodomain',
objectClasses = 'top,inetOrgPerson,posixAccount,shadowAccount,sambaSamAccount',
sambaSID = 'S-1-5-21-1234567890-123456789-1234567890-{uidnumber*2+1001}',
sn = '{login}',
uid = '{login}',
description = 'Exemplary configuration of an LDAP group',
displayName = '{user full name}',
gecos = '{user full name}',
loginShell = '/bin/false',
sambaAcctFlags = '[U          ]',
sambaLogonScript = '_{login}.bat',
sambaPrimaryGroupSID = 'S-1-5-21-1234567890-123456789-1234567890-1000',
sambaPasswordMustChange = '2147483647',
sambaPasswordHistory = '00000000000000000000000000000000000000',
sambaLogonHours = 'FFFFFFFFFFFFFFFFFFFF',
sambaKickoffTime = '0'
WHERE title = 'test' AND homeDirectory IS NULL AND gidNumber IS NULL AND sambaSID IS NULL;

SET SQL_SAFE_UPDATES = 1;

--changeset kitodo:76
/* Migration: Create table validationFolders_task_x_folder */
/* 1. Create table validationFolders_task_x_folder */
CREATE TABLE validationFolders_task_x_folder (
  task_id   int(11) NOT NULL
     COMMENT 'Task that triggers the validation of the contents of the folder',
  folder_id int(11) NOT NULL
     COMMENT 'Folder whose contents are to be validated in that task',
  PRIMARY KEY ( task_id, folder_id ),
  KEY FK_task_id   ( task_id ),
  KEY FK_folder_id ( folder_id ),
  CONSTRAINT FK_validationFolders_task_x_folder_task_id
    FOREIGN KEY ( task_id ) REFERENCES task ( id ),
  CONSTRAINT FK_validationFolders_task_x_folder_folder_id
    FOREIGN KEY ( folder_id ) REFERENCES folder ( id )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--changeset kitodo:77
/* Migration: Remove relation between task and user group and add */
/* relation between user group and client. */
/* 1. Drop cross table for tasks and users */
DROP TABLE task_x_user;

/* 2. Add cross table user group and client */
CREATE TABLE userGroup_x_client (
  `userGroup_id` INT(11) NOT NULL,
  `client_id` INT(11) DEFAULT NULL)
  ENGINE=InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE utf8mb4_unicode_ci;

/* 3. Add foreign keys */
ALTER TABLE userGroup_x_client ADD CONSTRAINT `FK_userGroup_x_client_userGroup_id`
FOREIGN KEY (userGroup_id) REFERENCES userGroup(id);

ALTER TABLE userGroup_x_client ADD CONSTRAINT `FK_userGroup_x_client_client_id`
FOREIGN KEY (client_id) REFERENCES client(id);

/* 4. Copy all user groups to new table */
INSERT INTO userGroup_x_client (userGroup_id, client_id) SELECT id, null FROM userGroup;

/* 5. Switch off safe updates */
SET SQL_SAFE_UPDATES = 0;

/* 6. Assign the dummy client to every user group */
UPDATE userGroup_x_client, (SELECT * FROM client WHERE name = 'Client_ChangeMe') dummyClient
SET userGroup_x_client.client_id = dummyClient.id WHERE userGroup_x_client.client_id IS NULL;

/* 7. Switch on safe updates */
SET SQL_SAFE_UPDATES = 1;

--changeset kitodo:78
/* Remove project authorities */
/* 1. Drop foreign keys */
ALTER TABLE userGroup_x_authority
  DROP FOREIGN KEY FK_userGroup_x_authority_authority_id;
ALTER TABLE userGroup_x_authority
  DROP FOREIGN KEY FK_userGroup_x_authority_userGroup_id;

/* 2. Switch off safe updates */
SET SQL_SAFE_UPDATES = 0;

/* 3. Delete project authorities from cross table *7
DELETE FROM userGroup_x_authority WHERE authority_id IN (SELECT id FROM authority WHERE title LIKE '%_projectAssignable');

/* 4. Delete project assignable authorities */
DELETE FROM authority WHERE title LIKE '%_projectAssignable';

/* 5. Switch on safe updates */
SET SQL_SAFE_UPDATES = 1;

/* 6. Add foreign keys */
ALTER TABLE userGroup_x_authority
  ADD CONSTRAINT FK_userGroup_x_authority_authority_id
  FOREIGN KEY (authority_id)
  REFERENCES authority (id),
  ADD CONSTRAINT FK_userGroup_x_authority_userGroup_id
  FOREIGN KEY (userGroup_id)
  REFERENCES userGroup (id);

--changeset kitodo:79
/* Change relationship between user group and client. */
/* Add dummy client to dockets, rulesets and user groups without assigned client */
/* 1. Drop foreign keys */
ALTER TABLE userGroup_x_client
  DROP FOREIGN KEY FK_userGroup_x_client_client_id;
ALTER TABLE userGroup_x_client
  DROP FOREIGN KEY FK_userGroup_x_client_userGroup_id;

/* 2. Remove all data from userGroup_x_client table */
TRUNCATE TABLE userGroup_x_client;

/* 2. Add column for client id to userGroup table */
ALTER TABLE userGroup ADD client_id INT(11);

/* 4. Switch off safe updates */
SET SQL_SAFE_UPDATES = 0;

/* 5. Assign the client to every docket and ruleset which has no client */
UPDATE docket docketTable, (SELECT * FROM client WHERE name = 'Client_ChangeMe') dummyClient
SET docketTable.client_id = dummyClient.id WHERE docketTable.client_id IS NULL;

UPDATE ruleset rulesetTable, (SELECT * FROM client WHERE name = 'Client_ChangeMe') dummyClient
SET rulesetTable.client_id = dummyClient.id WHERE rulesetTable.client_id IS NULL;

UPDATE userGroup userGroupTable, (SELECT * FROM client WHERE name = 'Client_ChangeMe') dummyClient
SET userGroupTable.client_id = dummyClient.id WHERE userGroupTable.client_id IS NULL;

/* 6. Switch on safe updates */
SET SQL_SAFE_UPDATES = 1;

/* 7. Add foreign key to userGroup table */
ALTER TABLE userGroup ADD CONSTRAINT `FK_userGroup_client_id`
foreign key (client_id) REFERENCES client(id);

/* 8. Drop userGroup_x_client table */
DROP TABLE userGroup_x_client;

--changeset kitodo:80
/* Change user group and role. */
/* 1. Drop foreign keys*/
ALTER TABLE userGroup
  DROP FOREIGN KEY FK_userGroup_client_id;

ALTER TABLE userGroup_x_authority
  DROP FOREIGN KEY FK_userGroup_x_authority_userGroup_id,
  DROP FOREIGN KEY FK_userGroup_x_authority_authority_id;

ALTER TABLE task_x_userGroup
  DROP FOREIGN KEY FK_task_x_userGroup_task_id,
  DROP FOREIGN KEY FK_task_x_userGroup_userGroup_id;

ALTER TABLE user_x_userGroup
  DROP FOREIGN KEY FK_user_x_userGroup_user_id,
  DROP FOREIGN KEY FK_user_x_userGroup_userGroup_id;

/* 2. Rename userGroup tables */
ALTER TABLE userGroup RENAME TO role;

ALTER TABLE userGroup_x_authority
  CHANGE userGroup_id role_id INT(11) NOT NULL,
  RENAME TO role_x_authority;

ALTER TABLE task_x_userGroup
  CHANGE userGroup_id role_id INT(11) NOT NULL,
  RENAME TO task_x_role;

ALTER TABLE user_x_userGroup
  CHANGE userGroup_id role_id INT(11) NOT NULL,
  RENAME TO user_x_role;

/* 3. Add foreign key to role tables */
ALTER TABLE role ADD CONSTRAINT FK_role_client_id
  FOREIGN KEY (client_id) REFERENCES client (id);

ALTER TABLE role_x_authority
  ADD CONSTRAINT FK_role_x_authority_authority_id
  FOREIGN KEY (authority_id) REFERENCES authority (id),
  ADD CONSTRAINT FK_role_x_authority_role_id
  FOREIGN KEY (role_id) REFERENCES role (id);

ALTER TABLE task_x_role
  ADD CONSTRAINT FK_task_x_role_task_id
  FOREIGN KEY (task_id) REFERENCES task (id),
  ADD CONSTRAINT FK_task_x_role_role_id
  FOREIGN KEY (role_id) REFERENCES role (id);

ALTER TABLE user_x_role
  ADD CONSTRAINT FK_user_x_role_user_id
  FOREIGN KEY (user_id) REFERENCES user (id),
  ADD CONSTRAINT FK_user_x_role_role_id
  FOREIGN KEY (role_id) REFERENCES role (id);

/* 4. Update entries in authority table */
UPDATE authority SET title = 'viewAllRoles_globalAssignable'
WHERE title = 'viewAllUserGroups_globalAssignable';

UPDATE authority SET title = 'viewRole_globalAssignable'
WHERE title = 'viewUserGroup_globalAssignable';

UPDATE authority SET title = 'addRole_globalAssignable'
WHERE title = 'addUserGroup_globalAssignable';

UPDATE authority SET title = 'editRole_globalAssignable'
WHERE title = 'editUserGroup_globalAssignable';

UPDATE authority SET title = 'deleteRole_globalAssignable'
WHERE title = 'deleteUserGroup_globalAssignable';

UPDATE authority SET title = 'viewAllRoles_clientAssignable'
WHERE title = 'viewAllUserGroups_clientAssignable';

UPDATE authority SET title = 'viewRole_clientAssignable'
WHERE title = 'viewUserGroup_clientAssignable';

UPDATE authority SET title = 'addRole_clientAssignable'
WHERE title = 'addUserGroup_clientAssignable';

UPDATE authority SET title = 'editRole_clientAssignable'
WHERE title = 'editUserGroup_clientAssignable';

UPDATE authority SET title = 'deleteRole_clientAssignable'
WHERE title = 'deleteUserGroup_clientAssignable';

--changeset kitodo:82
/* Add relationship between workflow and client and add dummy client to workflows.*/
/* 1. Add column for client id to workflow table */
ALTER TABLE workflow ADD client_id INT(11);

/* 2. Switch off safe updates */
SET SQL_SAFE_UPDATES = 0;

/* 3. Assign the client to every workflow which has no client */
UPDATE workflow workflowTable, (SELECT * FROM client WHERE name = 'Client_ChangeMe') dummyClient
SET workflowTable.client_id = dummyClient.id WHERE workflowTable.client_id IS NULL;

/* 4. Switch on safe updates */
SET SQL_SAFE_UPDATES = 1;

/* 5. Add foreign key to workflow table */
ALTER TABLE workflow ADD CONSTRAINT `FK_workflow_client_id`
foreign key (client_id) REFERENCES client(id);

--changeset kitodo:83
/* Change user group and role. */
/* 1. Switch off safe updates */
SET SQL_SAFE_UPDATES = 0;

/* 2. Delete admin authority from cross table */
DELETE FROM role_x_authority WHERE authority_id = (SELECT id FROM authority WHERE title = 'admin_globalAssignable');

/* 3. Drop admin authority /*
DELETE FROM authority WHERE title = 'admin_globalAssignable';

/* 4. Delete global authorities which shouldn't be global from cross table */
DELETE FROM role_x_authority WHERE authority_id = (SELECT id FROM authority WHERE title = 'viewAllProjects_globalAssignable');
DELETE FROM role_x_authority WHERE authority_id IN (SELECT id FROM authority WHERE title LIKE '%Project_globalAssignable');

DELETE FROM role_x_authority WHERE authority_id = (SELECT id FROM authority WHERE title = 'viewAllTemplates_globalAssignable');
DELETE FROM role_x_authority WHERE authority_id IN (SELECT id FROM authority WHERE title LIKE '%Template_globalAssignable');

DELETE FROM role_x_authority WHERE authority_id = (SELECT id FROM authority WHERE title = 'viewAllWorkflows_globalAssignable');
DELETE FROM role_x_authority WHERE authority_id IN (SELECT id FROM authority WHERE title LIKE '%Workflow_globalAssignable');

DELETE FROM role_x_authority WHERE authority_id = (SELECT id FROM authority WHERE title = 'viewAllDockets_globalAssignable');
DELETE FROM role_x_authority WHERE authority_id IN (SELECT id FROM authority WHERE title LIKE '%Docket_globalAssignable');

DELETE FROM role_x_authority WHERE authority_id = (SELECT id FROM authority WHERE title = 'viewAllRulesets_globalAssignable');
DELETE FROM role_x_authority WHERE authority_id IN (SELECT id FROM authority WHERE title LIKE '%Ruleset_globalAssignable');

DELETE FROM role_x_authority WHERE authority_id = (SELECT id FROM authority WHERE title = 'viewAllProcesses_globalAssignable');
DELETE FROM role_x_authority WHERE authority_id IN (SELECT id FROM authority WHERE title LIKE '%Process_globalAssignable');

DELETE FROM role_x_authority WHERE authority_id = (SELECT id FROM authority WHERE title = 'viewAllBatches_globalAssignable');
DELETE FROM role_x_authority WHERE authority_id IN (SELECT id FROM authority WHERE title LIKE '%Batch_globalAssignable');

/* 5. Drop global authorities which should not be global */
DELETE FROM authority WHERE title = 'viewAllProjects_globalAssignable';
DELETE FROM authority WHERE title LIKE '%Project_globalAssignable';

DELETE FROM authority WHERE title = 'viewAllTemplates_globalAssignable';
DELETE FROM authority WHERE title LIKE '%Template_globalAssignable';

DELETE FROM authority WHERE title = 'viewAllWorkflows_globalAssignable';
DELETE FROM authority WHERE title LIKE '%Workflow_globalAssignable';

DELETE FROM authority WHERE title = 'viewAllDockets_globalAssignable';
DELETE FROM authority WHERE title LIKE '%Docket_globalAssignable';

DELETE FROM authority WHERE title = 'viewAllRulesets_globalAssignable';
DELETE FROM authority WHERE title LIKE '%Ruleset_globalAssignable';

DELETE FROM authority WHERE title = 'viewAllProcesses_globalAssignable';
DELETE FROM authority WHERE title LIKE '%Process_globalAssignable';

DELETE FROM authority WHERE title = 'viewAllBatches_globalAssignable';
DELETE FROM authority WHERE title LIKE '%Batch_globalAssignable';

/* 6. Switch on safe updates */
SET SQL_SAFE_UPDATES = 1;

/* 7. Add initial roles if they do not exist yet */
INSERT IGNORE INTO role (title, client_id) VALUES ('AdminGroup', 1);
INSERT IGNORE INTO role (title, client_id) VALUES ('CheckDossierGroup', 1);
INSERT IGNORE INTO role (title, client_id) VALUES ('PrepareDossierGroup', 1);
INSERT IGNORE INTO role (title, client_id) VALUES ('ScanDossierAGroup', 1);
INSERT IGNORE INTO role (title, client_id) VALUES ('ScanDossierBGroup', 1);
INSERT IGNORE INTO role (title, client_id) VALUES ('CheckQualityGroup', 1);

/* 8. Add initial authorities to roles */
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  SELECT (SELECT id FROM role WHERE title = 'AdminGroup'), id FROM authority WHERE title LIKE '%';

INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'CheckDossierGroup'), (SELECT id FROM authority WHERE title = 'viewAllTasks_clientAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'CheckDossierGroup'), (SELECT id FROM authority WHERE title = 'viewTask_clientAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  SELECT (SELECT id FROM role WHERE title = 'CheckDossierGroup'), id FROM authority WHERE title LIKE '%ProcessMetaData_clientAssignable';
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  SELECT (SELECT id FROM role WHERE title = 'CheckDossierGroup'), id FROM authority WHERE title LIKE '%ProcessStructureData_clientAssignable';

INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'PrepareDossierGroup'), (SELECT id FROM authority WHERE title = 'viewAllTasks_clientAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'PrepareDossierGroup'), (SELECT id FROM authority WHERE title = 'viewTask_clientAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  SELECT (SELECT id FROM role WHERE title = 'PrepareDossierGroup'), id FROM authority WHERE title LIKE '%ProcessMetaData_clientAssignable';
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  SELECT (SELECT id FROM role WHERE title = 'PrepareDossierGroup'), id FROM authority WHERE title LIKE '%ProcessStructureData_clientAssignable';
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  SELECT (SELECT id FROM role WHERE title = 'PrepareDossierGroup'), id FROM authority WHERE title LIKE '%ProcessPagination_clientAssignable';

INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'ScanDossierAGroup'), (SELECT id FROM authority WHERE title = 'viewAllTasks_clientAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'ScanDossierAGroup'), (SELECT id FROM authority WHERE title = 'viewTask_clientAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  SELECT (SELECT id FROM role WHERE title = 'ScanDossierAGroup'), id FROM authority WHERE title LIKE '%ProcessImages_clientAssignable';

INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'ScanDossierBGroup'), (SELECT id FROM authority WHERE title = 'viewAllTasks_clientAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'ScanDossierBGroup'), (SELECT id FROM authority WHERE title = 'viewTask_clientAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  SELECT (SELECT id FROM role WHERE title = 'ScanDossierBGroup'), id FROM authority WHERE title LIKE '%ProcessImages_clientAssignable';

INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'CheckQualityGroup'), (SELECT id FROM authority WHERE title = 'viewAllTasks_clientAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'CheckQualityGroup'), (SELECT id FROM authority WHERE title = 'viewTask_clientAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  SELECT (SELECT id FROM role WHERE title = 'CheckQualityGroup'), id FROM authority WHERE title LIKE '%ProcessMetaData_clientAssignable';
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  SELECT (SELECT id FROM role WHERE title = 'CheckQualityGroup'), id FROM authority WHERE title LIKE '%ProcessStructureData_clientAssignable';
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  SELECT (SELECT id FROM role WHERE title = 'CheckQualityGroup'), id FROM authority WHERE title LIKE '%ProcessPagination_clientAssignable';
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'CheckQualityGroup'), (SELECT id FROM authority WHERE title = 'viewProcessImages_clientAssignable'));

--changeset kitodo:84
/* Convert the char set to uft8mb4 on all lately inserted tables and their columns. */
/* This does not change the global setting for database. */
ALTER TABLE project_x_template CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

--changeset kitodo:85
/* Add columns for parallel tasks to task table. */
ALTER TABLE task ADD concurrent TINYINT(1);
ALTER TABLE task ADD last TINYINT(1);

--changeset kitodo:86
/* Update values of columns for parallel tasks. */
/* 1. Switch off safe updates */
SET SQL_SAFE_UPDATES = 0;

/* 2. Update columns */
UPDATE task SET concurrent = 1 WHERE concurrent IS NULL;
UPDATE task SET last = 0 WHERE last IS NULL;

/* 3. Switch on safe updates */
SET SQL_SAFE_UPDATES = 1;

--changeset kitodo:87
/* Migration: Remove column wikiField from template table. */
ALTER TABLE template DROP COLUMN wikiField;

--changeset kitodo:88
/* Migration: Remove column indexAction from client, user and role tables. */
ALTER TABLE client DROP COLUMN indexAction;
ALTER TABLE role DROP COLUMN indexAction;
ALTER TABLE user DROP COLUMN indexAction;

--changeset kitodo:89
/* Migration: Remove relation between task and user group and add */
/* relation between user group and client. */

/* 1. Add workflowCondition table */
CREATE TABLE workflowCondition (
  id INT(11) NOT NULL  AUTO_INCREMENT,
  type VARCHAR(50) DEFAULT NULL,
  value VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
  ) ENGINE=InnoDB DEFAULT CHARACTER SET = utf8mb4 COLLATE utf8mb4_unicode_ci;

/* 2. Adjust column for store workflow condition */
ALTER TABLE task CHANGE workflowCondition workflowCondition_id INT(11);

/* 3. Add foreign keys */
ALTER TABLE task ADD CONSTRAINT `FK_task_workflowCondition_workflowCondition_id`
FOREIGN KEY (workflowCondition_id) REFERENCES workflowCondition(id);

--changeset kitodo:90
/* Add column configuration authorities */
INSERT INTO authority (title) VALUES ('configureColumns_globalAssignable');
INSERT INTO authority (title) VALUES ('configureColumns_clientAssignable');

/* Add role for new authority to default Client ('Client_ChangeMe') */
INSERT INTO role (title, client_id) VALUES ('ConfigureColumns', 1);

/* Add column configuration authorities to corresponding role */
INSERT INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id from role WHERE title = 'ConfigureColumns'), (SELECT id FROM  authority WHERE title = 'configureColumns_globalAssignable'));
INSERT INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id from role WHERE title = 'ConfigureColumns'), (SELECT id FROM  authority WHERE title = 'configureColumns_clientAssignable'));

/* Add columns table */
/* 1. Create listColumn table */
CREATE TABLE listColumn (
  id INT(11) NOT NULL AUTO_INCREMENT,
  title VARCHAR (255) DEFAULT NULL,
  custom TINYINT(1) DEFAULT 0,
  PRIMARY KEY (`id`)
) DEFAULT CHARACTER SET = utf8mb4 COLLATE utf8mb4_unicode_ci;

/* 2. Add standard columns */
/* projects page */
/* project columns */
INSERT INTO listColumn (title) VALUES ('project.title');
INSERT INTO listColumn (title) VALUES ('project.metsRightsOwner');
INSERT INTO listColumn (title) VALUES ('project.active');
/* template columns */
INSERT INTO listColumn (title) VALUES ('template.title');
INSERT INTO listColumn (title) VALUES ('template.ruleset');
/* workflow columns */
INSERT INTO listColumn (title) VALUES ('workflow.title');
INSERT INTO listColumn (title) VALUES ('workflow.filename');
INSERT INTO listColumn (title) VALUES ('workflow.active');
INSERT INTO listColumn (title) VALUES ('workflow.ready');
/* docket columns */
INSERT INTO listColumn (title) VALUES ('docket.title');
INSERT INTO listColumn (title) VALUES ('docket.filename');
/* ruleset columns */
INSERT INTO listColumn (title) VALUES ('ruleset.title');
INSERT INTO listColumn (title) VALUES ('ruleset.filename');
INSERT INTO listColumn (title) VALUES ('ruleset.sorting');

/* tasks page */
/* task columns */
INSERT INTO listColumn (title) VALUES ('task.title');
INSERT INTO listColumn (title) VALUES ('task.process');
INSERT INTO listColumn (title) VALUES ('task.project');
INSERT INTO listColumn (title) VALUES ('task.state');

/* processes pages */
/* process columns */
INSERT INTO listColumn (title) VALUES ('process.title');
INSERT INTO listColumn (title) VALUES ('process.state');
INSERT INTO listColumn (title) VALUES ('process.project');

/* user page */
/* user columns */
INSERT INTO listColumn (title) VALUES ('user.username');
INSERT INTO listColumn (title) VALUES ('user.location');
INSERT INTO listColumn (title) VALUES ('user.roles');
INSERT INTO listColumn (title) VALUES ('user.clients');
INSERT INTO listColumn (title) VALUES ('user.projects');
INSERT INTO listColumn (title) VALUES ('user.active');
/* role columns */
INSERT INTO listColumn (title) VALUES ('role.role');
INSERT INTO listColumn (title) VALUES ('role.client');
/* client columns */
INSERT INTO listColumn (title) VALUES ('client.client');
/* ldap columns */
INSERT INTO listColumn (title) VALUES ('ldapgroup.ldapgroup');
INSERT INTO listColumn (title) VALUES ('ldapgroup.home_directory');
INSERT INTO listColumn (title) VALUES ('ldapgroup.gidNumber');

/* 3. Create client_x_listcolumn table */
CREATE TABLE client_x_listColumn (
  client_id INT(11) NOT NULL,
  column_id INT(11) NOT NULL,
  PRIMARY KEY ( client_id, column_id ),
  KEY FK_client_x_listColumn_client_id (client_id),
  KEY FK_client_x_listColumn_column_id (column_id),
  CONSTRAINT FK_client_x_listColumn_client_id FOREIGN KEY (client_id) REFERENCES client(id),
  CONSTRAINT FK_client_x_listColumn_column_id FOREIGN KEY (column_id) REFERENCES listColumn(id)
);

/* 4. Add standard mappings */
INSERT INTO client_x_listColumn (client_id, column_id)
  SELECT client.id, listColumn.id FROM client CROSS JOIN listColumn;

--changeset kitodo:91
/* Migration: Remove column fileName from workflow table, indexAction from authority table and make title column unique. */
ALTER TABLE workflow DROP COLUMN fileName;
ALTER TABLE authority DROP COLUMN indexAction;
ALTER TABLE workflow ADD UNIQUE (title);

--changeset kitodo:93
/* Migration: Replace columns active and ready on status in workflow table. */
/* 1. Add status column */
ALTER TABLE workflow ADD COLUMN status VARCHAR(15) AFTER title;

/* 2. Update values in status column according to active and ready */
SET SQL_SAFE_UPDATES = 0;

UPDATE workflow SET status = 'DRAFT'
  WHERE active = 1 AND ready = 0;

UPDATE workflow SET status = 'ACTIVE'
  WHERE active = 1 AND ready = 1;

UPDATE workflow SET status = 'ARCHIVED'
WHERE active = 0 AND ready = 1;

/* 3. Drop active and ready columns */
ALTER TABLE workflow DROP COLUMN active;
ALTER TABLE workflow DROP COLUMN ready;

/* 4. Update column in listColumn table */
UPDATE listColumn SET title = 'workflow.status'
  WHERE title = 'workflow.active';

/* 5. Drop foreign keys */
ALTER TABLE client_x_listColumn
  DROP FOREIGN KEY FK_client_x_listColumn_client_id;
ALTER TABLE client_x_listColumn
  DROP FOREIGN KEY FK_client_x_listColumn_column_id;

/* 6. Delete not needed column */
DELETE FROM client_x_listColumn WHERE column_id = (SELECT id FROM listColumn WHERE title = 'workflow.ready');

DELETE FROM listColumn WHERE title = 'workflow.ready';

SET SQL_SAFE_UPDATES = 1;

/* 7. Restore foreign keys */
ALTER TABLE client_x_listColumn
  ADD CONSTRAINT FK_client_x_listColumn_client_id
    FOREIGN KEY (client_id) REFERENCES client(id),
  ADD CONSTRAINT FK_client_x_listColumn_column_id
    FOREIGN KEY (column_id) REFERENCES listColumn(id);

--changeset kitodo:94
-- Update roles and authorities.

-- 1. Add initial roles if they don not exists yet
--
INSERT IGNORE INTO role (title, client_id) VALUES ('WorkflowManagement', 1);

-- 2. Add initial authorities to roles
--
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'WorkflowManagement'), (SELECT id FROM authority WHERE title = 'viewAllWorkflows_clientAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  SELECT (SELECT id FROM role WHERE title = 'WorkflowManagement'), id FROM authority WHERE title LIKE '%Workflow_clientAssignable';
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'WorkflowManagement'), (SELECT id FROM authority WHERE title = 'viewAllDockets_clientAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  SELECT (SELECT id FROM role WHERE title = 'WorkflowManagement'), id FROM authority WHERE title LIKE '%Docket_clientAssignable';
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'WorkflowManagement'), (SELECT id FROM authority WHERE title = 'viewAllRulesets_clientAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  SELECT (SELECT id FROM role WHERE title = 'WorkflowManagement'), id FROM authority WHERE title LIKE '%Ruleset_clientAssignable';
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'WorkflowManagement'), (SELECT id FROM authority WHERE title = 'viewAllTemplates_clientAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  SELECT (SELECT id FROM role WHERE title = 'WorkflowManagement'), id FROM authority WHERE title LIKE '%Template_clientAssignable';

INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'ProcessManagement'), (SELECT id FROM authority WHERE title = 'viewAllProjects_clientAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'ProcessManagement'), (SELECT id FROM authority WHERE title = 'viewAllProcesses_clientAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  SELECT (SELECT id FROM role WHERE title = 'ProcessManagement'), id FROM authority WHERE title LIKE '%Process_clientAssignable';


-- 3. Add workflow management to user which is assigned to project management
--
INSERT IGNORE  INTO user_x_role (user_id, role_id)
  SELECT (SELECT user_id FROM user_x_role WHERE role_id = (SELECT id FROM role WHERE title = 'Projectmanagement')),
  id FROM role WHERE title = 'WorkflowManagement';

-- 4. Delete not needed authorities from process management

DELETE FROM role_x_authority
  WHERE role_id = (SELECT id FROM role WHERE title = 'ProcessManagement')
  AND authority_id = (SELECT id FROM authority WHERE title = 'viewAllWorkflows_clientAssignable');
DELETE FROM role_x_authority
  WHERE role_id = (SELECT id FROM role WHERE title = 'ProcessManagement')
  AND authority_id IN (SELECT id FROM authority WHERE title LIKE '%Workflow_clientAssignable');
DELETE FROM role_x_authority
  WHERE role_id = (SELECT id FROM role WHERE title = 'ProcessManagement')
  AND authority_id = (SELECT id FROM authority WHERE title = 'viewAllDockets_clientAssignable');
DELETE FROM role_x_authority
  WHERE role_id = (SELECT id FROM role WHERE title = 'ProcessManagement')
  AND authority_id IN (SELECT id FROM authority WHERE title LIKE '%Docket_clientAssignable');
DELETE FROM role_x_authority
  WHERE role_id = (SELECT id FROM role WHERE title = 'ProcessManagement')
  AND authority_id = (SELECT id FROM authority WHERE title = 'viewAllRulesets_clientAssignable');
DELETE FROM role_x_authority
  WHERE role_id = (SELECT id FROM role WHERE title = 'ProcessManagement')
  AND authority_id IN (SELECT id FROM authority WHERE title LIKE '%Ruleset_clientAssignable');
DELETE FROM role_x_authority
  WHERE role_id = (SELECT id FROM role WHERE title = 'ProcessManagement')
  AND authority_id IN (SELECT id FROM authority WHERE title LIKE '%Template_clientAssignable');


--changeset kitodo:95
/* Update roles and authorities. */
/* 1. Add initial roles if they don not exists yet */
INSERT IGNORE INTO role (title, client_id) VALUES ('WorkflowManagement', 1);

/* 2. Add initial authorities to roles */
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'WorkflowManagement'), (SELECT id FROM authority WHERE title = 'viewAllWorkflows_clientAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  SELECT (SELECT id FROM role WHERE title = 'WorkflowManagement'), id FROM authority WHERE title LIKE '%Workflow_clientAssignable';
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'WorkflowManagement'), (SELECT id FROM authority WHERE title = 'viewAllDockets_clientAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  SELECT (SELECT id FROM role WHERE title = 'WorkflowManagement'), id FROM authority WHERE title LIKE '%Docket_clientAssignable';
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'WorkflowManagement'), (SELECT id FROM authority WHERE title = 'viewAllRulesets_clientAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  SELECT (SELECT id FROM role WHERE title = 'WorkflowManagement'), id FROM authority WHERE title LIKE '%Ruleset_clientAssignable';
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'WorkflowManagement'), (SELECT id FROM authority WHERE title = 'viewAllTemplates_clientAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  SELECT (SELECT id FROM role WHERE title = 'WorkflowManagement'), id FROM authority WHERE title LIKE '%Template_clientAssignable';

INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'ProcessManagement'), (SELECT id FROM authority WHERE title = 'viewAllProjects_clientAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'ProcessManagement'), (SELECT id FROM authority WHERE title = 'viewAllProcesses_clientAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  SELECT (SELECT id FROM role WHERE title = 'ProcessManagement'), id FROM authority WHERE title LIKE '%Process_clientAssignable';


/* 3. Add workflow management to user which is assigned to project management */
INSERT IGNORE  INTO user_x_role (user_id, role_id)
  SELECT (SELECT user_id FROM user_x_role WHERE role_id = (SELECT id FROM role WHERE title = 'Projectmanagement')),
  id FROM role WHERE title = 'WorkflowManagement';

/* 4. Delete not needed authorities from process management */
DELETE FROM role_x_authority
  WHERE role_id = (SELECT id FROM role WHERE title = 'ProcessManagement')
  AND authority_id = (SELECT id FROM authority WHERE title = 'viewAllWorkflows_clientAssignable');
DELETE FROM role_x_authority
  WHERE role_id = (SELECT id FROM role WHERE title = 'ProcessManagement')
  AND authority_id IN (SELECT id FROM authority WHERE title LIKE '%Workflow_clientAssignable');
DELETE FROM role_x_authority
  WHERE role_id = (SELECT id FROM role WHERE title = 'ProcessManagement')
  AND authority_id = (SELECT id FROM authority WHERE title = 'viewAllDockets_clientAssignable');
DELETE FROM role_x_authority
  WHERE role_id = (SELECT id FROM role WHERE title = 'ProcessManagement')
  AND authority_id IN (SELECT id FROM authority WHERE title LIKE '%Docket_clientAssignable');
DELETE FROM role_x_authority
  WHERE role_id = (SELECT id FROM role WHERE title = 'ProcessManagement')
  AND authority_id = (SELECT id FROM authority WHERE title = 'viewAllRulesets_clientAssignable');
DELETE FROM role_x_authority
  WHERE role_id = (SELECT id FROM role WHERE title = 'ProcessManagement')
  AND authority_id IN (SELECT id FROM authority WHERE title LIKE '%Ruleset_clientAssignable');
DELETE FROM role_x_authority
  WHERE role_id = (SELECT id FROM role WHERE title = 'ProcessManagement')
  AND authority_id IN (SELECT id FROM authority WHERE title LIKE '%Template_clientAssignable');

--changeset kitodo:96
/* Add relationship between template and client and add dummy client to templates. */
/* 1. Add column for client id to template table */
ALTER TABLE template ADD client_id INT(11);

/* 2. Switch off safe updates */
SET SQL_SAFE_UPDATES = 0;

/* 3. Assign the client to every template which has no client */
UPDATE template
SET template.client_id = 1 WHERE template.client_id IS NULL;

/* 4. Switch on safe updates */
SET SQL_SAFE_UPDATES = 1;

/* 5. Add foreign key to template table */
ALTER TABLE template ADD CONSTRAINT `FK_template_client_id`
foreign key (client_id) REFERENCES client(id);

--changeset kitodo:97
/* Insert authorities for authorities management. */
INSERT IGNORE INTO authority (title) VALUES ('addAuthority_globalAssignable');
INSERT IGNORE INTO authority (title) VALUES ('editAuthority_globalAssignable');
INSERT IGNORE INTO authority (title) VALUES ('deleteAuthority_globalAssignable');
INSERT IGNORE INTO authority (title) VALUES ('viewAuthority_globalAssignable');
INSERT IGNORE INTO authority (title) VALUES ('viewAllAuthorities_globalAssignable');

INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  VALUES ((SELECT id FROM role WHERE title = 'Administration'), (SELECT id FROM authority WHERE title = 'viewAllAuthorities_globalAssignable'));
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
  SELECT (SELECT id FROM role WHERE title = 'Administration'), id FROM authority WHERE title LIKE '%Authority_globalAssignable';

--changeset kitodo:98
/* Add column repeatOnCorrection to task table. */
ALTER TABLE task ADD repeatOnCorrection TINYINT(1) DEFAULT 0;

--changeset kitodo:99
/* Migration: Create table comment */
/* 1. Create table comment-- Add columns table */
CREATE TABLE comment
(
  id                int(11) NOT NULL AUTO_INCREMENT,
  message           varchar(255) DEFAULT NULL,
  type              varchar(9)   DEFAULT NULL,
  isCorrected       tinyint(1)   DEFAULT NULL,
  creationDate      datetime     DEFAULT NULL,
  correctionDate    datetime     DEFAULT NULL,
  user_id           int(11)      DEFAULT NULL,
  currentTask_id    int(11)      DEFAULT NULL,
  correctionTask_id int(11)      DEFAULT NULL,
  process_id        int(11)      DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY FK_user_id (user_id),
  KEY FK_currentTask_id (currentTask_id),
  KEY FK_correctionTask_id (correctionTask_id),
  KEY FK_process_id (process_id),
  CONSTRAINT FK_comment_user_id
    FOREIGN KEY (user_id) REFERENCES user (id),
  CONSTRAINT FK_comment_currentTask_id
    FOREIGN KEY (currentTask_id) REFERENCES task (id),
  CONSTRAINT FK_comment_correctionTask_id
    FOREIGN KEY (correctionTask_id) REFERENCES task (id),
  CONSTRAINT FK_comment_process_id
    FOREIGN KEY (process_id) REFERENCES process (id)
) DEFAULT CHARACTER SET = utf8mb4
  COLLATE utf8mb4_unicode_ci;

--changeset kitodo:100
/* Insert authority for process export. */
INSERT IGNORE INTO authority (title) VALUES ('exportProcess_clientAssignable');

--changeset kitodo:101
INSERT INTO listColumn (title) VALUES ('task.priority');
INSERT INTO listColumn (title) VALUES ('task.duration');
INSERT INTO listColumn (title) VALUES ('task.correctionComment');
INSERT INTO listColumn (title) VALUES ('process.duration')

--changeset kitodo:102
/* 1. Add columns for parent process id  and ordering to process table */
ALTER TABLE process ADD parent_id INT(11);
ALTER TABLE process ADD ordering INT(6);

/* 2. Add foreign key to process table */
ALTER TABLE process ADD CONSTRAINT `FK_process_parent_id`
    FOREIGN KEY (parent_id) REFERENCES process(id)

--changeset kitodo:103
/* Migration: Increase length of 'message' column for 'comment' table to allow longer messages. */
ALTER TABLE comment
CHANGE message message LONGTEXT;

--changeset kitodo:105
/* 1. Add column for correction to task table */
ALTER TABLE task ADD correction TINYINT(1);

/* 2. Switch off safe updates */
SET SQL_SAFE_UPDATES = 0;

/* 3. Update correction column with data from priority column */
UPDATE task SET correction = 1 WHERE priority = 10;
UPDATE task SET correction = 0 WHERE priority <> 10;

/* 4. Switch on safe updates */
SET SQL_SAFE_UPDATES = 1;

/* 5. Drop priority column from task table */
ALTER TABLE task DROP priority;

--changeset kitodo:106
ALTER TABLE role DROP INDEX title;

--changeset kitodo:107
/* Remove obsolete project fields */
ALTER TABLE project DROP COLUMN metsDigiprovReferenceAnchor;
ALTER TABLE project DROP COLUMN metsDigiprovPresentationAnchor;
ALTER TABLE project DROP COLUMN metsPointerPathAnchor;

ALTER TABLE project DROP COLUMN dmsImportImagesPath;
ALTER TABLE project DROP COLUMN dmsImportSuccessPath;
ALTER TABLE project DROP COLUMN dmsImportErrorPath;

--changeset kitodo:108
ALTER TABLE project DROP INDEX title;

--changeset kitodo:109
/* 1. Add column for separateStructure to task table */
ALTER TABLE task ADD separateStructure TINYINT(1);

/* 2. Switch off safe updates */
SET SQL_SAFE_UPDATES = 0;

/* 3. Set default value for this column */
UPDATE task SET separateStructure = 0 WHERE id IS NOT NULL;

/* 4. Switch on safe updates */
SET SQL_SAFE_UPDATES = 1;

--changeset kitodo:110
/* 1. Add columns for generate and validate image to task table */
ALTER TABLE task
    ADD COLUMN typeGenerateImages TINYINT(1),
    ADD COLUMN typeValidateImages TINYINT(1);

/* 2. Switch off safe updates */
SET SQL_SAFE_UPDATES = 0;

/* 3. Set default value for this columns */
UPDATE task SET typeGenerateImages = 0, typeValidateImages = 0 WHERE id IS NOT NULL;

/* 4. Switch on safe updates */
SET SQL_SAFE_UPDATES = 1;

--changeset kitodo:111
/* 1. Add column for validateFolder to folder table */
ALTER TABLE folder ADD validateFolder TINYINT(1) NOT NULL DEFAULT 1;

--changeset kitodo:112
/* Migration: Delete tables contentFolders_task_x_folder and */
/*            validationFolders_task_x_folder */
/* 1. Delete tables contentFolders_task_x_folder and */
/*    validationFolders_task_x_folder */
DROP TABLE contentFolders_task_x_folder,
           validationFolders_task_x_folde

--changeset kitodo:113
/* Add authority and role to view database statistics */
INSERT IGNORE INTO authority (title) VALUES ('viewDatabaseStatistic_globalAssignable');
INSERT IGNORE INTO role (title, client_id) VALUES ('DatabaseStatistic', 1);
INSERT IGNORE INTO role_x_authority (role_id, authority_id) VALUES ((SELECT id from role WHERE title = 'DatabaseStatistic'), (SELECT id FROM authority WHERE title = 'viewDatabaseStatistic_globalAssignable'));

--changeset kitodo:114
ALTER TABLE project DROP COLUMN dmsImportTimeOut;

--changeset kitodo:116
/* 1. Add new authorities to cancel jobs */
INSERT INTO authority (title) VALUES ('cancelJob_globalAssignable');

SET SQL_SAFE_UPDATES=0;

/* 2. Set 'separateStructure=true' for all tasks */
UPDATE task SET separateStructure = 1 WHERE TRUE;

/* 3. Set 'repeatOnCorrection=true' for all tasks */
UPDATE task SET repeatOnCorrection = 1 WHERE TRUE;

SET SQL_SAFE_UPDATES=1;

--changeset kitodo:117
/* 1. set templateIds to null */
/* 2. delete template Tasks from task_x_role */
/* 3. delete template Tasks */
/* 4. truncate template */

SET SQL_SAFE_UPDATES = 0;
UPDATE process SET template_id = NULL;
DELETE FROM task_x_role WHERE task_id IN (SELECT id FROM task WHERE template_id is not NULL);
DELETE FROM task WHERE template_id is not NULL;
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE template;
TRUNCATE project_x_template;
SET FOREIGN_KEY_CHECKS = 1;
SET SQL_SAFE_UPDATES = 1;

--changeset kitodo:118
ALTER TABLE process ADD COLUMN exported TINYINT(1) DEFAULT 0;

--changeset kitodo:119
ALTER TABLE project DROP COLUMN useDmsImport,
                    DROP COLUMN dmsImportCreateProcessFolder;

--changeset kitodo:120
UPDATE process SET processBaseUri = NULL WHERE id > 0;
ALTER TABLE process MODIFY COLUMN processBaseUri varchar(255);

--changeset kitodo:121
INSERT INTO listColumn (title) VALUES ('process.correctionMessage');
INSERT INTO listColumn (title) VALUES ('task.correctionMessage');

--changeset kitodo:122
INSERT INTO listColumn (title) VALUES ('task.processId');

--changeset kitodo:123
INSERT INTO authority (title) VALUES ('seeCorrectionTask_globalAssignable')

--changeset kitodo:124
ALTER TABLE project
    DROP COLUMN fileFormatInternal,
    DROP COLUMN fileFormatDmsExport;

--changeset kitodo:125
INSERT INTO listColumn (title) VALUES ('process.lastEditingUser');
INSERT INTO listColumn (title) VALUES ('process.processingBeginLastTask');
INSERT INTO listColumn (title) VALUES ('process.processingEndLastTask');
INSERT INTO listColumn (title) VALUES ('task.lastEditingUser');
INSERT INTO listColumn (title) VALUES ('task.processingBegin');
INSERT INTO listColumn (title) VALUES ('task.processingEnd');

--changeset kitodo:126
ALTER TABLE user ADD COLUMN shortcuts VARCHAR(1024) DEFAULT '{"detailView":"Control Shift BracketRight","help":"Shift Minus","nextItem":"Control ArrowDown","nextItemMulti":"Control Shift ArrowDown","previousItem":"Control ArrowUp","previousItemMulti":"Control Shift ArrowUp","structuredView":"Control Shift Slash"}';

--changeset kitodo:127
/* Migration: Set booleans not null. */
ALTER TABLE comment
    MODIFY COLUMN isCorrected TINYINT(1) NOT NULL DEFAULT 0;

ALTER TABLE process
    MODIFY COLUMN inChoiceListShown TINYINT(1) NOT NULL DEFAULT 0;

ALTER TABLE project
    MODIFY COLUMN active TINYINT(1) NOT NULL DEFAULT 1;

ALTER TABLE property
    MODIFY COLUMN obligatory TINYINT(1) NOT NULL DEFAULT 0;

ALTER TABLE ruleset
    MODIFY COLUMN orderMetadataByRuleset TINYINT(1) NOT NULL DEFAULT 1;

ALTER TABLE task
    MODIFY COLUMN typeMetadata TINYINT(1) NOT NULL DEFAULT 0,
    MODIFY COLUMN typeAutomatic TINYINT(1) NOT NULL DEFAULT 0,
    MODIFY COLUMN typeImagesRead TINYINT(1) NOT NULL DEFAULT 0,
    MODIFY COLUMN typeImagesWrite TINYINT(1) NOT NULL DEFAULT 0,
    MODIFY COLUMN typeExportDms TINYINT(1) NOT NULL DEFAULT 0,
    MODIFY COLUMN typeAcceptClose TINYINT(1) NOT NULL DEFAULT 0,
    MODIFY COLUMN typeCloseVerify TINYINT(1) NOT NULL DEFAULT 0,
    MODIFY COLUMN batchStep TINYINT(1) NOT NULL DEFAULT 0,
    MODIFY COLUMN `concurrent` TINYINT(1) NOT NULL DEFAULT 0,
    MODIFY COLUMN `last` TINYINT(1) NOT NULL DEFAULT 0,
    MODIFY COLUMN correction TINYINT(1) NOT NULL DEFAULT 0,
    MODIFY COLUMN separateStructure TINYINT(1) NOT NULL DEFAULT 0,
    MODIFY COLUMN typeGenerateImages TINYINT(1) NOT NULL DEFAULT 0,
    MODIFY COLUMN typeValidateImages TINYINT(1) NOT NULL DEFAULT 0;

ALTER TABLE template
    MODIFY COLUMN inChoiceListShown TINYINT(1) NOT NULL DEFAULT 0;

ALTER TABLE user
    MODIFY COLUMN active TINYINT(1) NOT NULL DEFAULT 1,
    MODIFY COLUMN withMassDownload TINYINT(1) NOT NULL DEFAULT 0,
    MODIFY COLUMN configProductionDateShow TINYINT(1) NOT NULL DEFAULT 0;

--changeset kitodo:3
ALTER TABLE folder ADD COLUMN custom TINYINT(1) DEFAULT 0;

--changeset kitodo:4
UPDATE role SET title = 'Administration' WHERE title = 'AdminGroup';
UPDATE role SET title = 'Qualitätssicherung' WHERE title = 'CheckQualityGroup';
UPDATE role SET title = 'Dossier Scannen A' WHERE title = 'ScanDossierAGroup';
UPDATE role SET title = 'Dossier Scannen B' WHERE title = 'ScanDossierBGroup';
UPDATE role SET title = 'Dossier Scannen C' WHERE title = 'ScanDossierCGroup';
UPDATE role SET title = 'Dossier prüfen' WHERE title = 'CheckDossierGroup';
UPDATE role SET title = 'Dossier vorbereiten' WHERE title = 'PrepareDossierGroup';
UPDATE role SET title = 'Spaltenkonfiguration' WHERE title = 'ConfigureColumns';
UPDATE role SET title = 'Workflow Management' WHERE title = 'WorkflowManagement';
UPDATE role SET title = 'Qualitätssicherung Scans' WHERE title = 'CheckScansGroup';
UPDATE role SET title = 'Datenbankstatistik' WHERE title = 'DatabaseStatistic';
UPDATE role SET title = 'Datenimport Extern' WHERE title = 'ExternGroup';

--changeset kitodo:5
UPDATE user
SET shortcuts = '{"detailView":"Control Shift BracketRight","downItem":"ArrowDown","downItemMulti":"Shift ArrowDown","help":"Shift Minus","nextItem":"ArrowRight","nextItemMulti":"Shift ArrowRight","previousItem":"ArrowLeft","previousItemMulti":"Shift ArrowLeft","structuredView":"Control Shift Slash","upItem":"ArrowUp","upItemMulti":"Alt Shift ArrowUp"}'
WHERE shortcuts = '{"detailView":"Control Shift BracketRight","help":"Shift Minus","nextItem":"Control ArrowDown","nextItemMulti":"Control Shift ArrowDown","previousItem":"Control ArrowUp","previousItemMulti":"Control Shift ArrowUp","structuredView":"Control Shift Slash"}' OR shortcuts = '{"detailView":"","downItem":"","downItemMulti":"","help":"","nextItem":"","nextItemMulti":"","previousItem":"","previousItemMulti":"","structuredView":"","upItem":"","upItemMulti":""}';

--changeset kitodo:6
DROP PROCEDURE IF EXISTS `addPriority`;
DELIMITER //
CREATE PROCEDURE `addPriority`()
BEGIN
    DECLARE CONTINUE HANDLER FOR SQLEXCEPTION BEGIN END;
    ALTER TABLE process ADD COLUMN priority INT (3) DEFAULT 1;
END //
DELIMITER ;
CALL `addPriority`();
DROP PROCEDURE `addPriority`;

--changeset kitodo:7
CREATE TABLE report (
  id INT(11) NOT NULL AUTO_INCREMENT,
  kitodoID INT(11) DEFAULT 0,
  viaducID INT(11) DEFAULT 0,
  numberOfScans INT(11) DEFAULT 0,
  complexity VARCHAR(255) DEFAULT NULL,
  correctionWorkflowActivated TINYINT(1) DEFAULT 0,
  structuralError TINYINT(1) DEFAULT 0,
  missingInformation TINYINT(1) DEFAULT 0,
  reimport TINYINT(1) DEFAULT 0,
  uploadDestination VARCHAR(255) DEFAULT NULL,
  canceled TINYINT(1) DEFAULT 0,
  canceledTimestamp TIMESTAMP NULL DEFAULT NULL,
  canceledMessage VARCHAR(255) DEFAULT NULL,
  workingCopy TINYINT(1) DEFAULT 0,
  taskData TEXT DEFAULT NULL,
  PRIMARY KEY (`id`)
) DEFAULT CHARACTER SET = utf8mb4 COLLATE utf8mb4_unicode_ci;

--changeset kitodo:8
DROP PROCEDURE IF EXISTS `addReportingColumns`;
DELIMITER //
CREATE PROCEDURE `addReportingColumns`()
BEGIN
    DECLARE CONTINUE HANDLER FOR SQLEXCEPTION BEGIN END;
    ALTER TABLE process ADD COLUMN structuralError TINYINT(1) DEFAULT 0;
    ALTER TABLE process ADD COLUMN missingInformation TINYINT(1) DEFAULT 0;
    ALTER TABLE process ADD COLUMN reimport TINYINT(1) DEFAULT 0;
END //
DELIMITER ;
CALL `addReportingColumns`();
DROP PROCEDURE `addReportingColumns`;

--changeset kitodo:9
CREATE TABLE IF NOT EXISTS dataeditor_setting(
    id INT(11) NOT NULL AUTO_INCREMENT,
    user_id INT(11) NOT NULL,
    task_id INT(11) NOT NULL,
    structure_width FLOAT(3) DEFAULT NULL,
    metadata_width FLOAT(3) DEFAULT NULL,
    gallery_width FLOAT(3) DEFAULT NULL,
    PRIMARY KEY (id),
    KEY FK_dataeditorsetting_user_id (user_id),
    KEY FK_dataeditorsetting_task_id (task_id),
    CONSTRAINT FK_dataeditorsetting_user_id
        FOREIGN KEY (user_id) REFERENCES user (id),
    CONSTRAINT  FK_dataeditorsetting_task_id
        FOREIGN KEY (task_id) REFERENCES  task (id)
) DEFAULT CHARACTER SET = utf8mb4
  COLLATE utf8mb4_unicode_ci;

--changeset kitodo:10
INSERT INTO listColumn (title) VALUES ('task.extent');
INSERT INTO listColumn (title) VALUES ('task.complexity');

--changeset kitodo:11
INSERT IGNORE INTO authority (title) VALUES ('viewTaskManager_globalAssignable');
INSERT IGNORE INTO authority (title) VALUES ('viewTerms_globalAssignable');
INSERT IGNORE INTO authority (title) VALUES ('viewMigration_globalAssignable');

INSERT IGNORE INTO role_x_authority (role_id, authority_id)
SELECT (SELECT id FROM role WHERE title = 'Administration'), id FROM authority WHERE title = 'viewTaskManager_globalAssignable';
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
SELECT (SELECT id FROM role WHERE title = 'Administration'), id FROM authority WHERE title = 'viewTerms_globalAssignable';
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
SELECT (SELECT id FROM role WHERE title = 'Administration'), id FROM authority WHERE title = 'viewMigration_globalAssignable';

--changeset kitodo:12
/* Insert list column 'process.lastComment'.*/
INSERT IGNORE INTO listColumn (title) VALUES ('process.lastComment');

/*Update title of 'task.correctionComment' list column.*/
UPDATE listColumn SET title = 'task.lastComment' WHERE title = 'task.correctionComment';

--changeset kitodo:13
DROP PROCEDURE IF EXISTS `addCreatedColumn`;
DELIMITER //
CREATE PROCEDURE `addCreatedColumn`()
BEGIN
    DECLARE CONTINUE HANDLER FOR SQLEXCEPTION BEGIN END;
    ALTER TABLE report ADD COLUMN reportCreatedTimestamp TIMESTAMP NULL DEFAULT NULL;
END //
DELIMITER ;
CALL `addCreatedColumn`();
DROP PROCEDURE `addCreatedColumn`;

--changeset kitodo:14
DROP PROCEDURE IF EXISTS `changeTaskDataToJson`;
DELIMITER //
CREATE PROCEDURE `changeTaskDataToJson`()
BEGIN
    DECLARE CONTINUE HANDLER FOR SQLEXCEPTION BEGIN END;
    ALTER TABLE report MODIFY taskData JSON;
END //
DELIMITER ;
CALL `changeTaskDataToJson`();
DROP PROCEDURE `changeTaskDataToJson`;

--changeset kitodo:15
INSERT IGNORE INTO authority (title) VALUES ('unassignTasks_globalAssignable');
INSERT IGNORE INTO authority (title) VALUES ('unassignTasks_clientAssignable');

INSERT IGNORE INTO role_x_authority (role_id, authority_id)
SELECT (SELECT id FROM role WHERE title = 'Administration'), id FROM authority WHERE title = 'unassignTasks_globalAssignable';
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
SELECT (SELECT id FROM role WHERE title = 'Administration'), id FROM authority WHERE title = 'unassignTasks_clientAssignable';

--changeset kitodo:16
/*1. Switch off safe updates*/
SET SQL_SAFE_UPDATES = 0;

/* 2. Rename user list column "active" to "loggedIn"*/
UPDATE listColumn SET title = "user.loggedIn" WHERE title = "user.active";

/*3. Switch on safe updates*/
SET SQL_SAFE_UPDATES = 1;

--changeset kitodo:17
/* 1. Add column for separateStructure to workflow table */
ALTER TABLE workflow ADD separateStructure TINYINT(1);

/* 2. Switch off safe updates */
SET SQL_SAFE_UPDATES = 0;

/* 3. Set default value for this column */
UPDATE workflow SET separateStructure = 0 WHERE id IS NOT NULL;

--changeset kitodo:18
ALTER TABLE client_x_listColumn RENAME TO client_x_listcolumn;
ALTER TABLE ldapGroup RENAME TO ldapgroup;
ALTER TABLE ldapServer RENAME TO ldapserver;
ALTER TABLE listColumn RENAME TO listcolumn;
ALTER TABLE workflowCondition RENAME TO workflowcondition;

--changeset kitodo:19
/* Remove inChoiceListShown column from template table */
ALTER TABLE template DROP COLUMN inChoiceListShown;

--changeset kitodo:20
/*Insert authorities for upload and delete media in metadata editor.*/
INSERT IGNORE INTO authority (title) VALUES ('uploadMedia_globalAssignable');
INSERT IGNORE INTO authority (title) VALUES ('uploadMedia_clientAssignable');

INSERT IGNORE INTO authority (title) VALUES ('deleteMedia_globalAssignable');
INSERT IGNORE INTO authority (title) VALUES ('deleteMedia_clientAssignable');

INSERT IGNORE INTO role_x_authority (role_id, authority_id)
SELECT (SELECT id FROM role WHERE title = 'Administration'), id FROM authority WHERE title = 'uploadMedia_globalAssignable';
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
SELECT (SELECT id FROM role WHERE title = 'Administration'), id FROM authority WHERE title = 'uploadMedia_clientAssignable';

INSERT IGNORE INTO role_x_authority (role_id, authority_id)
SELECT (SELECT id FROM role WHERE title = 'Administration'), id FROM authority WHERE title = 'deleteMedia_globalAssignable';
INSERT IGNORE INTO role_x_authority (role_id, authority_id)
SELECT (SELECT id FROM role WHERE title = 'Administration'), id FROM authority WHERE title = 'deleteMedia_clientAssignable';

--changeset kitodo:21
/*Add authorities to run Kitodo scripts*/
INSERT IGNORE INTO authority (title) VALUES ('runKitodoScript_clientAssignable');

--changeset kitodo:22
-- Migration: Create table for import configurations.
/*1. Add table "mappingfile"*/
CREATE TABLE IF NOT EXISTS mappingfile (
    id INT(11) NOT NULL AUTO_INCREMENT,
    title varchar(255) NOT NULL,
    file varchar(255) NOT NULL,
    input_metadata_format varchar(255) NOT NULL,
    output_metadata_format varchar(255) NOT NULL,
    PRIMARY KEY(id)
    ) DEFAULT CHARACTER SET = utf8mb4
    COLLATE utf8mb4_unicode_ci;

/*2. Add table "importconfiguration"*/
CREATE TABLE IF NOT EXISTS importconfiguration
(
    id INT(11) NOT NULL AUTO_INCREMENT,
    title varchar(255) NOT NULL,
    description varchar(255) NOT NULL,
    configuration_type varchar(255) NOT NULL,
    prestructured_import tinyint(1) DEFAULT 0,
    interface_type varchar(255) DEFAULT NULL,
    return_format varchar(255) DEFAULT NULL,
    metadata_format varchar(255) DEFAULT NULL,
    default_import_depth INT(11) DEFAULT 2 NULL,
    parent_element_xpath varchar(255) DEFAULT NULL,
    parent_element_type varchar(255) DEFAULT NULL,
    parent_element_trim_mode varchar(255) DEFAULT NULL,
    default_searchfield_id INT(11),
    identifier_searchfield_id INT(11),
    parent_searchfield_id INT(11),
    default_templateprocess_id INT(11),
    parent_templateprocess_id INT(11),
    parent_mappingfile_id INT(11),
    scheme varchar(255) DEFAULT NULL,
    host varchar(255) DEFAULT NULL,
    port INT(11) DEFAULT NULL,
    path varchar(255) DEFAULT NULL,
    anonymous_access tinyint(1) DEFAULT 0,
    username varchar(255) DEFAULT NULL,
    password varchar(255) DEFAULT NULL,
    query_delimiter varchar(255) DEFAULT NULL,
    item_field_xpath varchar(255) DEFAULT NULL,
    item_field_owner_sub_path varchar(255) DEFAULT NULL,
    item_field_owner_metadata varchar(255) DEFAULT NULL,
    item_field_signature_sub_path varchar(255) DEFAULT NULL,
    item_field_signature_metadata varchar(255) DEFAULT NULL,
    id_prefix varchar(255) DEFAULT NULL,
    sru_version varchar(255) DEFAULT NULL,
    sru_record_schema varchar(255) DEFAULT NULL,
    oai_metadata_prefix varchar(255) DEFAULT NULL,
    PRIMARY KEY (id),
    KEY FK_importconfiguration_process_id (default_templateprocess_id),
    CONSTRAINT FK_importconfiguration_process_id
        FOREIGN KEY (default_templateprocess_id) REFERENCES process (id),
    KEY FK_parent_mappingfile_id (parent_mappingfile_id),
    CONSTRAINT FK_parent_mappingfile_id
        FOREIGN KEY (parent_mappingfile_id) REFERENCES mappingfile (id)
) DEFAULT CHARACTER SET = utf8mb4
  COLLATE utf8mb4_unicode_ci;

/*3. Add table "searchfield"*/
CREATE TABLE IF NOT EXISTS searchfield
(
    id INT(11) NOT NULL AUTO_INCREMENT,
    importconfiguration_id INT(11) NOT NULL,
    field_label varchar(255) NOT NULL,
    field_value varchar(255) NOT NULL,
    displayed tinyint(1) DEFAULT 1,
    parent_element tinyint(1) DEFAULT 0,
    PRIMARY KEY(id),
    KEY FK_searchfield_importconfiguration_id (importconfiguration_id),
    CONSTRAINT FK_searchfield_importconfiguration_id
        FOREIGN KEY (importconfiguration_id) REFERENCES importconfiguration (id)
) DEFAULT CHARACTER SET = utf8mb4
  COLLATE utf8mb4_unicode_ci;

/*4. Add table "importconfiguration_x_mappingfile"*/
CREATE TABLE IF NOT EXISTS importconfiguration_x_mappingfile (
    importconfiguration_id INT(11) NOT NULL,
    mappingfile_id INT(11) NOT NULL,
    PRIMARY KEY ( importconfiguration_id, mappingfile_id ),
    KEY FK_importconfiguration_x_mappingfile_importconfiguration_id (importconfiguration_id),
    KEY FK_importconfiguration_x_mappingfile_mappingfile_id (mappingfile_id),
    CONSTRAINT FK_importconfiguration_x_mappingfile_importconfiguration_id FOREIGN KEY (importconfiguration_id) REFERENCES importconfiguration(id),
    CONSTRAINT FK_importconfiguration_x_mappingfile_mappingfile_id FOREIGN KEY (mappingfile_id) REFERENCES mappingfile(id)
) DEFAULT CHARACTER SET = utf8mb4
  COLLATE utf8mb4_unicode_ci;

/*5. Add column 'default_importconfiguration_id' to table 'project'*/
ALTER TABLE project ADD default_importconfiguration_id INT(11);

/*6. Add PICA to Kitodo mapping file*/
INSERT IGNORE INTO mappingfile (title, file, input_metadata_format, output_metadata_format)
VALUES ('PICA to Kitodo mapping', 'pica2kitodo.xsl', 'PICA', 'KITODO');

/*7. Add default K10Plus PICA SRU import configuration*/
INSERT IGNORE INTO importconfiguration (title, description, configuration_type, interface_type, return_format,
                                        default_searchfield_id, identifier_searchfield_id, metadata_format, scheme,
                                        host, path, sru_version, sru_record_schema)
VALUES ('K10Plus-SLUB-PICA', 'K10Plus OPAC PICA', 'OPAC_SEARCH', 'SRU', 'XML', 2, 2, 'PICA', 'https', 'sru.k10plus.de',
        '/gvk', '1.1', 'picaxml');

/*8. Add search fields for K10Plus import configuration*/
INSERT IGNORE INTO searchfield (importconfiguration_id, field_label, field_value) VALUES (1, 'Titel', 'pica.tit');
INSERT IGNORE INTO searchfield (importconfiguration_id, field_label, field_value) VALUES (1, 'PPN', 'pica.ppn');
INSERT IGNORE INTO searchfield (importconfiguration_id, field_label, field_value) VALUES (1, 'Author', 'pica.per');
INSERT IGNORE INTO searchfield (importconfiguration_id, field_label, field_value) VALUES (1, 'ISSN', 'pica.iss');
INSERT IGNORE INTO searchfield (importconfiguration_id, field_label, field_value) VALUES (1, 'ISBN', 'pica.isb');
INSERT IGNORE INTO searchfield (importconfiguration_id, field_label, field_value) VALUES (1, 'Erscheinungsort', 'pica.plc');
INSERT IGNORE INTO searchfield (importconfiguration_id, field_label, field_value) VALUES (1, 'Erscheinungsjahr', 'pica.jah');
INSERT IGNORE INTO searchfield (importconfiguration_id, field_label, field_value) VALUES (1, 'Volltext', 'pica.txt');

/*9. Set mapping file for K10Plus import configuration*/
INSERT IGNORE INTO importconfiguration_x_mappingfile (importconfiguration_id, mappingfile_id) VALUES (1, 1);

/*10. Add authorities to view and edit import configurations and mapping files*/
INSERT IGNORE INTO authority (title) VALUES ('addImportConfiguration_clientAssignable');
INSERT IGNORE INTO authority (title) VALUES ('editImportConfiguration_clientAssignable');
INSERT IGNORE INTO authority (title) VALUES ('viewImportConfiguration_clientAssignable');
INSERT IGNORE INTO authority (title) VALUES ('viewAllImportConfigurations_clientAssignable');
INSERT IGNORE INTO authority (title) VALUES ('deleteImportConfiguration_clientAssignable');

INSERT IGNORE INTO authority (title) VALUES ('addMappingFile_clientAssignable');
INSERT IGNORE INTO authority (title) VALUES ('editMappingFile_clientAssignable');
INSERT IGNORE INTO authority (title) VALUES ('viewMappingFile_clientAssignable');
INSERT IGNORE INTO authority (title) VALUES ('viewAllMappingFiles_clientAssignable');
INSERT IGNORE INTO authority (title) VALUES ('deleteMappingFile_clientAssignable');

--changeset kitodo:23
/*Migration: Add 'sorting' column to importconfiguration/mappingfile cross table*/
ALTER TABLE importconfiguration_x_mappingfile ADD sorting INT(11) DEFAULT 0;

--changeset kitodo:24
/*Migration: Add 'identifier_metadata' column to importconfiguration*/
ALTER TABLE importconfiguration ADD identifier_metadata varchar(255) NOT NULL DEFAULT 'CatalogIDDigital';

--changeset kitodo:25
/*Migration: Add 'prestructured_import' column to 'mappingfile' table*/
ALTER TABLE mappingfile ADD prestructured_import tinyint(1) DEFAULT 0;

--changeset kitodo:26
/*Migration: Remove 'parent_element_xpath' column from importconfiguration*/
ALTER TABLE importconfiguration DROP COLUMN parent_element_xpath;

--changeset kitodo:27
/*Migration: Add 'metadata_record_id_xpath' and 'metadata_record_title_xpath' columns to importconfiguration*/
ALTER TABLE importconfiguration ADD metadata_record_id_xpath varchar(255) NOT NULL;
ALTER TABLE importconfiguration ADD metadata_record_title_xpath varchar(255) NOT NULL;

UPDATE importconfiguration SET metadata_record_id_xpath = './/*[local-name()=''recordInfo'']/*[local-name()=''recordIdentifier'']/text()' WHERE metadata_format = 'MODS';
UPDATE importconfiguration SET metadata_record_id_xpath = './/*[local-name()=''datafield''][@tag=''245'']/*[local-name()=''subfield''][@code=''a'']/text()' WHERE metadata_format = 'MARC';
UPDATE importconfiguration SET metadata_record_id_xpath = './/*[local-name()=''datafield''][@tag=''003@'']/*[local-name()=''subfield''][@code=''0'']/text()' WHERE metadata_format = 'PICA';

UPDATE importconfiguration SET metadata_record_title_xpath = './/*[local-name()=''titleInfo'']/*[local-name()=''title'']/text()' WHERE metadata_format = 'MODS';
UPDATE importconfiguration SET metadata_record_title_xpath = './/*[local-name()=''controlfield''][@tag=''001'']/text()' WHERE metadata_format = 'MARC';
UPDATE importconfiguration SET metadata_record_title_xpath = './/*[local-name()=''datafield''][@tag=''021A'']/*[local-name()=''subfield''][@code=''a'']/text()' WHERE metadata_format = 'PICA';
