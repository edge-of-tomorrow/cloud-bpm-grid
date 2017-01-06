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
# ! ENV PROCESS_SERVER_M2_SETTINGS

# ! ENV BUSINESS_CENTRAL_CONTROLLER_USER
# ! ENV BUSINESS_CENTRAL_CONTROLLER_PWD

# ENV TASK_INBOX_URL http://$BC_HOST:$BC_PORT/business-central
# ENV MAIL_FROM noreply@bpm.grid

ENV KEYCLOAK_VERSION 2.5.0.Final

# ! ENV DB_NAME
# ! ENV DB_USER
# ! ENV DB_PASSWORD
# ! ENV DB_HOST
ENV DB_PORT 5432
ENV DB_JNDI java:jboss/datasources/PostgreDS

EXPOSE 8080

####### SYSTEM USERS FOR DEVELOPMENT ############
USER root
# Set a password for root & jboss users (for any further system operations, etc)
RUN echo "root:rootman" | chpasswd && \
echo "jboss:jboss" | chpasswd

####### PROCESS SERVER & WILDFLY10 CONFIG FILES ############

# Install Database Driver and Datasource

ADD postgresql.jar $JBOSS_HOME/postgresql-driver.jar

ADD classes/config/add-datasource.cli $JBOSS_HOME/bin/add-datasource.cli
RUN $JBOSS_HOME/bin/jboss-cli.sh --file=$JBOSS_HOME/bin/add-datasource.cli

# Install Keycloak and secure Process Server

RUN yum -y install wget
RUN wget https://downloads.jboss.org/keycloak/${KEYCLOAK_VERSION}/adapters/keycloak-oidc/keycloak-wildfly-adapter-dist-${KEYCLOAK_VERSION}.zip -O $JBOSS_HOME/keycloak-adapter.zip

RUN unzip -q -d $JBOSS_HOME/ $JBOSS_HOME/keycloak-adapter.zip && \
rm $JBOSS_HOME/keycloak-adapter.zip

RUN $JBOSS_HOME/bin/jboss-cli.sh --file=$JBOSS_HOME/bin/adapter-install-offline.cli

ADD classes/config/secure-deployment-offline.cli $JBOSS_HOME/bin/secure-deployment-offline.cli
RUN $JBOSS_HOME/bin/jboss-cli.sh --file=$JBOSS_HOME/bin/secure-deployment-offline.cli

# Add Process Server
ADD ${PROCESS_SERVER_WAR} $JBOSS_HOME/standalone/deployments/kie-server-archive.war

RUN unzip -q -d $JBOSS_HOME/standalone/deployments/kie-server.war $JBOSS_HOME/standalone/deployments/kie-server-archive.war && \
touch $JBOSS_HOME/standalone/deployments/process-server.war.dodeploy && \
rm -rf $JBOSS_HOME/standalone/deployments/process-server-archive.war

ADD classes/config/logback.xml $JBOSS_HOME/standalone/deployments/kie-server.war/WEB-INF/classes/logback.xml

# Add Process Server extensions
ADD process-server-1.0-SNAPSHOT.jar $JBOSS_HOME/standalone/deployments/kie-server.war/WEB-INF/lib/process-server.jar

# Integrate Process Server with Authentication Server
ADD classes/config/jbpm.user.info.properties $JBOSS_HOME/deployments/kie-server.war/WEB-INF/classes/jbpm.user.info.properties

# Set right permissions for jboss user.
RUN chown -R jboss:jboss $JBOSS_HOME/standalone/deployments/* && \
chmod +x $JBOSS_HOME/bin/*.sh && \
chown jboss:jboss $JBOSS_HOME/bin/adapter-install-offline.cli && \
chown jboss:jboss $JBOSS_HOME/bin/secure-deployment-offline.cli && \
chown jboss:jboss $JBOSS_HOME/bin/add-datasource.cli

# Switchback to jboss user
USER jboss

####### COMMAND ############
WORKDIR $JBOSS_HOME/bin/
CMD ["./standalone.sh", "-b", "$JBOSS_BIND_ADDRESS", "-c", "standalone.xml", "-Dorg.kie.server.id", "$PROCESS_SERVER_ID", "-Dorg.kie.server.location", "http://$DOCKER_IP:$DOCKER_PORT/kie-server/services/rest/server", "-Dorg.kie.server.controller", "$PROCESS_SERVER_CONTROLLER", "-Dorg.kie.server.controller.user", "$BUSINESS_CENTRAL_CONTROLLER_USER", "-Dorg.kie.server.controller.pwd", "$BUSINESS_CENTRAL_CONTROLLER_PWD", "-Dkie.maven.settings.custom, "$PROCESS_SERVER_M2_SETTINGS", "-Dorg.kie.task.inbox.url", "$TASK_INBOX_URL", "-Dorg.kie.mail.from", "$MAIL_FROM", "-Dorg.kie.mail.session", "java:jboss/mail/Default", "-Dorg.jbpm.ht.userinfo", "db", "-Dorg.kie.server.persistence.dialect", "org.hibernate.dialect.PostgreSQLDialect", "-Dorg.kie.server.persistence.ds", "java:jboss/datasources/PostgreDS"]
