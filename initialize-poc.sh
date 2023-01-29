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

  echo "${host_url} is now up and running. Waiting 10s for a remote debugger to connect..."
  sleep 10

  echo "Continuing setup using kcadm.sh..."
}

# Waiting for Keycloak to start before proceeding with the configurations.
wait_for_keycloak

#then start kcadm.sh
echo "Connecting kcadm.sh to local keycloak instance..."

/opt/keycloak/bin/kcadm.sh config credentials --server http://localhost:8080 --realm master --user $KEYCLOAK_ADMIN --password $KEYCLOAK_ADMIN_PASSWORD

echo "Success! Now adding test-events to master-realm and initializing scheme..."
/opt/keycloak/bin/kcadm.sh update events/config -r master -s 'eventsListeners=["jboss-logging","spicedb-events"]'

sleep 4
echo "Success! Also waited 4 seconds to allow initializing the spiceDB schema."
# echo "Now adding users to master-realm including org_id field. Thank you, ChatGPT... :-D"
source /opt/keycloak/bin/usergenerator.sh 10

echo "Now adding an org/ tenant id to the admin, needed for creating groups that are tied to an org (we need the orgid of the creating person there)"

# adminUid, awk gets the plain id value from the get response, grep removes empty lines, tr -d removes doublequotes, sed removes leading whitespace.
# Bash magic? not for me ;) happy if anyone can simplify this (without installing external tools such as jq)
adminUid=$(/opt/keycloak/bin//kcadm.sh get users -r master -q username=admin --fields=id | awk -F':' '{print $2}' | grep . | tr -d "\"" | sed -e 's/^[[:space:]]*//')
/opt/keycloak/bin//kcadm.sh update users/$adminUid -s "attributes.org_id=12345"

echo "Now that we simulate an orgs admin, we can let them create a group that should be set up under the tenant 12345 derived by the admins orgid"
/opt/keycloak/bin/kcadm.sh create groups -r master -s name=MyGroup

echo "ok now lets add a user to the previously created group... in order to do that we need the userId and the groupId."

user1Uid=$(/opt/keycloak/bin//kcadm.sh get users -r master -q email=$username1@demo.com --fields=id | awk -F':' '{print $2}' | grep . | tr -d "\"" | sed -e 's/^[[:space:]]*//')
myGroupUid=$(/opt/keycloak/bin//kcadm.sh get groups -r master -q name=MyGroup --fields=id | awk -F':' '{print $2}' | grep . | tr -d "\"" | sed -e 's/^[[:space:]]*//')
/opt/keycloak/bin/kcadm.sh update users/$user1Uid/groups/$myGroupUid -r master -s realm=master -s userId=$user1Uid -s groupId=$myGroupUid -n

