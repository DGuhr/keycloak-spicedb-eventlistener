package io.dguhr.keycloak.service;

import com.authzed.api.v1.SchemaServiceGrpc;
import com.authzed.api.v1.SchemaServiceOuterClass;
import com.authzed.grpcutil.BearerToken;
import io.dguhr.keycloak.event.EventOperation;
import io.dguhr.keycloak.event.SpiceDbEventParser;
import io.dguhr.keycloak.event.SpiceDbTupleEvent;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;

public class SpiceDbServiceHandler extends ServiceHandler {
    private static final Logger logger = Logger.getLogger(SpiceDbServiceHandler.class);

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

    private SchemaServiceOuterClass.ReadSchemaResponse getOrCreateSchema() {

        ManagedChannel channel = ManagedChannelBuilder
                .forTarget("host.docker.internal:50051") // TODO: create local setup and make it configurable
                .usePlaintext() // if not using TLS, replace with .usePlaintext()
                .build();

        SchemaServiceGrpc.SchemaServiceBlockingStub schemaService = SchemaServiceGrpc.newBlockingStub(channel)
                .withCallCredentials(new BearerToken("12345"));

        SchemaServiceOuterClass.ReadSchemaRequest readRequest = SchemaServiceOuterClass.ReadSchemaRequest
                .newBuilder()
                .build();

        SchemaServiceOuterClass.ReadSchemaResponse readResponse;

        try {
            readResponse = schemaService.readSchema(readRequest);
        } catch (Exception e) {
            //ugly but hey..
            if(e.getMessage().contains("No schema has been defined")) {
                logger.warn("No scheme there yet, creating initial one.");
                logger.info(writeSchema(schemaService, getInitialSchema()));
            }
            return getOrCreateSchema();
        }
        logger.info("Scheme found: " + readResponse.getSchemaText());
        return readResponse;
    }

    private String writeSchema(SchemaServiceGrpc.SchemaServiceBlockingStub schemaService, String schema) {
        SchemaServiceOuterClass.WriteSchemaRequest request = SchemaServiceOuterClass.WriteSchemaRequest
                .newBuilder()
                .setSchema(schema)
                .build();

        SchemaServiceOuterClass.WriteSchemaResponse writeSchemaResponse;
        try {
            writeSchemaResponse = schemaService.writeSchema(request);
        } catch (Exception e) {
            logger.warn("Writing initial Schema failed!", e);
            throw new RuntimeException(e);
        }
        logger.info("writeSchemaResponse: " + writeSchemaResponse.toString());
        return writeSchemaResponse.toString();
    }

    private static String getInitialSchema() {
        return "definition principal {}\n" +
                "\n" +
                "definition tenant {\n" +
                "    relation member : principal \n" +
                "    relation admin : principal\n" +
                "}\n" +
                "\n" +
                "definition group {\n" +
                "    relation direct_member : principal\n" +
                "    relation group_admin : principal\n" +
                "\n" +
                "    permission member = direct_member + group_admin\n" +
                "    permission admin = group_admin\n" +
                "}\n" +
                "\n" +
                "definition role {\n" +
                "    relation assigned_group: group\n" +
                "\n" +
                "    permission member = assigned_group#member\n" +
                "}";
    }

    @Override
    public void validateConfig() {
        //not implemented yet
    }
}
