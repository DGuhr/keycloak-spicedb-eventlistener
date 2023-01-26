package io.dguhr.keycloak.event;

public class SpiceDbTupleEvent {

    private SpiceDbObject object;

    private SpiceDbRelation relation;

    private SpiceDbSubject subject;

    private EventOperation operation;

    private String orgId;

    private boolean isOrgAdmin;

    public SpiceDbTupleEvent(SpiceDbObject object, SpiceDbRelation relation,
                             SpiceDbSubject subject, EventOperation operation,
                             String orgId, boolean isOrgAdmin) {
        this.object = object;
        this.relation = relation;
        this.subject = subject;
        this.operation = operation;
        this.orgId = orgId;
        this.isOrgAdmin = isOrgAdmin;
    }

    public SpiceDbObject getObject() {
        return object;
    }

    public SpiceDbTupleEvent object(SpiceDbObject object) {
        this.object = object;
        return this;
    }

    public SpiceDbRelation getRelation() {
        return relation;
    }

    public SpiceDbTupleEvent relation(SpiceDbRelation relation) {
        this.relation = relation;
        return this;
    }

    public SpiceDbSubject getSubject() {
        return subject;
    }

    public SpiceDbTupleEvent subject(SpiceDbSubject subject) {
        this.subject = subject;
        return this;
    }

    public EventOperation getOperation() {
        return operation;
    }

    public SpiceDbTupleEvent operation(EventOperation operation) {
        this.operation = operation;
        return this;
    }

    public String getOrgId() {
        return orgId;
    }

    public SpiceDbTupleEvent orgId(String orgId) {
        this.orgId = orgId;
        return this;
    }

    public boolean getIsOrgAdmin() {
        return isOrgAdmin;
    }

    public SpiceDbTupleEvent isOrgAdmin(boolean orgAdmin) {
        isOrgAdmin = orgAdmin;
        return this;
    }
}
