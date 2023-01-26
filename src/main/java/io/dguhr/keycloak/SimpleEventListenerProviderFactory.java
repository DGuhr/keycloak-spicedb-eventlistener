package io.dguhr.keycloak;

import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class SimpleEventListenerProviderFactory implements EventListenerProviderFactory {

    private KeycloakSession session;
    private volatile SimpleEventListenerProvider instance;


    public SimpleEventListenerProviderFactory() {}

    @Override
    public EventListenerProvider create(KeycloakSession session) {
        SimpleEventListenerProvider provider = instance;
        if (provider != null) {
            return provider;
        }

        provider = new SimpleEventListenerProvider(session);
        instance = provider;

        return provider;
    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return "test-events";
    }
}
