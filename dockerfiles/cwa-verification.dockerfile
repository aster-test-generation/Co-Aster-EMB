FROM amazoncorretto:11-alpine-jdk

COPY ./dist/cwa-verification-sut.jar .
COPY ./dist/jacocoagent.jar .



ENV TOOL="undefined"
ENV RUN="0"

ENTRYPOINT \
    java \
    -javaagent:jacocoagent.jar=destfile=./jacoco/cwa-verification__${TOOL}__${RUN}__jacoco.exec,append=false,dumponexit=true \
    -Dspring.datasource.url=jdbc:h2:mem:testdb -Dspring.datasource.driver-class-name=org.h2.Driver -Dspring.datasource.username=sa -Dspring.datasource.password -jar cwa-verification-sut.jar \
    --server.port=8080 --spring.profiles.active=local,external,internal --management.server.port=-1 --server.ssl.enabled=false --cwa-testresult-server.url=http://cwa-testresult-server:8088