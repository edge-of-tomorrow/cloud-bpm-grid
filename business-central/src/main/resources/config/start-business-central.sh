#!/usr/bin/env bash

# Configure the connection to Authentication Server
#if [ -n "${AUTHENTICATION_SERVER+set}" ]; then
    ./jboss-cli.sh --file=adapter-install-offline.cli
    # Filter env properties and secure deployment
    #TODO
    ./jboss-cli.sh --file=secure-deployment-offline.cli
#fi

# Start Business Central
./standalone.sh -b $JBOSS_BIND_ADDRESS --server-config=standalone.xml
exit $?
