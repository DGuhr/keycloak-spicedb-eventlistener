package io.dguhr.keycloak;

import org.jboss.logging.Logger;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.KeycloakSession;

public class SimpleEventListenerProvider implements EventListenerProvider {

    private KeycloakSession session;
    private static final Logger logger = Logger.getLogger(SimpleEventListenerProvider.class);

    public SimpleEventListenerProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void onEvent(Event event) {

    }

    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {
        if(event.getResourceType().equals(ResourceType.USER) && event.getOperationType().equals(OperationType.CREATE)) {
            var userId = event.getResourcePath().split("/")[1];
            var user = session.users().getUserById(session.getContext().getRealm(), userId);
            logger.info("Username: " + user.getUsername());
        }
    }

    @Override
    public void close() {

    }
}
