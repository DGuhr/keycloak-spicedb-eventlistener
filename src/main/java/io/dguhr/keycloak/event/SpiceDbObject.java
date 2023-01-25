package io.dguhr.keycloak.event;

public class SpiceDbObject {

    private String objectType;
    private String objectValue;

    public String getObjectValue() {
        return objectValue;
    }

    public SpiceDbObject objectValue(String objectValue) {
        this.objectValue = objectValue;
        return this;
    }

    public String getObjectType() {
        return objectType;
    }

    public SpiceDbObject objectType(String objectType) {
        this.objectType = objectType;
        return this;
    }
}
