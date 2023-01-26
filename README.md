# keycloak-spicedb-eventlistener
An event listener for Keycloak, creating spiceDB relationship data for keycloak users and groups by listening on the events in keycloak and using the spiceDB java client.

Inspired by [this](https://github.com/embesozzi/keycloak-openfga-event-listener) implementation for openFGA

:warning::warning::warning:
**warning** 
This is a highly experimental WIP PoC for now, so use at your own risk and definitely nowhere near production. It may likely be that it gets abandoned shortly. :warning: :warning: :warning:


# try it out:

1) mvn clean install
2docker compose up
2) wait until the script runs (usera are automatically proivisioned)
3) use e.g. zed (the spicedb command line tool) to connect to the spiceDB instance and see that relations are written containing the username (form: userid_username) -> `zed context set first-dev-context :50051 "abcdefgh" --insecure` followed by `zed relationship read tenant` should output 3 members of 2 tenants, as defined in `initialize-poc.sh`


# TODO:
1) create groups event, group membership event.
2) much more. as said highly experimental ;)
