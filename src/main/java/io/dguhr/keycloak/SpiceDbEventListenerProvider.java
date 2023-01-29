package io.dguhr.keycloak;

import io.dguhr.keycloak.service.ServiceHandler;
import io.dguhr.keycloak.event.SpiceDbEventParser;
import org.jboss.logging.Logger;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.KeycloakSession;

public class SpiceDbEventListenerProvider implements EventListenerProvider {
	private static final Logger LOG = Logger.getLogger(SpiceDbEventListenerProvider.class);
	private ServiceHandler service;
	private KeycloakSession session;

	public SpiceDbEventListenerProvider(ServiceHandler service, KeycloakSession session) {
		LOG.info("[SpiceDbEventListener] SpiceDbEventListenerProvider initializing...");
		this.service = service;
		this.session = session;
	}

	@Override
	public void onEvent(Event event) {
		LOG.info("[SpiceDbEventListener] onEvent type: " + event.getType().toString());
		LOG.info("[SpiceDbEventListener] Discarding event...");
	}

	@Override
	public void onEvent(AdminEvent adminEvent, boolean includeRepresentation) {

		try {
			if (isHandledEvent(adminEvent)) {
			SpiceDbEventParser spiceDbEventParser = new SpiceDbEventParser(adminEvent, session);
			service.handle(adminEvent.getId(), spiceDbEventParser.toTupleEvent());
			}
		} catch (IllegalArgumentException e) {
			LOG.warn(e.getMessage());
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private boolean isHandledEvent(AdminEvent event) { //TODO remove methods from parser, reevaluate approach
		return event.getResourceType().equals(ResourceType.GROUP_MEMBERSHIP) && event.getOperationType().equals(OperationType.CREATE)
				|| event.getResourceType().equals(ResourceType.GROUP) && event.getOperationType().equals(OperationType.CREATE)
				|| event.getResourceType().equals(ResourceType.USER) && event.getOperationType().equals(OperationType.CREATE);
	}

	@Override
	public void close() {
		// ignore
	}
}
