package io.dguhr.keycloak;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dguhr.keycloak.service.ServiceHandler;
import io.dguhr.keycloak.event.SpiceDbEventParser;
import org.jboss.logging.Logger;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;

public class SpiceDbEventListenerProvider implements EventListenerProvider {
	private static final Logger LOG = Logger.getLogger(SpiceDbEventListenerProvider.class);
	private ObjectMapper mapper;
	private ServiceHandler service;
	private KeycloakSession session;

	public SpiceDbEventListenerProvider(ServiceHandler service, KeycloakSession session) {
		LOG.info("[SpiceDbEventListener] SpiceDbEventListenerProvider initializing...");
		this.service = service;
		this.session = session;
		mapper = new ObjectMapper();
	}

	@Override
	public void onEvent(Event event) {
		LOG.info("[SpiceDbEventListener] onEvent type: " + event.getType().toString());
		LOG.info("[SpiceDbEventListener] Discarding event...");
	}

	@Override
	public void onEvent(AdminEvent adminEvent, boolean includeRepresentation) {
		LOG.info("[SpiceDbEventListener] onEvent Admin received events");

		try {
			LOG.infof("[SpiceDbEventListener] admin event: " + mapper.writeValueAsString(adminEvent));
			SpiceDbEventParser spiceDbEventParser = new SpiceDbEventParser(adminEvent, session);
			LOG.infof("[SpiceDbEventListener] event received: " + spiceDbEventParser);
			service.handle(adminEvent.getId(), spiceDbEventParser.toTupleEvent());
		} catch (IllegalArgumentException e) {
			LOG.warn(e.getMessage());
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void close() {
		// ignore
	}
}
