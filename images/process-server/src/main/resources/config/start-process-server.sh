#!/usr/bin/env bash

if [ -n "$DOCKER_IP" ] && [ -n "$DOCKER_PORT" ]; then
    PROCESS_SERVER_HOST=$DOCKER_IP
    PROCESS_SERVER_PORT=$DOCKER_PORT
fi

if [ -n "$LOGGER_PACKAGE" ] && [ -n "$LOGGER_LEVEL" ]; then
    cat <<EOF > add-logger.cli
embed-server --server-config=standalone.xml
/subsystem=logging/logger=$LOGGER_PACKAGE:add
/subsystem=logging/logger=$LOGGER_PACKAGE:write-attribute(name="level", value="$LOGGER_LEVEL")
EOF
    ./jboss-cli.sh --file=$JBOSS_HOME/bin/add-logger.cli
fi

# Start Process Server
./standalone.sh -b $JBOSS_BIND_ADDRESS --server-config=standalone.xml -Dorg.kie.server.id=$PROCESS_SERVER_ID -Dorg.kie.server.location=http://$PROCESS_SERVER_HOST:$PROCESS_SERVER_PORT/kie-server/services/rest/server -Dorg.kie.server.controller=$PROCESS_SERVER_CONTROLLER -Dorg.kie.server.controller.user=$BUSINESS_CENTRAL_CONTROLLER_USER -Dorg.kie.server.controller.pwd=$BUSINESS_CENTRAL_CONTROLLER_PWD -Dkie.maven.settings.custom=$JBOSS_HOME/bin/settings.xml -Dorg.kie.task.inbox.url=$TASK_INBOX_URL -Dorg.kie.mail.from=$MAIL_FROM -Dorg.kie.mail.session=java:jboss/mail/Default -Dorg.jbpm.ht.userinfo=db -Dorg.kie.server.persistence.dialect=org.hibernate.dialect.PostgreSQLDialect -Dorg.kie.server.persistence.ds=java:jboss/datasources/PostgreDS -Djboss.socket.binding.port-offset=101 -DDB_NAME=$DB_NAME -DDB_USER=$DB_USER -DDB_PWD=$DB_PWD -DDB_HOST=$DB_HOST -DDB_PORT=$DB_PORT
exit $?
