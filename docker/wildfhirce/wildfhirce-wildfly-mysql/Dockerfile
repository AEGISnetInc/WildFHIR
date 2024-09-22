# syntax=docker/dockerfile:1

# Use latest jboss/base-jdk:11 image as the base
FROM jboss/base-jdk:11

# Set the WILDFLY_VERSION env variable
ENV WILDFLY_VERSION=20.0.1.Final
ENV WILDFLY_SHA1=95366b4a0c8f2e6e74e3e4000a98371046c83eeb
ENV JBOSS_HOME=/opt/jboss/wildfly

USER root

# Add the WildFly distribution to /opt, and make wildfly the owner of the extracted tar content
# Make sure the distribution is available from a well-known place
RUN cd $HOME \
    && curl -O https://download.jboss.org/wildfly/$WILDFLY_VERSION/wildfly-$WILDFLY_VERSION.tar.gz \
    && sha1sum wildfly-$WILDFLY_VERSION.tar.gz | grep $WILDFLY_SHA1 \
    && tar xf wildfly-$WILDFLY_VERSION.tar.gz \
    && mv $HOME/wildfly-$WILDFLY_VERSION $JBOSS_HOME \
    && rm wildfly-$WILDFLY_VERSION.tar.gz \
    && chown -R jboss:0 ${JBOSS_HOME} \
    && chmod -R g+rw ${JBOSS_HOME}

# Ensure signals are forwarded to the JVM process correctly for graceful shutdown
ENV LAUNCH_JBOSS_IN_BACKGROUND=true

USER jboss

RUN /opt/jboss/wildfly/bin/add-user.sh admin admin --silent

# Add MySQL 8.0 connector library
ADD mysql $JBOSS_HOME/modules/system/layers/base/com/mysql

# Copy standalone conf for start up with JAVA_OPTS settings
COPY ./standalone.conf $JBOSS_HOME/bin

# Copy standalone configuration with datasource connection to wildfhirce-mysql and https access using a self-signed certificate
COPY ./standalone.xml $JBOSS_HOME/standalone/configuration

# Copy WildFHIR CE Server Application for FHIR R4 to Wildfly standalone deployments
COPY ./wildfhir-rest-server.war $JBOSS_HOME/standalone/deployments

# Expose the ports we're interested in
EXPOSE 80 443 9990

# Set the default command to run on boot
# This will boot WildFly in the standalone mode and bind to all interface
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]