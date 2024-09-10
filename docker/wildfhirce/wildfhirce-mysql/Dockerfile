# syntax=docker/dockerfile:1

# Use the official MySQL 8.0 image as a base
FROM mysql:8.0

# Used to gracefully shutdown mysql after each execution
COPY ./wait_then_shutdown.sh /tmp/wait_then_shutdown.sh

# Set the default user for the MySQL image
USER mysql

# Copy DDL and shell scripts
COPY ./00_wildfhirr4_DDL.sql /docker-entrypoint-initdb.d/00_wildfhirr4_DDL.sql
COPY ./01_calcDistanceKm_function.sql /docker-entrypoint-initdb.d/01_calcDistanceKm_function.sql
COPY ./02_calcDistanceMi_function.sql /docker-entrypoint-initdb.d/02_calcDistanceMi_function.sql
COPY ./03_code_insert.sql /docker-entrypoint-initdb.d/03_code_insert.sql
COPY ./04_conformance_insert.sql /docker-entrypoint-initdb.d/04_conformance_insert.sql
COPY ./05_resource_insert.sql /docker-entrypoint-initdb.d/05_resource_insert.sql
COPY ./06_resourcemetadata_insert.sql /docker-entrypoint-initdb.d/06_resourcemetadata_insert.sql
COPY ./99_last_processed_file.sh /docker-entrypoint-initdb.d/99_last_processed_file.sh

# Copy MySQL custom configuration
# - innodb_buffer_pool_size = 1024M
# - max_connections=256
COPY ./my.cnf /etc/my.cnf

# Set environment variables for MySQL
ENV MYSQL_ALLOW_EMPTY_PASSWORD=1

RUN /entrypoint.sh mysqld & /tmp/wait_then_shutdown.sh  # <-- 

# Expose port 3306 to allow connections to the database
EXPOSE 3306

# Start the MySQL server when the container is run
CMD ["mysqld"]