#!/bin/sh

# ~18s for the service to launch
# admin:admin123
# docker logs -f artifact-repository
# http://localhost:8282/

ARTIFACT_REPOSITORY=~/.artifact-repository
if [ ! -d "$ARTIFACT_REPOSITORY" ]; then
    mkdir $ARTIFACT_REPOSITORY && sudo chown -R 200 $ARTIFACT_REPOSITORY
fi

docker rm -f artifact-repository
docker run -d -p 8282:8081 --name artifact-repository -v $ARTIFACT_REPOSITORY:/nexus-data sonatype/nexus3
