#!/bin/sh

BASE=`pwd`

PROCESS_SERVER_ID=process-server-1
PROCESS_SERVER_LOCATION=http://localhost:8181/kie-server/rest/server
PROCESS_SERVER_PORT=8181
PROCESS_SERVER_INMEM=true
PROCESS_SERVER_M2_SETTINGS=$BASE/settings.xml

BUSINESS_CENTRAL_URL=http://localhost:8080/business-central
BUSINESS_CENTRAL_CONTROLLER=$BUSINESS_CENTRAL_URL/rest/controller
BUSINESS_CENTRAL_CONTROLLER_USER=communicator
BUSINESS_CENTRAL_CONTROLLER_PWD=communicator1234!

DB_HOST=localhost
DB_PORT=5555
DB_NAME=bpmgrid
DB_USER=dbuser
DB_PWD=dbuser1234!

java -Dorg.kie.server.id=$PROCESS_SERVER_ID -Dorg.kie.server.location=$PROCESS_SERVER_LOCATION -Dswarm.http.port=$PROCESS_SERVER_PORT -Dorg.kie.server.controller=$BUSINESS_CENTRAL_CONTROLLER -Dorg.kie.server.controller.user=$BUSINESS_CENTRAL_CONTROLLER_USER -Dorg.kie.server.controller.pwd=$BUSINESS_CENTRAL_CONTROLLER_PWD -Dkie.maven.settings.custom=$PROCESS_SERVER_M2_SETTINGS -Dorg.kie.server.inmemory=$PROCESS_SERVER_INMEM -Dorg.kie.server.db.host=$DB_HOST -Dorg.kie.server.db.port=$DB_PORT -Dorg.kie.server.db.name=$DB_NAME -Dorg.kie.server.db.username=$DB_USER -Dorg.kie.server.db.password=$DB_PWD -jar target/process-server-1.0-SNAPSHOT-swarm.jar
