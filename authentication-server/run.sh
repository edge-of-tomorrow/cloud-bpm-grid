#!/bin/sh

docker rm -f authentication-server
docker run -d -p 8383:8080 --link postgres:postgres -e KEYCLOAK_USER=ibek -e KEYCLOAK_PASSWORD=ibek1234! --name authentication-server jboss/keycloak-postgres
