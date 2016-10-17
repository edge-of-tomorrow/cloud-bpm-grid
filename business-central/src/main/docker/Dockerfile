####### BASE ############
FROM jboss/wildfly:10.1.0.Final

####### MAINTAINER ############
MAINTAINER "Ivo Bek" "ibek@redhat.com"

####### ENVIRONMENT ############
ENV JAVA_OPTS -XX:MaxPermSize=256m -Xms256m -Xmx512m
ENV JBOSS_BIND_ADDRESS 0.0.0.0
ENV BUSINESS_CENTRAL_VERSION 7.0.0-SNAPSHOT
ENV BUSINESS_CENTRAL_WAR kie-wb-1.0-SNAPSHOT.war
EXPOSE 8080

VOLUME ["/opt/jboss/wildfly/bin/.niogit"]

####### SYSTEM USERS FOR DEVELOPMENT ############
USER root
# Set a password for root & jboss users (for any further system operations, etc)
RUN echo "root:rootman" | chpasswd && \
echo "jboss:jboss" | chpasswd

####### BUSINESS CENTRAL & WILDFLY10 CONFIG FILES ############
# Latest WAR from Maven repository & Custom Wildfly configuration files

# KIE Server custom configuration.
ADD classes/config/business-central-users.properties $JBOSS_HOME/standalone/configuration/business-central-users.properties
ADD classes/config/business-central-roles.properties $JBOSS_HOME/standalone/configuration/business-central-roles.properties
ADD classes/config/standalone-full-business-central.xml $JBOSS_HOME/standalone/configuration/standalone-full-business-central.xml
ADD classes/config/settings.xml $JBOSS_HOME/bin/settings.xml
ADD classes/config/secure-deployment-offline.cli $JBOSS_HOME/bin/secure-deployment-offline.cli

# Patch
# ADD classes/config/jbpm-console-ng-business-domain-backend-7.0.0-SNAPSHOT.jar $JBOSS_HOME/jbpm-console-ng-business-domain-backend-7.0.0-SNAPSHOT.jar

# Custom kie-server Wildfly startup scripts.
ADD classes/config/start-business-central.sh $JBOSS_HOME/bin/start-business-central.sh

# Add BUSINESS_CENTRAL_WAR
ADD ${BUSINESS_CENTRAL_WAR} $JBOSS_HOME/standalone/deployments/business-central-archive.war

# Work with unpacked WAR archives in WildFly, so other images can override webapp files (such as persistence.xml,etc)
RUN unzip -q -d $JBOSS_HOME/standalone/deployments/business-central.war $JBOSS_HOME/standalone/deployments/business-central-archive.war && \
touch $JBOSS_HOME/standalone/deployments/business-central.war.dodeploy && \
rm -rf $JBOSS_HOME/standalone/deployments/business-central-archive.war

RUN yum -y install wget

RUN wget https://downloads.jboss.org/keycloak/2.2.1.Final/adapters/keycloak-oidc/keycloak-wildfly-adapter-dist-2.2.1.Final.zip -O $JBOSS_HOME/keycloak-adapter.zip

RUN unzip -q -d $JBOSS_HOME/ $JBOSS_HOME/keycloak-adapter.zip && \
rm $JBOSS_HOME/keycloak-adapter.zip

RUN rm $JBOSS_HOME/standalone/configuration/standalone.xml
RUN mv $JBOSS_HOME/standalone/configuration/standalone-full-business-central.xml $JBOSS_HOME/standalone/configuration/standalone.xml

# Patch
# RUN rm $JBOSS_HOME/standalone/deployments/business-central.war/WEB-INF/lib/jbpm-console-ng-business-domain-backend-7.0.0-SNAPSHOT.jar
# RUN mv $JBOSS_HOME/jbpm-console-ng-business-domain-backend-7.0.0-SNAPSHOT.jar $JBOSS_HOME/standalone/deployments/business-central.war/WEB-INF/lib/jbpm-console-ng-business-domain-backend-7.0.0-SNAPSHOT.jar

# Set right permissions for jboss user.
RUN chown -R jboss:jboss $JBOSS_HOME/standalone/deployments/* && \
chmod +x $JBOSS_HOME/bin/*.sh && \
chown jboss:jboss $JBOSS_HOME/bin/start-business-central.sh && \
chown jboss:jboss $JBOSS_HOME/bin/adapter-install-offline.cli && \
chown jboss:jboss $JBOSS_HOME/bin/secure-deployment-offline.cli && \
chown jboss:jboss $JBOSS_HOME/standalone/configuration/business-central-users.properties && \ 
chown jboss:jboss $JBOSS_HOME/standalone/configuration/business-central-roles.properties && \
chown jboss:jboss $JBOSS_HOME/standalone/configuration/standalone.xml && \
chown jboss:jboss $JBOSS_HOME/bin/settings.xml && \
chown jboss:jboss /opt/jboss/wildfly/bin/.niogit

# Switchback to jboss user
USER jboss

####### COMMAND ############
WORKDIR $JBOSS_HOME/bin/
CMD ["./start-business-central.sh"]
