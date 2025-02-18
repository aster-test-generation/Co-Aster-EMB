FROM amazoncorretto:8-alpine-jdk

COPY ./dist/genome-nexus-sut.jar .
COPY ./dist/jacocoagent.jar .



ENV TOOL="undefined"
ENV RUN="0"

ENTRYPOINT \
    java \
    -javaagent:jacocoagent.jar=destfile=./jacoco/genome-nexus__${TOOL}__${RUN}__jacoco.exec,append=false,dumponexit=true \
     -jar genome-nexus-sut.jar \
    --server.port=8080 --spring.data.mongodb.uri=mongodb://db:27017/mongo_db --spring.cache.type=NONE