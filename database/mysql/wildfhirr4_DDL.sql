-- create application user
CREATE USER IF NOT EXISTS wildfhiruser IDENTIFIED BY 'wildfhiruser';

-- -----------------------------------------------------
-- Schema wildfhirr4
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS wildfhirr4 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci ;
USE wildfhirr4 ;

-- -----------------------------------------------------
-- Table wildfhirr4.code
-- -----------------------------------------------------
CREATE TABLE wildfhirr4.code (
  id INT(11) NOT NULL AUTO_INCREMENT,
  codeName VARCHAR(64) CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_unicode_ci' NOT NULL,
  value VARCHAR(255) CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_unicode_ci' NOT NULL,
  intValue BIGINT(20) DEFAULT '0',
  description VARCHAR(255) CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_unicode_ci' DEFAULT NULL,
  resourceContents LONGTEXT CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_unicode_ci' NULL DEFAULT NULL,
  PRIMARY KEY (id))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_unicode_ci
COMMENT = 'Code Table - WildFHIR configuration settings';


-- -----------------------------------------------------
-- Table wildfhirr4.conformance
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS wildfhirr4.conformance (
  id INT(11) NOT NULL AUTO_INCREMENT,
  resourceId VARCHAR(255) CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_unicode_ci' NOT NULL,
  versionId INT(11) NOT NULL,
  resourceType VARCHAR(45) CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_unicode_ci' NOT NULL,
  status VARCHAR(45) CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_unicode_ci' NOT NULL,
  lastUser VARCHAR(255) CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_unicode_ci' NULL DEFAULT NULL,
  lastUpdate DATETIME NOT NULL,
  resourceContents LONGTEXT CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_unicode_ci' NULL DEFAULT NULL,
  PRIMARY KEY (id))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_unicode_ci
COMMENT = 'Stores the CapabilityStatement resource for this server';

CREATE UNIQUE INDEX id_UNIQUE ON wildfhirr4.conformance (id ASC);


-- -----------------------------------------------------
-- Table wildfhirr4.resource
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS wildfhirr4.resource (
  id INT(11) NOT NULL AUTO_INCREMENT,
  resourceId VARCHAR(255) CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_unicode_ci' NOT NULL,
  versionId INT(11) NOT NULL,
  resourceType VARCHAR(45) CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_unicode_ci' NOT NULL,
  status VARCHAR(45) CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_unicode_ci' NOT NULL,
  lastUser VARCHAR(255) CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_unicode_ci' NULL DEFAULT NULL,
  lastUpdate DATETIME NOT NULL,
  resourceContents LONGTEXT CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_unicode_ci' NULL DEFAULT NULL,
  sort0 VARCHAR(500) CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_unicode_ci' NULL DEFAULT NULL,
  sort1 VARCHAR(500) CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_unicode_ci' NULL DEFAULT NULL,
  sort2 VARCHAR(500) CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_unicode_ci' NULL DEFAULT NULL,
  sort3 VARCHAR(500) CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_unicode_ci' NULL DEFAULT NULL,
  sort4 VARCHAR(500) CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_unicode_ci' NULL DEFAULT NULL,
  sort5 VARCHAR(500) CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_unicode_ci' NULL DEFAULT NULL,
  sort6 VARCHAR(500) CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_unicode_ci' NULL DEFAULT NULL,
  sort7 VARCHAR(500) CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_unicode_ci' NULL DEFAULT NULL,
  sort8 VARCHAR(500) CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_unicode_ci' NULL DEFAULT NULL,
  sort9 VARCHAR(500) CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_unicode_ci' NULL DEFAULT NULL,
  PRIMARY KEY (id))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_unicode_ci
COMMENT = 'Stores current and history versions of a resource';

CREATE UNIQUE INDEX id_UNIQUE ON wildfhirr4.resource (id ASC);

CREATE INDEX idx_resource_version ON wildfhirr4.resource (resourceId ASC, versionId ASC);

CREATE INDEX idx_resource_status_type ON wildfhirr4.resource (resourceType ASC, status ASC);


-- -----------------------------------------------------
-- Table wildfhirr4.resourcemetadata
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS wildfhirr4.resourcemetadata (
  id INT(11) NOT NULL AUTO_INCREMENT,
  resourceJoinId INT(11) NOT NULL,
  paramName VARCHAR(127) CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_unicode_ci' NOT NULL,
  paramType VARCHAR(45) CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_unicode_ci' NOT NULL,
  paramValue VARCHAR(750) CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_unicode_ci' NULL DEFAULT NULL,
  systemValue VARCHAR(750) CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_unicode_ci' NULL DEFAULT NULL,
  codeValue VARCHAR(750) CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_unicode_ci' NULL DEFAULT NULL,
  textValue VARCHAR(750) CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_unicode_ci' NULL DEFAULT NULL,
  paramValueU VARCHAR(750) CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_unicode_ci' NULL DEFAULT NULL,
  textValueU VARCHAR(750) CHARACTER SET 'utf8mb4' COLLATE 'utf8mb4_unicode_ci' NULL DEFAULT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_resourcemetatdata_resource
    FOREIGN KEY (resourceJoinId)
    REFERENCES wildfhirr4.resource (id))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_unicode_ci
COMMENT = 'The current valid resource searchable metadata';

CREATE UNIQUE INDEX id_UNIQUE ON wildfhirr4.resourcemetadata (id ASC);

CREATE INDEX fk_resourcemetatdata_resource_idx ON wildfhirr4.resourcemetadata (resourceJoinId ASC);

CREATE INDEX idx_resourcemetadata_paramName ON wildfhirr4.resourcemetadata (paramName ASC);

CREATE INDEX idx_resourcemetadata_paramNameType ON wildfhirr4.resourcemetadata (paramName ASC, paramType ASC);

CREATE INDEX idx_resourcemetadata_paramValue ON wildfhirr4.resourcemetadata (paramValue ASC);

CREATE INDEX idx_resourcemetadata_systemValue ON wildfhirr4.resourcemetadata (systemValue ASC);

CREATE INDEX idx_resourcemetadata_codeValue ON wildfhirr4.resourcemetadata (codeValue ASC);

CREATE INDEX idx_resourcemetadata_textValue ON wildfhirr4.resourcemetadata (textValue ASC);

CREATE INDEX idx_resourcemetadata_paramValueU ON wildfhirr4.resourcemetadata (paramValueU ASC);

CREATE INDEX idx_resourcemetadata_textValueU ON wildfhirr4.resourcemetadata (textValueU ASC);

-- grant privileges to wildfhiruser
GRANT SELECT,INSERT,UPDATE,DELETE,EXECUTE ON wildfhirr4.* to wildfhiruser;
