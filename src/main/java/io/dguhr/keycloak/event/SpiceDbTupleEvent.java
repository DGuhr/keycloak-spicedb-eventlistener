package io.dguhr.keycloak.event;

public class SpiceDbTupleEvent {

    private SpiceDbResource resource;

    private SpiceDbRelation relation;

    private SpiceDbSubject object;

    public SpiceDbResource getResource() {
        return resource;
    }

    public void setResource(SpiceDbResource resource) {
        this.resource = resource;
    }

    public SpiceDbRelation getRelation() {
        return relation;
    }

    public void setRelation(SpiceDbRelation relation) {
        this.relation = relation;
    }

    public SpiceDbSubject getObject() {
        return object;
    }

    public void setObject(SpiceDbSubject object) {
        this.object = object;
    }
}
