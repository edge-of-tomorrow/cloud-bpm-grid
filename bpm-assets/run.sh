#!/bin/sh

# Clone repositories which should be added to Business Central

INIT_DIR=init
if [ ! -d "$INIT_DIR" ]; then
    mkdir -m 777 $INIT_DIR
    git clone git+ssh://code.engineering.redhat.com/bpms-qe-assets.git init/bpms-qe-assets
else
    cd init/bpms-qe-assets
    git pull
fi

