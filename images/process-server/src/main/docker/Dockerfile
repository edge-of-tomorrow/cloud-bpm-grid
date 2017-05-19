####### BASE ############
FROM jboss/wildfly:10.1.0.Final

####### MAINTAINER ############
MAINTAINER "Ivo Bek" "ibek@redhat.com"

####### ENVIRONMENT ############
ENV JAVA_OPTS -XX:MaxPermSize=256m -Xms256m -Xmx768m -Djava.net.preferIPv4Stack=true
ENV JBOSS_BIND_ADDRESS 0.0.0.0

ENV PROCESS_SERVER_ID process-server-1
ENV PROCESS_SERVER_VERSION ${version.org.kie}
ENV PROCESS_SERVER_WAR kie-server-${project.version}

# ! ENV PROCESS_SERVER_CONTROLLER http://$BC_HOST:$BC_PORT/business-central/rest/controller

# ! ENV BUSINESS_CENTRAL_CONTROLLER_USER
# ! ENV BUSINESS_CENTRAL_CONTROLLER_PWD

# ENV TASK_INBOX_URL http://$BC_HOST:$BC_PORT/business-central
# ENV MAIL_FROM noreply@bpm.grid

ENV KEYCLOAK_VERSION ${version.org.keycloak}

# ! ENV DB_NAME
# ! ENV DB_USER
# ! ENV DB_PWD
# ! ENV DB_HOST
ENV DB_PORT 5432

# ! ENV AUTH_HOST
# ! ENV AUTH_PORT
# ! ENV AUTH_REALM
# ! ENV AUTH_PROCESS_SERVER_SECRET

EXPOSE 8080

USER root

COPY settings.xml classes/config/add-datasource.cli classes/config/secure-deployment-offline.cli classes/config/start-process-server.sh $JBOSS_HOME/bin/
COPY postgresql.jar $JBOSS_HOME/postgresql-driver.jar
COPY ${PROCESS_SERVER_WAR} $JBOSS_HOME/standalone/deployments/kie-server.war

# Set a password for root & jboss users (for any further system operations, etc)
RUN echo "root:rootman" | chpasswd && \
echo "jboss:jboss" | chpasswd && \
rm $JBOSS_HOME/standalone/configuration/standalone.xml && \
mv $JBOSS_HOME/standalone/configuration/standalone-full.xml $JBOSS_HOME/standalone/configuration/standalone.xml && \
$JBOSS_HOME/bin/jboss-cli.sh --file=$JBOSS_HOME/bin/add-datasource.cli && \
curl -o $JBOSS_HOME/keycloak-adapter.zip https://downloads.jboss.org/keycloak/${KEYCLOAK_VERSION}/adapters/keycloak-oidc/keycloak-wildfly-adapter-dist-${KEYCLOAK_VERSION}.zip && \
unzip -q -d $JBOSS_HOME/ $JBOSS_HOME/keycloak-adapter.zip && \
rm $JBOSS_HOME/keycloak-adapter.zip && \
$JBOSS_HOME/bin/jboss-cli.sh --file=$JBOSS_HOME/bin/adapter-install-offline.cli && \
$JBOSS_HOME/bin/jboss-cli.sh --file=$JBOSS_HOME/bin/secure-deployment-offline.cli && \
touch $JBOSS_HOME/standalone/deployments/kie-server.war.dodeploy

COPY classes/config/logback.xml $JBOSS_HOME/standalone/deployments/kie-server.war/WEB-INF/classes/logback.xml

# Add Process Server extensions
COPY process-server-1.0-SNAPSHOT.jar $JBOSS_HOME/standalone/deployments/kie-server.war/WEB-INF/lib/process-server.jar

# Integrate Process Server with Authentication Server
COPY classes/config/jbpm.user.info.properties $JBOSS_HOME/standalone/deployments/kie-server.war/WEB-INF/classes/jbpm.user.info.properties

# Set right permissions for jboss user.
RUN chown -R jboss:jboss $JBOSS_HOME/* && \
chmod +x $JBOSS_HOME/bin/*.sh

# Switchback to jboss user
USER jboss

####### COMMAND ############
WORKDIR $JBOSS_HOME/bin/
CMD ["./start-process-server.sh"]
