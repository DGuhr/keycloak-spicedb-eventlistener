#!/bin/bash

num_users=$1
org_ids=("$(cat /dev/urandom | tr -dc '0-9' | fold -w 8 | head -n 1)" "$(cat /dev/urandom | tr -dc '0-9' | fold -w 8 | head -n 1)" "$(cat /dev/urandom | tr -dc '0-9' | fold -w 8 | head -n 1)")

for i in $(seq 1 $num_users)
do
  username="user$i"
  firstName="First$i"
  lastName="Last$i"
  email="$username@demo.com"
  org_id=${org_ids[$((RANDOM % 3))]}
  password=$(cat /dev/urandom | tr -dc 'a-zA-Z0-9' | fold -w 12 | head -n 1)

  /opt/keycloak/bin/kcadm.sh create users -r master -s username=$username -s firstName=$firstName -s lastName=$lastName -s enabled=true -s email=$email -s emailVerified=true -s "attributes.org_id=$org_id"
  /opt/keycloak/bin/kcadm.sh set-password -r master --username $username --new-password $password
  eval "username$i=$username"
done

echo "Added $num_users users."
