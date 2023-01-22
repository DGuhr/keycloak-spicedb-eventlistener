#!/bin/bash

function main() {
    # Parameters
    /opt/keycloak/bin/initialize-poc.sh &

    # Launch base container entrypoint with container's runtime cmd arguments..."
    /opt/keycloak/bin/kc.sh --debug start-dev
}

main "$@"