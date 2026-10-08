-- =====================================================
-- WildFHIR R4 schema - MySQL 8.x
-- Keep in sync with wildfhirr4_DDL_postgres.sql (same tables, columns,
-- lengths, index names and functions, all identifiers lowercase).
--
-- Run with the mysql client (DELIMITER is a client command), e.g.:
--   mysql -u root -p < wildfhirr4_DDL_mysql.sql
-- =====================================================

-- -----------------------------------------------------
-- Create application user
-- -----------------------------------------------------
CREATE USER IF NOT EXISTS wildfhiruser IDENTIFIED BY 'wildfhiruser';

-- -----------------------------------------------------
-- Schema wildfhirr4
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS wildfhirr4
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_bin;
USE wildfhirr4;

-- -----------------------------------------------------
-- Table wildfhirr4.code
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS wildfhirr4.code (
  id               INT          NOT NULL AUTO_INCREMENT,
  codename         VARCHAR(64)  NOT NULL,
  value            VARCHAR(255) NOT NULL,
  intvalue         BIGINT       DEFAULT 0,
  description      VARCHAR(255) DEFAULT NULL,
  resourcecontents LONGTEXT     DEFAULT NULL,
  PRIMARY KEY (id))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_bin
COMMENT = 'Code Table - WildFHIR configuration settings';


-- -----------------------------------------------------
-- Table wildfhirr4.conformance
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS wildfhirr4.conformance (
  id               INT          NOT NULL AUTO_INCREMENT,
  resourceid       VARCHAR(255) NOT NULL,
  versionid        INT          NOT NULL,
  resourcetype     VARCHAR(45)  NOT NULL,
  status           VARCHAR(45)  NOT NULL,
  lastuser         VARCHAR(255) DEFAULT NULL,
  lastupdate       DATETIME     NOT NULL,
  resourcecontents LONGTEXT     DEFAULT NULL,
  PRIMARY KEY (id))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_bin
COMMENT = 'Stores the CapabilityStatement resource for this server';


-- -----------------------------------------------------
-- Table wildfhirr4.serverdirectory
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS wildfhirr4.serverdirectory (
  id                 INT           NOT NULL AUTO_INCREMENT,
  name               VARCHAR(255)  NOT NULL,
  description        VARCHAR(255)  DEFAULT NULL,
  basepath           VARCHAR(2000) NOT NULL,
  lastuser           VARCHAR(45)   DEFAULT NULL,
  lastupdate         DATETIME      DEFAULT NULL,
  oauthgranttype     VARCHAR(45)   DEFAULT NULL,
  oauthclientid      VARCHAR(64)   DEFAULT NULL,
  oauthclientsecret  VARCHAR(255)  DEFAULT NULL,
  oauthusername      VARCHAR(255)  DEFAULT NULL,
  oauthpassword      VARCHAR(255)  DEFAULT NULL,
  oauthprivatekey    TEXT          DEFAULT NULL,
  oauthpublickey     TEXT          DEFAULT NULL,
  oauthscope         VARCHAR(2000) DEFAULT NULL,
  oauthauthurl       VARCHAR(2000) DEFAULT NULL,
  oauthtokenurl      VARCHAR(2000) DEFAULT NULL,
  oauthintrospecturl VARCHAR(2000) DEFAULT NULL,
  PRIMARY KEY (id))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_bin
COMMENT = 'FHIR server directory list';


-- -----------------------------------------------------
-- Table wildfhirr4.resource
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS wildfhirr4.resource (
  id               INT          NOT NULL AUTO_INCREMENT,
  resourceid       VARCHAR(255) NOT NULL,
  versionid        INT          NOT NULL,
  resourcetype     VARCHAR(45)  NOT NULL,
  status           VARCHAR(45)  NOT NULL,
  lastuser         VARCHAR(255) DEFAULT NULL,
  lastupdate       DATETIME     NOT NULL,
  resourcecontents LONGTEXT     DEFAULT NULL,
  sort0            VARCHAR(500) DEFAULT NULL,
  sort1            VARCHAR(500) DEFAULT NULL,
  sort2            VARCHAR(500) DEFAULT NULL,
  sort3            VARCHAR(500) DEFAULT NULL,
  sort4            VARCHAR(500) DEFAULT NULL,
  sort5            VARCHAR(500) DEFAULT NULL,
  sort6            VARCHAR(500) DEFAULT NULL,
  sort7            VARCHAR(500) DEFAULT NULL,
  sort8            VARCHAR(500) DEFAULT NULL,
  sort9            VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (id),
  INDEX idx_resource_version (resourceid, versionid),
  INDEX idx_resource_status_type (resourcetype, status))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_bin
COMMENT = 'Stores current and history versions of a resource';


