# Cloud BPM Grid
Cloud BPM Grid is a platform which integrates latest JBoss community projects in order to provide "complete" BPM solutions for building prototypes, experimenting with the latest features, verifying the fixed issues and for day to day usage.

Solutions:

1. **Authoring** - Business Central + BPM Assets + Authentication Server
2. **Runtime** - Process Server + Authentication Server + PostgreSQL DB
3. **Complete** (default profile) - Authoring + Runtime

See all [out of the box features on wiki](https://github.com/edge-of-tomorrow/cloud-bpm-grid/wiki/Features).

## Env Setup

Prerequisites - Maven 3.2.5+, JDK 1.8, **Docker** (1.10.3)
```sh
export CBG_HOME=[path to the root of this repository]
export PATH=$PATH:$CBG_HOME/bin
export CBG_PROFILE=default
```

Set all configuration in a single file ```$CBG_HOME/profiles/$CBG_PROFILE/conf/base.cfg```.

## Usage

cbg COMMAND [PROFILE=default]

```sh
1. cbg setup
2. cbg build
3. cbg start
4. cbg stop
```

> Each server can be managed with its script:

* Authentication Server via ```cbg-authentication-server```
* Business Central via ```cbg-business-central```
* Process Server via ```cbg-process-server```
* etc.

> To get status of the entire platform execute ```cbg status default```
```sh
>          Cloud BPM Grid [default]          <
artifact-repository      ready to start
database                 ready to start
authentication-server 	 ready to start
bpm-assets               ready to start
business-central         new (ready to build)
process-server           new (ready to build)
```

## Architecture

Component             | Project
--------------------- | ---------------------------------------
artifact-repository   | Nexus 3 latest Docker image
database              | PostgreSQL latest Docker image
authentication-server | KeyCloak latest Docker image 
bpm-assets            | Git repositories
business-central      | JBPM Workbench in Wildfly 10 Docker image
process-server        | JBPM Kie Server in Wildfly 10 Docker image

![Cloud BPM Grid Architecture](https://github.com/edge-of-tomorrow/cloud-bpm-grid/raw/master/doc/architecture.png "Cloud BPM Grid Architecture")

