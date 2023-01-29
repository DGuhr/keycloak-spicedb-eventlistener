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

import java.util.concurrent.TimeUnit;

public class SpiceDbServiceHandler extends ServiceHandler {
    private static final Logger logger = Logger.getLogger(SpiceDbServiceHandler.class);

    public SpiceDbServiceHandler(KeycloakSession session, Config.Scope config) { //TODO : refactor getOrCreate - called everytime, should only once.
        super(session, config);
        getOrCreateSchema();
    }

    @Override
    public void handle(String eventID, SpiceDbTupleEvent sdbEvent) {
        if(sdbEvent.getOperation().equals(EventOperation.NOT_HANDLED)) {
            logger.info("HANDLE:: not handling.");
        }

        if(sdbEvent.getOperation().equals(EventOperation.ADDGROUPMEMBER)) {
            logger.info("HANDLE:: Add Group member..");
            addGroupMember(sdbEvent);
        }

        if(sdbEvent.getOperation().equals(EventOperation.ADDUSER)) {
            logger.info("HANDLE:: add user..");
            addUser(sdbEvent);
        }

        if(sdbEvent.getOperation().equals(EventOperation.ADDGROUP)) {
            logger.info("HANDLE:: add group..");
            addGroup(sdbEvent);
        }
    }

    private void addGroup(SpiceDbTupleEvent sdbEvent) {
        writeSpiceDbRelationship(sdbEvent);
    }

    private void addUser(SpiceDbTupleEvent sdbEvent) {
       writeSpiceDbRelationship(sdbEvent);
    }

    private void addGroupMember(SpiceDbTupleEvent sdbEvent) {
        writeSpiceDbRelationship(sdbEvent);
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
                writeSchema(schemaService, getInitialSchema());
            } else {
                throw new RuntimeException("connection to spicedb not available.");
            }
            return getOrCreateSchema();
        } finally {
            channel.shutdown();
            try {
                if (!channel.awaitTermination(3000, TimeUnit.MILLISECONDS)) {
                    channel.shutdownNow();
                }
            } catch (InterruptedException e) {
                channel.shutdownNow();
            }
        }
        logger.info("Scheme found.");
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
                "    relation member : principal\n" +
                "    relation tenant_admin : principal\n" +
                "    permission admin = tenant_admin\n" +
                "}\n" +
                "\n" +
                "definition group {\n" +
                "    relation parent : tenant | group\n" +
                "    relation direct_member : principal\n" +
                "    relation group_admin : principal\n" +
                "\n" +
                "    permission member = direct_member + group_admin + parent->admin\n" +
                "    permission admin = group_admin + parent->admin\n" +
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
                .forTarget(config.get("spicedbHost") + ":" + config.get("spicedbPort"))
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
            logger.info("Trying to write this stuff now...");
            writeRelationResponse = permissionService.writeRelationships(req);
        } catch (Exception e) {
            logger.warn("WriteRelationshipsRequest failed: ", e);
            return "";
        } finally {
            channel.shutdown();
            try {
                if (!channel.awaitTermination(3000, TimeUnit.MILLISECONDS)) {
                    channel.shutdownNow();
                }
            } catch (InterruptedException e) {
                channel.shutdownNow();
            }
        }

        logger.info("writeRelationshipResponse: " + writeRelationResponse);
        return writeRelationResponse.getWrittenAt().getToken();
    }
    @Override
    public void validateConfig() {
        //not implemented yet
    }
}
