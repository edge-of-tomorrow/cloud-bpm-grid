####### BASE ############
FROM jboss/wildfly:10.1.0.Final

####### MAINTAINER ############
MAINTAINER "Ivo Bek" "ibek@redhat.com"

####### ENVIRONMENT ############
ENV JAVA_OPTS -XX:MaxPermSize=256m -Xms256m -Xmx768m -Djava.net.preferIPv4Stack=true
ENV JBOSS_BIND_ADDRESS 0.0.0.0

ENV PROCESS_SERVER_ID process-server-1
ENV PROCESS_SERVER_VERSION 7.0.0-SNAPSHOT
ENV PROCESS_SERVER_WAR kie-server-1.0-SNAPSHOT.war

# ! ENV PROCESS_SERVER_CONTROLLER http://$BC_HOST:$BC_PORT/business-central/rest/controller

# ! ENV BUSINESS_CENTRAL_CONTROLLER_USER
# ! ENV BUSINESS_CENTRAL_CONTROLLER_PWD

# ENV TASK_INBOX_URL http://$BC_HOST:$BC_PORT/business-central
# ENV MAIL_FROM noreply@bpm.grid

ENV KEYCLOAK_VERSION 2.5.0.Final

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

####### SYSTEM USERS FOR DEVELOPMENT ############
USER root
# Set a password for root & jboss users (for any further system operations, etc)
RUN echo "root:rootman" | chpasswd && \
echo "jboss:jboss" | chpasswd

####### PROCESS SERVER & WILDFLY10 CONFIG FILES ############

RUN rm $JBOSS_HOME/standalone/configuration/standalone.xml && \
mv $JBOSS_HOME/standalone/configuration/standalone-full.xml $JBOSS_HOME/standalone/configuration/standalone.xml

ADD settings.xml $JBOSS_HOME/bin/settings.xml

# Install Database Driver and Datasource

ADD postgresql.jar $JBOSS_HOME/postgresql-driver.jar

ADD classes/config/add-datasource.cli $JBOSS_HOME/bin/add-datasource.cli
RUN $JBOSS_HOME/bin/jboss-cli.sh --file=$JBOSS_HOME/bin/add-datasource.cli

# Install Keycloak and secure Process Server

ADD classes/config/secure-deployment-offline.cli $JBOSS_HOME/bin/secure-deployment-offline.cli

RUN yum -y install wget && \
yum clean all -y && \
wget https://downloads.jboss.org/keycloak/${KEYCLOAK_VERSION}/adapters/keycloak-oidc/keycloak-wildfly-adapter-dist-${KEYCLOAK_VERSION}.zip -O $JBOSS_HOME/keycloak-adapter.zip && \
unzip -q -d $JBOSS_HOME/ $JBOSS_HOME/keycloak-adapter.zip && \
rm $JBOSS_HOME/keycloak-adapter.zip && \
$JBOSS_HOME/bin/jboss-cli.sh --file=$JBOSS_HOME/bin/adapter-install-offline.cli && \
$JBOSS_HOME/bin/jboss-cli.sh --file=$JBOSS_HOME/bin/secure-deployment-offline.cli

# Add Process Server
ADD classes/config/start-process-server.sh $JBOSS_HOME/bin/start-process-server.sh
ADD ${PROCESS_SERVER_WAR} $JBOSS_HOME/standalone/deployments/kie-server-archive.war

RUN unzip -q -d $JBOSS_HOME/standalone/deployments/kie-server.war $JBOSS_HOME/standalone/deployments/kie-server-archive.war && \
touch $JBOSS_HOME/standalone/deployments/kie-server.war.dodeploy && \
rm $JBOSS_HOME/standalone/deployments/kie-server-archive.war

ADD classes/config/logback.xml $JBOSS_HOME/standalone/deployments/kie-server.war/WEB-INF/classes/logback.xml

# Add Process Server extensions
ADD process-server-1.0-SNAPSHOT.jar $JBOSS_HOME/standalone/deployments/kie-server.war/WEB-INF/lib/process-server.jar

# Integrate Process Server with Authentication Server
ADD classes/config/jbpm.user.info.properties $JBOSS_HOME/standalone/deployments/kie-server.war/WEB-INF/classes/jbpm.user.info.properties

# Set right permissions for jboss user.
RUN chown -R jboss:jboss $JBOSS_HOME/* && \
chmod +x $JBOSS_HOME/bin/*.sh

# Switchback to jboss user
USER jboss

####### COMMAND ############
WORKDIR $JBOSS_HOME/bin/
CMD ["./start-process-server.sh"]
