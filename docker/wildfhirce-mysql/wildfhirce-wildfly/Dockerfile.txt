# syntax=docker/dockerfile:1

# Use quay.io/wildfly/wildfly:39.0.1.Final-2-jdk21 image as the base
FROM quay.io/wildfly/wildfly:39.0.1.Final-2-jdk21

# Set the JBOSS_HOME env variable
ENV JBOSS_HOME=/opt/jboss/wildfly

# FHIR packages comma separated list of packagename#version
ENV FHIR_PACKAGES=hl7.fhir.r4.core#4.0.1

# Ensure signals are forwarded to the JVM process correctly for graceful shutdown
ENV LAUNCH_JBOSS_IN_BACKGROUND=true

USER root

# Create FHIR package cache folder
RUN cd /opt/jboss && mkdir .fhir && chown jboss .fhir && chmod 777 .fhir

USER jboss

RUN /opt/jboss/wildfly/bin/add-user.sh admin admin --silent

# Add MySQL 9.6.0 connector library
ADD mysql $JBOSS_HOME/modules/system/layers/base/com/mysql

# Copy standalone conf for start up with JAVA_OPTS settings
COPY ./standalone.conf $JBOSS_HOME/bin

# Copy standalone configuration with datasource connection to wildfhirce-mysql and https access using a self-signed certificate
COPY ./standalone.xml $JBOSS_HOME/standalone/configuration

# Copy WildFHIR CE Applications for FHIR R4 to Wildfly standalone deployments
COPY ./wildfhir-client.war $JBOSS_HOME/standalone/deployments
COPY ./wildfhir-rest-server.war $JBOSS_HOME/standalone/deployments

# Expose the ports we're interested in
EXPOSE 8080 8443 9990

# Set the default command to run on boot
# This will boot WildFly in the standalone mode and bind to all interface
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]