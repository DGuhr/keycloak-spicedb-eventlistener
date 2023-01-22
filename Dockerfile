FROM quay.io/keycloak/keycloak:20.0.3 as builder

ENV KC_DB=postgres
ENV KC_HTTP_RELATIVE_PATH=/auth

COPY ./target/keycloak-spicedb-event-listener-2.0.0-jar-with-dependencies.jar /opt/keycloak/providers/keycloak-spicedb-event-listener-2.0.0.jar
RUN /opt/keycloak/bin/kc.sh build

FROM quay.io/keycloak/keycloak:20.0.3

COPY --from=builder /opt/keycloak/lib/quarkus/ /opt/keycloak/lib/quarkus/
COPY --from=builder /opt/keycloak/providers/ /opt/keycloak/providers/
COPY ./initialize-poc.sh /opt/keycloak/bin
COPY ./init.sh /opt/keycloak/bin

USER root
RUN chmod -R 554 /opt/keycloak/bin/initialize-poc.sh
RUN chmod -R 554 /opt/keycloak/bin/init.sh

USER 1000
ENTRYPOINT ["/opt/keycloak/bin/init.sh"]