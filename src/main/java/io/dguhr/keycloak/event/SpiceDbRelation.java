package io.dguhr.keycloak.event;

public class SpiceDbRelation {

    private String relation;

    public String getRelationValue() {
        return relation;
    }

    public SpiceDbRelation relation(String relation) {
        this.relation = relation;
        return this;
    }
}
