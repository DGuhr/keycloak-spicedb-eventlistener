package io.dguhr.keycloak.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.logging.Logger;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;

/**
 * add user event representation
 * DEBUG [org.keycloak.events] (executor-thread-6)
 * operationType=CREATE,
 * realmId=a88ae7e5-bad3-459a-83c3-958a7098f584,
 * clientId=80463bd7-c175-486f-9796-6e271f29ebee,
 * userId=ff7d8d03-31c3-4c6b-8a6e-b1d82f96521c,
 * ipAddress=172.17.0.1,
 * resourceType=USER,
 * resourcePath=users/37c0245d-793e-4b46-b9e9-6e7502ef1347
 *
 * Add group event rep
 * DEBUG [org.keycloak.events] (executor-thread-7)
 * operationType=CREATE,
 * realmId=a88ae7e5-bad3-459a-83c3-958a7098f584,
 * clientId=80463bd7-c175-486f-9796-6e271f29ebee,
 * userId=ff7d8d03-31c3-4c6b-8a6e-b1d82f96521c,
 * ipAddress=172.17.0.1,
 * resourceType=GROUP,
 * resourcePath=groups/7ba3c729-a67b-4057-b38d-2f555512296b
 *
 * add user to group event rep
 * DEBUG [org.keycloak.events] (executor-thread-10)
 * operationType=CREATE,
 * realmId=a88ae7e5-bad3-459a-83c3-958a7098f584,
 * clientId=80463bd7-c175-486f-9796-6e271f29ebee,
 * userId=ff7d8d03-31c3-4c6b-8a6e-b1d82f96521c,
 * ipAddress=172.17.0.1,
 * resourceType=GROUP_MEMBERSHIP,
 * resourcePath=users/37c0245d-793e-4b46-b9e9-6e7502ef1347/groups/7ba3c729-a67b-4057-b38d-2f555512296b
 *
 * add subgroup event rep (name foo)
 * DEBUG [org.keycloak.events] (executor-thread-15)
 * operationType=CREATE,
 * realmId=a88ae7e5-bad3-459a-83c3-958a7098f584,
 * clientId=80463bd7-c175-486f-9796-6e271f29ebee,
 * userId=ff7d8d03-31c3-4c6b-8a6e-b1d82f96521c,
 * ipAddress=172.17.0.1,
 * resourceType=GROUP,
 * resourcePath=groups/7ba3c729-a67b-4057-b38d-2f555512296b/children
 *
 * add member to subgroup event rep (same as add to group)
 * DEBUG [org.keycloak.events] (executor-thread-18)
 * operationType=CREATE,
 * realmId=a88ae7e5-bad3-459a-83c3-958a7098f584,
 * clientId=80463bd7-c175-486f-9796-6e271f29ebee,
 * userId=ff7d8d03-31c3-4c6b-8a6e-b1d82f96521c,
 * ipAddress=172.17.0.1,
 * resourceType=GROUP_MEMBERSHIP,
 * resourcePath=users/ff7d8d03-31c3-4c6b-8a6e-b1d82f96521c/groups/57227efa-2d22-41c6-b0fe-574af3f9a919
 */
public class SpiceDbEventParser {

    private static final Logger logger = Logger.getLogger(SpiceDbEventParser.class);

    private final AdminEvent event;
    private final KeycloakSession session;


    public SpiceDbEventParser(AdminEvent event, KeycloakSession session) {
        this.event = event;
        this.session = session;
    }