-- -----------------------------------------------------
-- Table wildfhirr4.resourcemetadata
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS wildfhirr4.resourcemetadata (
  id             INT          NOT NULL AUTO_INCREMENT,
  resourcejoinid INT          NOT NULL,
  paramname      VARCHAR(127) NOT NULL,
  paramtype      VARCHAR(45)  NOT NULL,
  paramvalue     VARCHAR(670) DEFAULT NULL,
  systemvalue    VARCHAR(670) DEFAULT NULL,
  codevalue      VARCHAR(670) DEFAULT NULL,
  textvalue      VARCHAR(670) DEFAULT NULL,
  paramvalueu    VARCHAR(670) DEFAULT NULL,
  textvalueu     VARCHAR(670) DEFAULT NULL,
  PRIMARY KEY (id),
  INDEX fk_resourcemetatdata_resource_idx (resourcejoinid),
  INDEX idx_resourcemetadata_paramname (paramname),
  INDEX idx_resourcemetadata_paramnametype (paramname, paramtype),
  INDEX idx_resourcemetadata_paramvalue (paramvalue),
  INDEX idx_resourcemetadata_systemvalue (systemvalue),
  INDEX idx_resourcemetadata_codevalue (codevalue),
  INDEX idx_resourcemetadata_textvalue (textvalue),
  INDEX idx_resourcemetadata_paramvalueu (paramvalueu),
  INDEX idx_resourcemetadata_textvalueu (textvalueu),
  CONSTRAINT fk_resourcemetatdata_resource
    FOREIGN KEY (resourcejoinid)
    REFERENCES wildfhirr4.resource (id))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_bin
COMMENT = 'The current valid resource searchable metadata';


-- -----------------------------------------------------
-- Table wildfhirr4.subscriptionactivity
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS wildfhirr4.subscriptionactivity (
  id             INT           NOT NULL AUTO_INCREMENT,
  subscriptionid VARCHAR(255)  NOT NULL,
  recorded       DATETIME      NOT NULL,
  type           VARCHAR(45)   NOT NULL,
  status         VARCHAR(45)   NOT NULL,
  description    VARCHAR(1000) NOT NULL,
  PRIMARY KEY (id),
  INDEX idx_resourceid_status (subscriptionid, status))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_bin
COMMENT = 'Stores record of subscription activity';


-- -----------------------------------------------------
-- Function wildfhirr4.calcdistancekm
-- Great-circle (haversine) distance between two lat/lon points, in kilometers
-- -----------------------------------------------------
DROP FUNCTION IF EXISTS wildfhirr4.calcdistancekm;

DELIMITER $$
CREATE FUNCTION wildfhirr4.calcdistancekm(
  lat1 DOUBLE,
  lon1 DOUBLE,
  lat2 DOUBLE,
  lon2 DOUBLE)
  RETURNS DOUBLE
  DETERMINISTIC
  NO SQL
BEGIN
  DECLARE dlat     DOUBLE DEFAULT RADIANS(lat2 - lat1);
  DECLARE dlon     DOUBLE DEFAULT RADIANS(lon2 - lon1);
  DECLARE a        DOUBLE;
  DECLARE c        DOUBLE;
  DECLARE distance DOUBLE;

  SET a = SIN(dlat/2) * SIN(dlat/2) +
          COS(RADIANS(lat1)) * COS(RADIANS(lat2)) *
          SIN(dlon/2) * SIN(dlon/2);
  SET a = LEAST(a, 1.0); -- guard against rounding pushing a above 1 for near-antipodal points
  SET c = 2 * ATAN2(SQRT(a), SQRT(1-a));
  SET distance = 6371.0 * c; -- Earth's radius in kilometers
  RETURN distance;
END$$
DELIMITER ;


-- -----------------------------------------------------
-- Function wildfhirr4.calcdistancemi
-- Great-circle (haversine) distance between two lat/lon points, in miles
-- -----------------------------------------------------
DROP FUNCTION IF EXISTS wildfhirr4.calcdistancemi;

DELIMITER $$
CREATE FUNCTION wildfhirr4.calcdistancemi(
  lat1 DOUBLE,
  lon1 DOUBLE,
  lat2 DOUBLE,
  lon2 DOUBLE)
  RETURNS DOUBLE
  DETERMINISTIC
  NO SQL
BEGIN
  DECLARE dlat     DOUBLE DEFAULT RADIANS(lat2 - lat1);
  DECLARE dlon     DOUBLE DEFAULT RADIANS(lon2 - lon1);
  DECLARE a        DOUBLE;
  DECLARE c        DOUBLE;
  DECLARE distance DOUBLE;

  SET a = SIN(dlat/2) * SIN(dlat/2) +
          COS(RADIANS(lat1)) * COS(RADIANS(lat2)) *
          SIN(dlon/2) * SIN(dlon/2);
  SET a = LEAST(a, 1.0); -- guard against rounding pushing a above 1 for near-antipodal points
  SET c = 2 * ATAN2(SQRT(a), SQRT(1-a));
  SET distance = 3959.0 * c; -- Earth's radius in miles
  RETURN distance;
END$$
DELIMITER ;


-- -----------------------------------------------------
-- Grant privileges to wildfhiruser
-- -----------------------------------------------------
GRANT SELECT, INSERT, UPDATE, DELETE, EXECUTE ON wildfhirr4.* TO wildfhiruser;
