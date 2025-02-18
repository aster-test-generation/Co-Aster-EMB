FROM amazoncorretto:8-alpine-jdk

COPY ./dist/session-service-sut.jar .
COPY ./dist/jacocoagent.jar .



ENV TOOL="undefined"
ENV RUN="0"

ENTRYPOINT \
    java \
    -javaagent:jacocoagent.jar=destfile=./jacoco/session-service__${TOOL}__${RUN}__jacoco.exec,append=false,dumponexit=true \
     -jar session-service-sut.jar \
    --server.port=8080 --spring.data.mongodb.uri=mongodb://db:27017/mongo_db --spring.cache.type=NONE