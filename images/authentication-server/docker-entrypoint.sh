#!/bin/bash

if [ $KEYCLOAK_USER ] && [ $KEYCLOAK_PASSWORD ]; then
    keycloak/bin/add-user-keycloak.sh --user $KEYCLOAK_USER --password $KEYCLOAK_PASSWORD
fi

exec /opt/jboss/keycloak/bin/standalone.sh -Djavax.net.ssl.trustStore=/opt/jboss/ldap.truststore -Djavax.net.ssl.trustStorePassword=$LDAP_TRUSTSTOREPWD $@
exit $?
