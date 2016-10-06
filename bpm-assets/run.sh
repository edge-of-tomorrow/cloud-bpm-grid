#!/bin/sh

# Clone repositories which should be added to Business Central
# Optionally you can add hooks to automatically push commits to the origin repositories

NIOGIT_DIR=niogit
if [ -d "$NIOGIT_DIR" ]; then
    rm -rf $NIOGIT_DIR
fi

mkdir -m 777 $NIOGIT_DIR
git clone --mirror git+ssh://code.engineering.redhat.com/cloud-bpm-grid-system.git $NIOGIT_DIR/system.git
git clone --mirror git+ssh://code.engineering.redhat.com/bpms-qe-assets.git $NIOGIT_DIR/bpms-qe-assets.git

