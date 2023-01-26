# keycloak-spicedb-eventlistener
An event listener for Keycloak, creating spiceDB relationship data for keycloak users and groups by listening on the events in keycloak and using the spiceDB java client.

Inspired by [this](https://github.com/embesozzi/keycloak-openfga-event-listener) implementation for openFGA

:warning::warning::warning:
**warning** 
This is a highly experimental WIP PoC for now, so use at your own risk and definitely nowhere near production. It may likely be that it gets abandoned shortly. :warning: :warning: :warning:


# try it out:

1) mvn clean install
2) docker compose up
3) wait until the custom entrypoint script runs (users are automatically proivisioned using kcadm) - watch the logs :)
3) use e.g. [zed](https://github.com/authzed/zed) (the spicedb command line tool) to connect to the spiceDB instance and see that relations are written containing the username (form: userid_username) -> `zed context set first-dev-context :50051 "abcdefgh" --insecure` followed by `zed relationship read tenant` should output 3 members of 2 tenants, as defined in `initialize-poc.sh`


# TODO:
1) write "create group" events, "group membership" events and more. Should be relatively straight-forwards now.
2) much more. as said highly experimental ;)
