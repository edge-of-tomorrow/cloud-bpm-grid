#!/bin/sh

# TODO: change --net="host" but we need to access the execution server (make it a docker image as well?)
# then, add  --link artifact-repository:arepo

BPM_ASSETS=/home/ibek/Work/github/cloud-bpm-grid/bpm-assets/niogit
#if [ ! -d "$BPM_ASSETS" ]; then
    cd ../bpm-assets
    ./run.sh
    cd ../business-central
#fi

docker rm -f business-central
docker run -d -p 8080:8080 --net="host" --name business-central -v $BPM_ASSETS:/opt/jboss/wildfly/bin/.niogit kie/business-central
