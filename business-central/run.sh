#!/bin/sh

# TODO: change --net="host" but we need to access the execution server (make it a docker image as well?)
# then, add  --link artifact-repository:arepo

docker rm -f business-central
docker run -p 8080:8080 --net="host" --name business-central -d kie/business-central
