#!/bin/sh

docker rm -f bpm-database
docker run -p 5432:5432 --name bpm-database -e POSTGRES_DB=bpmgrid -e POSTGRES_USER=dbuser -e POSTGRES_PASSWORD=dbuser1234! -e POSTGRES_ROOT_PASSWORD=rootman1234! -d postgres
