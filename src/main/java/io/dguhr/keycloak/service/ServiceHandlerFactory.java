package io.dguhr.keycloak.service;

import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;

public class ServiceHandlerFactory {

    public static ServiceHandler create(String serviceName, KeycloakSession session, Config.Scope config){
        switch (serviceName) {
            case ("FILE"):
                return new FileServiceHandler(session, config);
            case ("SPICEDB"): {
                return new SpiceDbServiceHandler(session, config);
            }
            case ("KAFKA"):
                throw new IllegalArgumentException("This service has not been implemented yet... " + serviceName);
            case ("HTTP_CLIENT"):
                throw new IllegalArgumentException("This service has not been implemented yet... " + serviceName);
            default:
                throw new IllegalArgumentException("The service " + serviceName + " is not implemented");
        }
    }
}
