package io.dguhr.keycloak.event;

import com.authzed.api.v1.Core;
import com.authzed.api.v1.PermissionService;
import com.authzed.api.v1.PermissionsServiceGrpc;
import com.authzed.api.v1.SchemaServiceGrpc;
import com.authzed.api.v1.SchemaServiceOuterClass;
import com.authzed.grpcutil.BearerToken;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.ManagedChannelBuilder;
import org.jboss.logging.Logger;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import io.grpc.ManagedChannel;
import org.keycloak.models.UserModel;

public class SpiceDbEventParser {

    public static final String EVT_RESOURCE_USERS = "users";
    public static final String EVT_RESOURCE_GROUPS = "groups";
    public static final String EVT_RESOURCE_ROLES_BY_ID = "roles-by-id";
    public static final String OBJECT_TYPE_USER = "user";
    public static final String OBJECT_TYPE_ROLE = "role";
    public static final String OBJECT_TYPE_GROUP = "group";

    private AdminEvent event;
    private KeycloakSession session;

    private static final Logger logger = Logger.getLogger(SpiceDbEventParser.class);

    public SpiceDbEventParser(AdminEvent event, KeycloakSession session) {
        this.event = event;
        this.session = session;
    }

    /***
     * Convert the Keycloak event to Event Tuple following the OpenFGA specs
     * The OpenFGA authorization model is more complex, nevertheless, here is a simplified version of the Authorization Model that fit our requirements'
     * role
     *   |_ assignee     --> user   == Keycloak User Role Assignment
     *   |_ parent_group --> group  == Keycloak Group Role Assignment
     *   |_ parent       --> role   == Keycloak Role to Role Assignment
     *  group
     *   |_ assignee     --> user   == Keycloak User Group Role Assignment
     */
    public SpiceDbTupleEvent toTupleEvent() {
        if(getEventOperation().equals("")) {
            return null;
        }

        // Get all the required information from the KC event
        String evtObjType = getEventObjectType();
        String evtUserType = getEventUserType(); //rm
        String evtUserId = evtUserType.equals(OBJECT_TYPE_ROLE) ? findRoleNameInRealm(getEventUserId()) : getEventUserId(); //rm
        String evtObjectId = getEventObjectName();
        String evtOrgId = getOrgIdByUserId(evtUserId);

        logger.info("[SpiceDbEventListener] TYPE OF EVENT IS: " + event.getResourceTypeAsString());
        logger.info("[SpiceDbEventListener] ORG ID FOR USER IN EVENT IS: " + evtOrgId);
        logger.info("[SpiceDbEventListener] EVENTS definition IS: " + evtObjType);
        //logger.info("[SpiceDbEventListener] EVENTS user type IS: " + evtUserType);
        logger.info("[SpiceDbEventListener] EVENTS user ID IS: " + evtUserId);
        logger.info("[SpiceDbEventListener] EVENTS group value ID IS: " + evtObjectId);
        logger.info("[SpiceDbEventListener] EVENT representation is: " + event.getRepresentation());

        //TODO use the spicedb client
        // Check if the type (objectType) and object (userType) is present in the authorization model
        // So far, every relation between the type and the object is UNIQUE
        // perhaps add this check and create it using the API if not exist?
        //ObjectRelation objectRelation = model.filterByType(evtObjType).filterByObject(evtUserType);

        ManagedChannel channel = ManagedChannelBuilder
                .forTarget("host.docker.internal:50051") // TODO: create local setup and make it configurable
                .usePlaintext() // if not using TLS, replace with .usePlaintext()
                .build();

        SchemaServiceGrpc.SchemaServiceBlockingStub schemaService = SchemaServiceGrpc.newBlockingStub(channel)
                .withCallCredentials(new BearerToken("12345"));
        String schema = getInitialSchema();

        PermissionsServiceGrpc.PermissionsServiceBlockingStub permissionService = PermissionsServiceGrpc.newBlockingStub(channel)
                .withCallCredentials(new BearerToken("12345")); //TODO configurable

        SchemaServiceOuterClass.ReadSchemaResponse schemaResponse = getOrCreateSchema(schemaService); //TODO refactor, maybe idempotent updates are a thing

        UserModel user = getUserByUserId(evtUserId);

        //return writeSpiceDbRelationship(evtUserId, evtObjectId, permissionService, user);
        return new SpiceDbTupleEvent(); //TODO: untangle. for users and groups, based on the kc event.

    }

