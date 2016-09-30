#!/bin/sh

BASE=`pwd`

java -Dorg.kie.server.id=process-server-1 -Dorg.kie.server.location=http://localhost:8181/server -Dswarm.port.offset=101 -Dorg.kie.server.controller=http://localhost:8080/business-central/rest/controller -Dorg.kie.server.controller.user=communicator -Dorg.kie.server.controller.pwd=communicator1234! -Dkie.maven.settings.custom=$BASE/settings.xml -jar target/process-server-1.0-SNAPSHOT-swarm.jar
