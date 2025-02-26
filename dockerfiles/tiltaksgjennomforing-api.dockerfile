FROM amazoncorretto:17-alpine-jdk

COPY ./dist/tiltaksgjennomforing-api-sut.jar .
COPY ./dist/jacocoagent.jar .


ENV AZURE_APP_WELL_KNOWN_URL="http://localhost:8083/aad/.well-known/openid-configuration"
ENV TOKEN_X_WELL_KNOWN_URL="http://localhost:8083/tokenx/.well-known/openid-configuration"
ENV VAULT_TOKEN="VAULT_TOKEN"
ENV KAFKA_BROKERS="KAFKA_BROKERS"
ENV KAFKA_TRUSTSTORE_PATH="KAFKA_TRUSTSTORE_PATH"
ENV KAFKA_CREDSTORE_PASSWORD="KAFKA_CREDSTORE_PASSWORD"
ENV KAFKA_KEYSTORE_PATH="KAFKA_KEYSTORE_PATH"
ENV KAFKA_SCHEMA_REGISTRY="KAFKA_SCHEMA_REGISTRY"
ENV KAFKA_SCHEMA_REGISTRY_USER="KAFKA_SCHEMA_REGISTRY_USER"
ENV KAFKA_SCHEMA_REGISTRY_PASSWORD="KAFKA_SCHEMA_REGISTRY_PASSWORD"
ENV AZURE_APP_TENANT_ID="AZURE_APP_TENANT_ID"
ENV AZURE_APP_CLIENT_ID="aad"
ENV AZURE_APP_CLIENT_SECRET="secret"
ENV beslutter.ad.gruppe="99ea78dc-db77-44d0-b193-c5dc22f01e1d"
#ENV TOOL="undefined"
#ENV RUN="0"

ENTRYPOINT \
    java \
#    unfortunately dumponexit is completely unreliable in Docker :(
#    -javaagent:jacocoagent.jar=destfile=./jacoco/tiltaksgjennomforing-api__${TOOL}__${RUN}__jacoco.exec,append=false,dumponexit=true \
    -javaagent:jacocoagent.jar=output=tcpserver,address=*,port=6300,append=false,dumponexit=false \
     -jar tiltaksgjennomforing-api-sut.jar \
    --server.port=8080 --spring.profiles.active=dev-gcp-labs --spring.datasource.driverClassName=org.postgresql.Driver --spring.sql.init.platform=postgres --no.nav.security.jwt.issuer.aad.discoveryurl=http://localhost:8083/aad/.well-known/openid-configuration --no.nav.security.jwt.issuer.tokenx.discoveryurl=http://localhost:8083/tokenx/.well-known/openid-configuration --management.server.port=-1 --server.ssl.enabled=false --spring.datasource.url=jdbc:postgresql://db:5432/tiltaksgjennomforing  --spring.datasource.username=postgres --spring.datasource.password=password --sentry.logging.enabled=false --sentry.environment=local --logging.level.root=OFF --logging.config=classpath:logback-spring.xml --logging.level.org.springframework=INFO