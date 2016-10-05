#!/bin/sh

docker rm -f authentication-server
docker run -d -p 8383:8080 --link bpm-database:postgres -e POSTGRES_DATABASE=bpmgrid -e POSTGRES_USER=dbuser -e POSTGRES_PASSWORD=dbuser1234! -e KEYCLOAK_USER=ibek -e KEYCLOAK_PASSWORD=ibek1234! --name authentication-server jboss/keycloak-postgres
