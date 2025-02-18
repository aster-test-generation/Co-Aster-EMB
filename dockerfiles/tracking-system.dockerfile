FROM amazoncorretto:11-alpine-jdk

COPY ./dist/tracking-system-sut.jar .
COPY ./dist/jacocoagent.jar .



ENV TOOL="undefined"
ENV RUN="0"

ENTRYPOINT \
    java \
    -javaagent:jacocoagent.jar=destfile=./jacoco/tracking-system__${TOOL}__${RUN}__jacoco.exec,append=false,dumponexit=true \
     -jar tracking-system-sut.jar \
    --server.port=8080 --spring.profiles.active=dev --spring.datasource.url="jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1" --spring.datasource.username=sa --spring.datasource.password