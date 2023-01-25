package io.dguhr.keycloak.event;

public class SpiceDbTupleEventBuilder {
    private SpiceDbObject object;
    private SpiceDbRelation relation;
    private SpiceDbSubject subject;
    private EventOperation operation;
    private String orgId;
    private boolean isOrgAdmin;

    public SpiceDbTupleEventBuilder object(SpiceDbObject resource) {
        this.object = resource;
        return this;
    }

    public SpiceDbTupleEventBuilder relation(SpiceDbRelation relation) {
        this.relation = relation;
        return this;
    }

    public SpiceDbTupleEventBuilder subject(SpiceDbSubject subject) {
        this.subject = subject;
        return this;
    }

    public SpiceDbTupleEventBuilder operation(EventOperation operation) {
        this.operation = operation;
        return this;
    }

    public SpiceDbTupleEventBuilder orgId(String orgId) {
        this.orgId = orgId;
        return this;
    }

    public SpiceDbTupleEventBuilder isOrgAdmin(boolean isOrgAdmin) {
        this.isOrgAdmin = isOrgAdmin;
        return this;
    }
    public SpiceDbTupleEvent build() {
        return new SpiceDbTupleEvent(object, relation, subject, operation, orgId, isOrgAdmin);
    }
}