# keycloak-spicedb-eventlistener
An event listener for Keycloak, creating spiceDB relationship data for keycloak users and groups by listening on the events in keycloak and using the spiceDB java client.

Inspired by [this](https://github.com/embesozzi/keycloak-openfga-event-listener) implementation for openFGA

:warning::warning::warning:
**warning** 
This is a highly experimental WIP PoC for now, so use at your own risk and definitely nowhere near production. It may likely be that it gets abandoned shortly. :warning: :warning: :warning:


# try it out:

1) mvn clean install
2) docker compose up
3) wait until the custom entrypoint script runs (users are automatically provisioned using kcadm.sh) - watch the logs :)
3) use e.g. [zed](https://github.com/authzed/zed) (the spicedb command line tool) to connect to the spiceDB instance and see that relations are written containing the username (form: userid_username) -> `zed context set first-dev-context :50051 "abcdefgh" --insecure` followed by `zed relationship read tenant` should output 3 members of 2 tenants (12345, 23456), and `zed relationship read group` should show a <groupId>_<name> pair with parent tenant 12345 (derived from the creating user inside keycloak, added an org_id to the admin account as part of the script) as defined in `initialize-poc.sh`.


# TODO:
1) handle "group membership" event and more. Should be relatively straight-forwards now.
2) much more. as said highly experimental, dunno where to go with this, but it's fun ;)

# Done
1) extended to (experimentally) handle "add group" events