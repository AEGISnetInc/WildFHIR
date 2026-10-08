# syntax=docker/dockerfile:1

# Use the official, PostgreSQL 18 image as a base
FROM postgres:18

# Database created on first start; the DDL then creates the wildfhirr4
# schema and wildfhiruser inside it.
ENV POSTGRES_DB=wildfhirr4

# Builtin C.UTF-8 locale: code-point sort order (matches MySQL
# utf8mb4_0900_bin) and lets ordinary indexes serve LIKE 'prefix%'.
ENV POSTGRES_INITDB_ARGS="--encoding=UTF8 --locale-provider=builtin --builtin-locale=C.UTF-8"

# Copy DDL and shell scripts
COPY ./00_wildfhirr4_DDL_postgres.sql /docker-entrypoint-initdb.d/00_wildfhirr4_DDL_postgres.sql
COPY ./01_code_insert.sql /docker-entrypoint-initdb.d/01_code_insert.sql
COPY ./02_conformance_insert.sql /docker-entrypoint-initdb.d/02_conformance_insert.sql
COPY ./03_serverdirectory_insert.sql /docker-entrypoint-initdb.d/03_serverdirectory_insert.sql
COPY ./04_resource_insert.sql /docker-entrypoint-initdb.d/04_resource_insert.sql
COPY ./05_resourcemetadata_insert.sql /docker-entrypoint-initdb.d/05_resourcemetadata_insert.sql
