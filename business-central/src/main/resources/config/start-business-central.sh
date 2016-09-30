#!/usr/bin/env bash

# Start Business Central
./standalone.sh -b $JBOSS_BIND_ADDRESS --server-config=standalone-full-business-central.xml
exit $?
