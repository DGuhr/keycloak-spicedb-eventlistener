package io.dguhr.keycloak.event;

public class SpiceDbSubject {

    private String subjectType;
    private String subjectValue;

    public String getSubjectType() {
        return subjectType;
    }

    public SpiceDbSubject subjectType(String subjectType) {
        this.subjectType = subjectType;
        return this;
    }

    public String getSubjectValue() {
        return subjectValue;
    }

    public SpiceDbSubject subjectValue(String subjectValue) {
        this.subjectValue = subjectValue;
        return this;
    }
}
