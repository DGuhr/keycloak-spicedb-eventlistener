package io.dguhr.keycloak.event;

public enum EventOperation {
    ADDUSER,
    UPDATEUSER,//also update group membership on name change?! - UUID only?
    DELETEUSER,
    ADDGROUP,
    UPDATEGROUP,//also update existing relationships?! - UUID only?
    DELETEGROUP,
    ADDGROUPMEMBER,
    DELETEGROUPMEMBER,
}
