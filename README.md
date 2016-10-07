# Cloud BPM Grid

Solutions:

1. Authoring - Business Central + BPM Assets + Authentication Server
2. Runtime - Process Server + Authentication Server + PostgreSQL DB
3. Complete - Authoring + Process Server + Authentication Server + PostgreSQL DB

cbg build authoring
cbg start runtime
cbg restart authoring
cbg rebuild all
cbg stop
cbg clear
cbg start process-server ps2 8585

## Setup

export CBG_HOME=[path to the root of this repository]

