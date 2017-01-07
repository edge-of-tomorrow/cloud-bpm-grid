####### BASE ############
FROM jboss/wildfly:10.1.0.Final

####### MAINTAINER ############
MAINTAINER "Ivo Bek" "ibek@redhat.com"

####### ENVIRONMENT ############
ENV JAVA_OPTS -XX:MaxPermSize=256m -Xms1303m -Xmx1303m -Djava.net.preferIPv4Stack=true

ENV BUSINESS_CENTRAL_VERSION 7.0.0-SNAPSHOT
ENV BUSINESS_CENTRAL_WAR kie-wb-1.0-SNAPSHOT.war

ENV KEYCLOAK_VERSION 2.5.0.Final

EXPOSE 8080

VOLUME ["/opt/jboss/wildfly/bin/.niogit"]

####### SYSTEM USERS FOR DEVELOPMENT ############
USER root
# Set a password for root & jboss users (for any further system operations, etc)
RUN echo "root:rootman" | chpasswd && \
echo "jboss:jboss" | chpasswd

####### BUSINESS CENTRAL & WILDFLY10 CONFIG FILES ############

ADD classes/config/standalone-full-business-central.xml $JBOSS_HOME/standalone/configuration/standalone-full-business-central.xml

RUN rm $JBOSS_HOME/standalone/configuration/standalone.xml && \
mv $JBOSS_HOME/standalone/configuration/standalone-full-business-central.xml $JBOSS_HOME/standalone/configuration/standalone.xml

# Install Keycloak and secure Process Server

ADD classes/config/secure-deployment-offline.cli $JBOSS_HOME/bin/secure-deployment-offline.cli

RUN yum -y install wget && \
yum clean all -y && \
wget https://downloads.jboss.org/keycloak/${KEYCLOAK_VERSION}/adapters/keycloak-oidc/keycloak-wildfly-adapter-dist-${KEYCLOAK_VERSION}.zip -O $JBOSS_HOME/keycloak-adapter.zip && \
unzip -q -d $JBOSS_HOME/ $JBOSS_HOME/keycloak-adapter.zip && \
rm $JBOSS_HOME/keycloak-adapter.zip && \
$JBOSS_HOME/bin/jboss-cli.sh --file=$JBOSS_HOME/bin/adapter-install-offline.cli && \
$JBOSS_HOME/bin/jboss-cli.sh --file=$JBOSS_HOME/bin/secure-deployment-offline.cli

# Add Business Central
ADD ${BUSINESS_CENTRAL_WAR} $JBOSS_HOME/standalone/deployments/business-central-archive.war

RUN unzip -q -d $JBOSS_HOME/standalone/deployments/business-central.war $JBOSS_HOME/standalone/deployments/business-central-archive.war && \
touch $JBOSS_HOME/standalone/deployments/business-central.war.dodeploy && \
rm $JBOSS_HOME/standalone/deployments/business-central-archive.war

# Business Central Customization

ADD classes/config/business-central-users.properties $JBOSS_HOME/standalone/configuration/business-central-users.properties
ADD classes/config/business-central-roles.properties $JBOSS_HOME/standalone/configuration/business-central-roles.properties
ADD classes/config/settings.xml $JBOSS_HOME/bin/settings.xml

# Patch
# ADD classes/config/jbpm-console-ng-business-domain-backend-7.0.0-SNAPSHOT.jar $JBOSS_HOME/jbpm-console-ng-business-domain-backend-7.0.0-SNAPSHOT.jar
# RUN rm $JBOSS_HOME/standalone/deployments/business-central.war/WEB-INF/lib/jbpm-console-ng-business-domain-backend-7.0.0-SNAPSHOT.jar
# RUN mv $JBOSS_HOME/jbpm-console-ng-business-domain-backend-7.0.0-SNAPSHOT.jar $JBOSS_HOME/standalone/deployments/business-central.war/WEB-INF/lib/jbpm-console-ng-business-domain-backend-7.0.0-SNAPSHOT.jar

# Set right permissions for jboss user.
RUN chown -R jboss:jboss $JBOSS_HOME/* && \
chmod +x $JBOSS_HOME/bin/*.sh

# Switchback to jboss user
USER jboss

####### COMMAND ############
WORKDIR $JBOSS_HOME/bin/
CMD ["./standalone.sh", "-b", "0.0.0.0", "-c", "standalone.xml"]
