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

    public SpiceDbServiceHandler(KeycloakSession session, Config.Scope config) {
        super(session, config);
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
