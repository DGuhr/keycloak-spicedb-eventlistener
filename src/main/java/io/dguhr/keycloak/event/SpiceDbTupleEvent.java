package io.dguhr.keycloak.event;

public class SpiceDbTupleEvent {

    private SpiceDbObject resource;

    private SpiceDbRelation relation;

    private SpiceDbSubject subject;

    private EventOperation operation;

    private String orgId;

    private boolean isOrgAdmin;

    public SpiceDbTupleEvent(SpiceDbObject resource, SpiceDbRelation relation,
                             SpiceDbSubject subject, EventOperation operation,
                             String orgId, boolean isOrgAdmin) {
        this.resource = resource;
        this.relation = relation;
        this.subject = subject;
        this.operation = operation;
        this.orgId = orgId;
        this.isOrgAdmin = isOrgAdmin;
    }

    public SpiceDbObject getResource() {
        return resource;
    }

    public void setResource(SpiceDbObject resource) {
        this.resource = resource;
    }

    public SpiceDbRelation getRelation() {
        return relation;
    }

    public void setRelation(SpiceDbRelation relation) {
        this.relation = relation;
    }

    public SpiceDbSubject getSubject() {
        return subject;
    }

    public void setSubject(SpiceDbSubject subject) {
        this.subject = subject;
    }

    public EventOperation getOperation() {
        return operation;
    }

    public void operation(EventOperation operation) {
        this.operation = operation;
    }

    public String getOrgId() {
        return orgId;
    }

    public void orgId(String orgId) {
        this.orgId = orgId;
    }

    public boolean getIsOrgAdmin() {
        return isOrgAdmin;
    }

    public void isOrgAdmin(boolean orgAdmin) {
        isOrgAdmin = orgAdmin;
    }
}
