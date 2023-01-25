package io.dguhr.keycloak.service;

import io.dguhr.keycloak.event.EventOperation;
import io.dguhr.keycloak.event.SpiceDbTupleEvent;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;

public class SpiceDbServiceHandler extends ServiceHandler {

    public SpiceDbServiceHandler(KeycloakSession session, Config.Scope config) {
        super(session, config);
    }

    @Override
    public void handle(String eventID, SpiceDbTupleEvent sdbEvent) {
        if(sdbEvent.getOperation().equals(EventOperation.ADDGROUPMEMBER)) {
            addGroupMember();
        }
        if(sdbEvent.getOperation().equals(EventOperation.ADDUSER)) {
            addUser();
        }

        if(sdbEvent.getOperation().equals(EventOperation.ADDGROUP)) {
            addGroup();
        }
    }

    private void addGroup() {

        //TODO
    }

    private void addUser() {
        //TODO
    }

    private void addGroupMember() {
        //TODO
    }

    @Override
    public void validateConfig() {
        //not implemented yet
    }
}
