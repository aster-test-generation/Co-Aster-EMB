FROM amazoncorretto:17-alpine-jdk

COPY ./dist/tiltaksgjennomforing-api-sut.jar .
COPY ./dist/jacocoagent.jar .



#ENV TOOL="undefined"
#ENV RUN="0"

ENTRYPOINT \
    java \
#    unfortunately dumponexit is completely unreliable in Docker :(
#    -javaagent:jacocoagent.jar=destfile=./jacoco/tiltaksgjennomforing-api__${TOOL}__${RUN}__jacoco.exec,append=false,dumponexit=true \
    -javaagent:jacocoagent.jar=output=tcpserver,address=*,port=6300,append=false,dumponexit=false \
     -jar tiltaksgjennomforing-api-sut.jar \
    --server.port=8080 --spring.profiles.active=dev-gcp-labs --spring.datasource.driverClassName=org.postgresql.Driver --spring.sql.init.platform=postgres --no.nav.security.jwt.issuer.aad.discoveryurl=http://mock-oauth2-server:8083/azuread/.well-known/openid-configuration --no.nav.security.jwt.issuer.tokenx.discoveryurl=http://mock-oauth2-server:8083/azuread/.well-known/openid-configuration --management.server.port=-1 --server.ssl.enabled=false --spring.datasource.url=jdbc:postgresql://db:5432/tiltaksgjennomforing  --spring.datasource.username=postgres --spring.datasource.password=password --sentry.logging.enabled=false --sentry.environment=local --logging.level.root=OFF --logging.config=classpath:logback-spring.xml --logging.level.org.springframework=INFO