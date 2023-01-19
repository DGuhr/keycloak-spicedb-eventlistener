package io.dguhr.keycloak;

import io.dguhr.keycloak.service.ServiceHandler;
import io.dguhr.keycloak.service.ServiceHandlerFactory;
import org.keycloak.Config.Scope;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class SpiceDbEventListenerProviderFactory implements EventListenerProviderFactory {

	private static final String PROVIDER_ID = "spicedb-events";
	private SpiceDbEventListenerProvider instance;
	private String serviceHandlerName;
	private Scope config;

	@Override
	public EventListenerProvider create(KeycloakSession session) {
		if (instance == null) {
			ServiceHandler serviceHandler = ServiceHandlerFactory.create(serviceHandlerName, session, config);
			serviceHandler.validateConfig();
			instance = new SpiceDbEventListenerProvider(serviceHandler, session);
		}
		return instance;
	}

	@Override
	public String getId() {
		return PROVIDER_ID;
	}

	@Override
	public void init(Scope config) {
		this.serviceHandlerName = config.get("serviceHandlerName");
		if (serviceHandlerName == null) {
			throw new NullPointerException("Service handler name must not be null.");
		}

		this.config = config;
	}

	@Override
	public void postInit(KeycloakSessionFactory ksf) {
		// ignore
	}

	@Override
	public void close() {
		// ignore
	}
}
