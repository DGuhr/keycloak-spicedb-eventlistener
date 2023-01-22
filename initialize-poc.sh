#!/bin/bash

function wait_for_keycloak() {
  local -r MAX_WAIT=60
  local curl_request
  local host_url="http://localhost:8080/"
  local wait_time

  curl_request="curl -I -f -s ${host_url}"
  wait_time=0

  # Waiting for the application to return a 200 status code.
  until ${curl_request}; do
    if [[ ${wait_time} -ge ${MAX_WAIT} ]]; then
      echo "Keycloake did not start within ${MAX_WAIT} seconds. Aborting."
      exit 1
    else
      echo "Keycloak not started yet. Waiting (${wait_time}/${MAX_WAIT}) ..."
      sleep 1
      ((++wait_time))
    fi
  done

  echo "${host_url} is now up and running. Continuing to setup keycloak using kcadm.sh..."
}

# Waiting for Keycloak to start before proceeding with the configurations.
wait_for_keycloak

#then start kcadm.sh
echo "Connecting kcadm.sh..."

/opt/keycloak/bin/kcadm.sh config credentials --server http://localhost:8080 --realm master --user $KEYCLOAK_ADMIN --password $KEYCLOAK_ADMIN_PASSWORD

echo "Success! Now adding spicedb-events to master-realm..."
/opt/keycloak/bin/kcadm.sh update events/config -r master -s 'eventsListeners=["jboss-logging","spicedb-events"]'

echo "Success! Now adding users to master-realm including org_id field..."
# Users
/opt/keycloak/bin/kcadm.sh create users -r master -s username=paula -s firstName=Paula -s lastName=Von -s enabled=true -s email=paula@demo.com -s "attributes.org_id=12345"
/opt/keycloak/bin/kcadm.sh set-password -r master --username paula --new-password demo1234!
/opt/keycloak/bin/kcadm.sh create users -r master -s username=peter -s firstName=Peter -s lastName=Anderson -s enabled=true -s email=peter@demo.com -s "attributes.org_id=12345"
/opt/keycloak/bin/kcadm.sh set-password -r master --username peter --new-password demo1234!
/opt/keycloak/bin/kcadm.sh create users -r master -s username=richard  -s firstName=Richard -s lastName=Miles -s enabled=true -s email=richard@demo.com -s "attributes.org_id=23456"
/opt/keycloak/bin/kcadm.sh set-password -r master --username richard --new-password demo1234!