    public SpiceDbTupleEvent toTupleEvent() {
        var currentTupleEvent = new SpiceDbTupleEventBuilder().build();
        var currentOperation = getEventOperation();

        if(currentOperation.equals(EventOperation.NOT_HANDLED)) {
            return currentTupleEvent.operation(currentOperation);
        }

        currentTupleEvent.operation(currentOperation);

        if (currentOperation.equals(EventOperation.ADDUSER)) {
            //subject: principal.
            //relation: member.
            //object: org.
            var userId = getUserIdFromResourcePath();
            var user = getUserByUserId(userId);
            var tenantId = getTenantIdForUser(user);
            currentTupleEvent
                    .orgId(tenantId)
                    .subject(new SpiceDbSubject()
                            .subjectType("principal")
                            .subjectValue(userId+"_"+user.getUsername()))
                    .relation(new SpiceDbRelation()
                            .relation("member"))
                    .object(new SpiceDbObject()
                            .objectType("tenant")
                            .objectValue(tenantId));
            return currentTupleEvent;
        }

        if (currentOperation.equals(EventOperation.ADDGROUP)) {
            //subject: tenant.
            //relation: parent.
            //object: group.
            var groupId = getGroupIdFromKcEvent();
            var groupName = getGroupNameFromKcEvent();

            var userId = getUserIdForGroupCreateEvent(); //here we need the creating users org id to add the group under that org.
            var user = getUserByUserId(userId);
            var tenantId = getTenantIdForUser(user);
            currentTupleEvent
                    .orgId(tenantId)
                    .subject(new SpiceDbSubject()
                            .subjectType("tenant")
                            .subjectValue(tenantId))
                    .relation(new SpiceDbRelation()
                            .relation("parent"))
                    .object(new SpiceDbObject()
                            .objectType("group")
                            .objectValue(groupId+"_"+groupName));
            return currentTupleEvent;
        }

        if(currentOperation.equals(EventOperation.ADDGROUPMEMBER)) {
            //subject: principal.
            //relation: direct_member.
            //object: group.
            var userId = getUserIdFromResourcePath();
            var user = getUserByUserId(userId);
            var orgId = getTenantIdForUser(user);
            var groupId = getGroupIdFromKcEvent();
            var groupName = getGroupNameFromKcEvent();
            currentTupleEvent
                    .orgId(orgId)
                    .subject(new SpiceDbSubject()
                            .subjectType("principal")
                            .subjectValue(groupId+"_"+groupName))
                    .relation(new SpiceDbRelation()
                            .relation("direct_member"))
                    .object(new SpiceDbObject()
                            .objectType("group")
                            .objectValue(orgId));
            return currentTupleEvent;
        }
        return currentTupleEvent.operation(EventOperation.NOT_HANDLED);
    }

    private GroupModel getGroupByGroupId(String groupId) {
        return session.groups().getGroupById(session.getContext().getRealm(), groupId);
    }

    public String getTenantIdForUser(UserModel user) {
        logger.info("Searching org_id for user: " + user.getUsername());
        String orgId = user.getFirstAttribute("org_id");

        if(orgId == null || orgId.isBlank()){
            orgId = "default";
        }

        return orgId;
    }

    /**
     * Returns the intended operation by evaluating the incoming keycloak event
     * @return the corresponding {@link EventOperation}, NOT_HANDLED indicates not handled (yet)
     */
    public EventOperation getEventOperation() {

        logger.info("received keycloak event with resourceType: " + event.getResourceTypeAsString() + " and operationType: " + event.getOperationType().toString());

        if (isAddUserKcEvent()) {
            return EventOperation.ADDUSER;
        }

        //Note: child groups would have to come here, essentially the same event as add user to group, but instead resourcePath starts with groups and ends with children.
        // look at later if needed...

        if (isCreateGroupKcEvent()) {
            return EventOperation.ADDGROUP;
        }

        if (isGroupMemberAddedEvent()) {
            return EventOperation.ADDGROUPMEMBER;
        }

        return EventOperation.NOT_HANDLED;
    }

    private boolean isGroupMemberAddedEvent() {
        return event.getResourceType().equals(ResourceType.GROUP_MEMBERSHIP) && event.getOperationType().equals(OperationType.CREATE);
    }

    private boolean isCreateGroupKcEvent() {
        return event.getResourceType().equals(ResourceType.GROUP) && event.getOperationType().equals(OperationType.CREATE);
    }

    private boolean isAddUserKcEvent() {
        return event.getResourceType().equals(ResourceType.USER) && event.getOperationType().equals(OperationType.CREATE);
    }

    public String getEventAuthenticatedUserId() {
        return this.event.getAuthDetails().getUserId();
    }

    public UserModel getUserByUserId(String userId) {
        return session.users().getUserById(session.getContext().getRealm(), userId);
    }

    public String getUserIdFromResourcePath() {
        return this.event.getResourcePath().split("/")[1];
    }

    public String getGroupIdFromKcEvent() {
        return getValueFromEventByAttributeKey("id");
    }
    public String getGroupNameFromKcEvent() {
        return getValueFromEventByAttributeKey("name");
    }

    public String getUserIdForGroupCreateEvent() {
        return event.getAuthDetails().getUserId();
    }

    private String getValueFromEventByAttributeKey(String attributeName) {
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
}
