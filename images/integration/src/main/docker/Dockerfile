####### BASE ############
FROM jboss/base-jdk:8

####### MAINTAINER ############
MAINTAINER "Radovan Synek" "rsynek@redhat.com"

####### ENVIRONMENT ############
ENV JAVA_OPTS -Xms128m -Xmx128m -Djava.net.preferIPv4Stack=true

ENV INTEGRATION_JAR integration-1.0-SNAPSHOT.jar

# ! ENV PROCESS_SERVER_HOST
# ! ENV PROCESS_SERVER_PORT

# ! ENV PROCESS_SERVER_INTEGRATION_USER
# ! ENV PROCESS_SERVER_INTEGRATION_PWD

USER root

RUN groupadd -r integration && useradd -r -g integration integration

# Set a password for root & integration users (for any further system operations, etc)
RUN echo "root:rootman" | chpasswd && \
    echo "integration:integration" | chpasswd

USER integration

COPY ${INTEGRATION_JAR} /home/integration/cbg-integration.jar

####### COMMAND ############
WORKDIR /home/integration
#CMD ["./start-integration.sh"]
CMD ["java", "-jar", "cbg-integration.jar", "/etc/camel-context.xml"]
