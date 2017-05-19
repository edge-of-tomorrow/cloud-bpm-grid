####### BASE ############
FROM jboss/wildfly:10.1.0.Final

####### MAINTAINER ############
MAINTAINER "Ivo Bek" "ibek@redhat.com"

####### ENVIRONMENT ############
ENV JAVA_OPTS -XX:MaxPermSize=256m -Xms1303m -Xmx1303m -Djava.net.preferIPv4Stack=true

ENV BUSINESS_CENTRAL_VERSION ${version.org.kie}
ENV BUSINESS_CENTRAL_WAR kie-wb-${project.version}

ENV KEYCLOAK_VERSION ${version.org.keycloak}
# ! ENV AUTH_HOST
# ! ENV AUTH_PORT
# ! ENV AUTH_REALM
# ! ENV AUTH_PROCESS_SERVER_SECRET

EXPOSE 8080

VOLUME ["/opt/jboss/wildfly/bin/.niogit"]

####### SYSTEM USERS FOR DEVELOPMENT ############
USER root

COPY classes/config/standalone-full-business-central.xml $JBOSS_HOME/standalone/configuration/standalone-full-business-central.xml
COPY classes/config/secure-deployment-offline.cli classes/config/settings.xml $JBOSS_HOME/bin/
COPY classes/config/business-central-users.properties $JBOSS_HOME/standalone/configuration/business-central-users.properties
COPY classes/config/business-central-roles.properties $JBOSS_HOME/standalone/configuration/business-central-roles.properties
COPY ${BUSINESS_CENTRAL_WAR} $JBOSS_HOME/standalone/deployments/business-central.war

# Set a password for root & jboss users (for any further system operations, etc)
RUN echo "root:rootman" | chpasswd && \
echo "jboss:jboss" | chpasswd && \
rm $JBOSS_HOME/standalone/configuration/standalone.xml && \
mv $JBOSS_HOME/standalone/configuration/standalone-full-business-central.xml $JBOSS_HOME/standalone/configuration/standalone.xml && \
curl -o $JBOSS_HOME/keycloak-adapter.zip https://downloads.jboss.org/keycloak/${KEYCLOAK_VERSION}/adapters/keycloak-oidc/keycloak-wildfly-adapter-dist-${KEYCLOAK_VERSION}.zip && \
unzip -q -d $JBOSS_HOME/ $JBOSS_HOME/keycloak-adapter.zip && \
rm $JBOSS_HOME/keycloak-adapter.zip && \
$JBOSS_HOME/bin/jboss-cli.sh --file=$JBOSS_HOME/bin/adapter-install-offline.cli && \
$JBOSS_HOME/bin/jboss-cli.sh --file=$JBOSS_HOME/bin/secure-deployment-offline.cli && \
touch $JBOSS_HOME/standalone/deployments/business-central.war.dodeploy

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
