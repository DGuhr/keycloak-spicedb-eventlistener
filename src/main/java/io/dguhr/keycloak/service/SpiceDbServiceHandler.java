package io.dguhr.keycloak.service;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;

public class SpiceDbServiceHandler extends ServiceHandler {

    public SpiceDbServiceHandler(KeycloakSession session, Config.Scope config) {
        super(session, config);
    }

    @Override
    public void handle(String eventID, String eventValue) {

    }

    @Override
    public void validateConfig() {
        //not implemented yet
    }
}
