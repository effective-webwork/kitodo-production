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
