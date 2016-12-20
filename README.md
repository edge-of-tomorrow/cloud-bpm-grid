# Cloud BPM Grid
Cloud BPM Grid is a platform which integrates latest JBoss community projects in order to provide "complete" BPM solutions for building prototypes, experimenting with the latest features, verifying the fixed issues and for day to day usage.

Solutions:

1. **Authoring** - Business Central + BPM Assets + Authentication Server
2. **Runtime** - Process Server + Authentication Server + PostgreSQL DB
3. **Complete** - Authoring + Runtime

See all [out of the box features on wiki](https://github.com/edge-of-tomorrow/cloud-bpm-grid/wiki/Features).

## Env Setup

Prerequisites - Maven 3.2.5+, JDK 1.8, **Docker** (1.10.3)
```sh
export CBG_HOME=[path to the root of this repository]
export PATH=$PATH:$CBG_HOME/bin
```

Set all configuration in a single file ```conf/base.cfg```.

## Usage

cbg COMMAND [SOLUTION=all|authoring|runtime]

```sh
1. cbg setup
2. cbg build
3. cbg start
4. cbg stop
```

> Each server can be managed with its script (e.g. PostgreSQL database via \$CBG_HOME/bin/cbg-database).

> To get status of the platform execute ```cbg status```

## Architecture

Component             | Project
--------------------- | ---------------------------------------
artifact-repository   | Nexus 3 latest Docker image
database              | PostgreSQL latest Docker image
authentication-server | KeyCloak latest Docker image 
bpm-assets            | Git repositories
business-central      | JBpm Workbench in Wildfly Docker image
process-server        | JBpm Kie Server in Wildfly Swarm

![Cloud BPM Grid Architecture](https://github.com/edge-of-tomorrow/cloud-bpm-grid/raw/master/doc/architecture.png "Cloud BPM Grid Architecture")
