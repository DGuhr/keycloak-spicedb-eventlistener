# keycloak-spicedb-eventlistener
An event listener for Keycloak, creating spiceDB relationship data for keycloak users and groups by listening on the events in keycloak and using the spiceDB java client.

Inspired by [this](https://github.com/embesozzi/keycloak-openfga-event-listener) implementation for openFGA

**warning** 
This is a highly experimental WIP PoC for now, so use at your own risk and definitely nowhere near production. It may likely be that it gets abandoned shortly. :warning:


# try it out:

1) mvn clean install
2) docker build . -t dguhr/keycloak_spicedbtest
3) docker compose up

4) create users and groups in keycloak
5) go to realm settings -> events and activate 'spicedb-events'
6) add users to groups. 
7) use e.g. zed (the spicedb command line tool) to connect to the spiceDB instance and see that relations are written containing the username (form: userid_username)