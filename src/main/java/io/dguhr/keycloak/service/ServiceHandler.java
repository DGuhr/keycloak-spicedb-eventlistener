package io.dguhr.keycloak.service;

import io.dguhr.keycloak.event.SpiceDbTupleEvent;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;

public abstract class ServiceHandler {

    protected final KeycloakSession session;
    protected final Config.Scope config;

    public ServiceHandler(KeycloakSession session, Config.Scope config ) {
        this.session = session;
        this.config = config;
    }

    public abstract void handle(String eventID, SpiceDbTupleEvent eventValue);

    public abstract void validateConfig();

    public void close() {
        // close this instance of the event listener
    }

}
