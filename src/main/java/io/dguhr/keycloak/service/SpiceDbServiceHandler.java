package io.dguhr.keycloak.service;

import com.authzed.api.v1.Core;
import com.authzed.api.v1.PermissionService;
import com.authzed.api.v1.PermissionsServiceGrpc;
import com.authzed.api.v1.SchemaServiceGrpc;
import com.authzed.api.v1.SchemaServiceOuterClass;
import com.authzed.grpcutil.BearerToken;
import io.dguhr.keycloak.event.EventOperation;
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
        getOrCreateSchema();
    }

    @Override
    public void handle(String eventID, SpiceDbTupleEvent sdbEvent) {
        if(sdbEvent.equals(EventOperation.NOT_HANDLED)) {
            logger.info("HANDLE:: not handling.");
        }

        if(sdbEvent.getOperation().equals(EventOperation.ADDGROUPMEMBER)) {
            logger.info("HANDLE:: Add Group member..");
            addGroupMember();
        }

        if(sdbEvent.getOperation().equals(EventOperation.ADDUSER)) {
            logger.info("HANDLE:: add user..");
            addUser();
        }

        if(sdbEvent.getOperation().equals(EventOperation.ADDGROUP)) {
            logger.info("HANDLE:: add user..");
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
                .forTarget(config.get("spicedbHost") +":"+config.get("spicedbPort"))
                .usePlaintext()
                .build();

        SchemaServiceGrpc.SchemaServiceBlockingStub schemaService = SchemaServiceGrpc.newBlockingStub(channel)
                .withCallCredentials(new BearerToken(config.get("spicedbToken")));

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
                "    permission member = assigned_group->member\n" +
                "}";
    }

    private String writeSpiceDbRelationship(SpiceDbTupleEvent sdbEvent) {

        ManagedChannel channel = ManagedChannelBuilder
                .forTarget(config.get("spicedbHost")+":"+config.get("spicedbPort"))
                .usePlaintext()
                .build();

        PermissionsServiceGrpc.PermissionsServiceBlockingStub permissionService = PermissionsServiceGrpc.newBlockingStub(channel)
                .withCallCredentials(new BearerToken(config.get("spicedbToken")));

        PermissionService.WriteRelationshipsRequest req = PermissionService.WriteRelationshipsRequest.newBuilder().addUpdates(
                        Core.RelationshipUpdate.newBuilder()
                                .setOperation(Core.RelationshipUpdate.Operation.OPERATION_CREATE)
                                .setRelationship(
                                        Core.Relationship.newBuilder()
                                                .setResource(
                                                        Core.ObjectReference.newBuilder()
                                                                .setObjectType(
                                                                        sdbEvent
                                                                                .getObject()
                                                                                .getObjectType())
                                                                .setObjectId(sdbEvent
                                                                        .getObject()
                                                                        .getObjectValue())
                                                                .build())
                                                .setRelation(sdbEvent
                                                        .getRelation()
                                                        .getRelationValue())
                                                .setSubject(
                                                        Core.SubjectReference.newBuilder()
                                                                .setObject(
                                                                        Core.ObjectReference.newBuilder()
                                                                                .setObjectType(
                                                                                        sdbEvent
                                                                                        .getSubject()
                                                                                        .getSubjectType())
                                                                                .setObjectId(
                                                                                        sdbEvent
                                                                                        .getSubject()
                                                                                        .getSubjectValue())
                                                                                .build())
                                                                .build())
                                                .build())
                                .build())
                .build();

        PermissionService.WriteRelationshipsResponse writeRelationResponse;
        try {
            writeRelationResponse = permissionService.writeRelationships(req);
        } catch (Exception e) {
            logger.warn("WriteRelationshipsRequest failed: ", e);
            return "";
        }
        logger.info("writeRelationResponse: " + writeRelationResponse);
        return writeRelationResponse.getWrittenAt().getToken();
    }
    @Override
    public void validateConfig() {
        //not implemented yet
    }
}