    private static String writeSpiceDbRelationship(String evtUserId, String evtObjectId, PermissionsServiceGrpc.PermissionsServiceBlockingStub permissionService, UserModel user) {
        PermissionService.WriteRelationshipsRequest req = PermissionService.WriteRelationshipsRequest.newBuilder().addUpdates(
                        Core.RelationshipUpdate.newBuilder()
                                .setOperation(Core.RelationshipUpdate.Operation.OPERATION_CREATE)
                                .setRelationship(
                                        Core.Relationship.newBuilder()
                                                .setResource(
                                                        Core.ObjectReference.newBuilder()
                                                                .setObjectType("thelargeapp/group")
                                                                .setObjectId(evtObjectId)
                                                                .build())
                                                .setRelation("direct_member")
                                                .setSubject(
                                                        Core.SubjectReference.newBuilder()
                                                                .setObject(
                                                                        Core.ObjectReference.newBuilder()
                                                                                .setObjectType("thelargeapp/user")
                                                                                .setObjectId(evtUserId + "_" + user.getUsername())
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

    private static SchemaServiceOuterClass.ReadSchemaResponse getOrCreateSchema(SchemaServiceGrpc.SchemaServiceBlockingStub schemaService) {
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
            }
            return getOrCreateSchema(schemaService);

        }
        logger.info("Scheme found: " + readResponse.getSchemaText());
        return readResponse;
    }

    private static String writeSchema(SchemaServiceGrpc.SchemaServiceBlockingStub schemaService, String schema) {
        SchemaServiceOuterClass.WriteSchemaRequest request = SchemaServiceOuterClass.WriteSchemaRequest
                .newBuilder()
                .setSchema(schema)
                .build();

        SchemaServiceOuterClass.WriteSchemaResponse writeSchemaResponse;
        try {
            writeSchemaResponse = schemaService.writeSchema(request);
        } catch (Exception e) {
            logger.warn("WriteSchemaRequest failed", e);
            throw new RuntimeException(e);
        }
        logger.info("writeSchemaResponse: " + writeSchemaResponse.toString());
        return null;
    }

    /**
     * Checks for group_membership events.
     *
     * @return object type or error
     */
    public String getEventObjectType() {
        switch (event.getResourceType()) {
            //remove roles from the game for now. TODO: check if wanted.
            //case user: write user relation to spicedb. at best when assuming org_id = context then write orgid/user:value
            /*case REALM_ROLE_MAPPING:
            case REALM_ROLE:
                return OBJECT_TYPE_ROLE;*/
            case USER:
                return OBJECT_TYPE_USER; //TODO
            case GROUP_MEMBERSHIP:
                return OBJECT_TYPE_GROUP;
            default:
                logger.info("Event is not handled, id:" + event.getId() + " resource name: " + event.getResourceType().name());
                return "";
        }
    }

    public String getOrgIdByUserId(String userId) {
        logger.info("Searching org_id for userId: " + userId);
        String orgId = getUserByUserId(userId).getFirstAttribute("org_id");
        logger.info("Found org_id: " + orgId + " for userId: " + userId);
        return orgId;
    }

    /**
     * perhaps rename to getEventSubjectType?
     *
     * @return
     */
    public String getEventUserType() {
        switch (getEventResourceName()) {
            case EVT_RESOURCE_USERS:
                return OBJECT_TYPE_USER;
            case EVT_RESOURCE_GROUPS:
                return OBJECT_TYPE_GROUP;
            case EVT_RESOURCE_ROLES_BY_ID:
                return OBJECT_TYPE_ROLE;
            default:
                //throw new IllegalArgumentException("Resource type is not handled: " + event.getOperationType());
                logger.info("Event is not handled, id:" + event.getId() + " resource name: " + event.getResourceType().name());
                return "";
        }
    }

    /**
     * //TODO: eval + ext
     *
     * @return
     */
    public String getEventOperation() {
        switch (event.getOperationType()) {
            case CREATE:
                return "writes";
            case DELETE:
                return "deletes";
            default:
                logger.info("Event is not handled, id:" + event.getId() + " resource name: " + event.getResourceType().name());
                return "";
        }
    }

    public String getEventAuthenticatedUserId() {
        return this.event.getAuthDetails().getUserId();
    }

    public UserModel getUserByUserId(String userId) {
        return session.users().getUserById(session.getContext().getRealm(), userId);
    }

    public String getEventUserId() {
        return this.event.getResourcePath().split("/")[1];
    }

    public String getEventResourceName() {
        return this.event.getResourcePath().split("/")[0];
    }

    public Boolean isUserEvent() {
        return getEventResourceName().equalsIgnoreCase(EVT_RESOURCE_USERS);
    }

    public Boolean isRoleEvent() {
        return getEventResourceName().equalsIgnoreCase(EVT_RESOURCE_ROLES_BY_ID);
    }

    public Boolean isGroupEvent() {
        return getEventResourceName().equalsIgnoreCase(EVT_RESOURCE_GROUPS);
    }

    public String getEventObjectId() { //todo: generalize to use with different arguments in different strategy impls.
        return getObjectByAttributeName("id");
    }

    public String getEventObjectName() { //todo: generalize to use with different arguments in different strategy impls.
        return getObjectByAttributeName("name");
    }

    private String getObjectByAttributeName(String attributeName) { //TODO this does not work for every event type. users have "username" etc...
        ObjectMapper mapper = new ObjectMapper();
        String representation = event.getRepresentation().replaceAll("\\\\", "");
        try {
            JsonNode jsonNode = mapper.readTree(representation);
            if (jsonNode.isArray()) {
                return jsonNode.get(0).get(attributeName).asText();
            }
            return jsonNode.get(attributeName).asText();
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public String findRoleNameInRealm(String roleId) {
        logger.debug("Finding role name by role id: " + roleId);
        return session.getContext().getRealm().getRoleById(roleId).getName();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("AdminEvent resourceType=");
        sb.append(event.getResourceType());
        sb.append(", operationType=");
        sb.append(event.getOperationType());
        sb.append(", realmId=");
        sb.append(event.getAuthDetails().getRealmId());
        sb.append(", clientId=");
        sb.append(event.getAuthDetails().getClientId());
        sb.append(", userId=");
        sb.append(event.getAuthDetails().getUserId());
        sb.append(", ipAddress=");
        sb.append(event.getAuthDetails().getIpAddress());
        sb.append(", resourcePath=");
        sb.append(event.getResourcePath());
        if (event.getError() != null) {
            sb.append(", error=");
            sb.append(event.getError());
        }
        return sb.toString();
    }

    private static String getInitialSchema() {
        return "definition thelargeapp/group {\n" +
                "  relation direct_member: thelargeapp/user\n" +
                "  relation admin: thelargeapp/user\n" +
                "  permission member = direct_member + admin\n" +
                "}\n" +
                "definition thelargeapp/user {}";
    }
}
