FROM amazoncorretto:21-alpine-jdk

COPY ./dist/person-controller-sut.jar .
COPY ./dist/jacocoagent.jar .



ENV TOOL="undefined"
ENV RUN="0"

ENTRYPOINT \
    java \
    -javaagent:jacocoagent.jar=destfile=./jacoco/person-controller__${TOOL}__${RUN}__jacoco.exec,append=false,dumponexit=true \
     -jar person-controller-sut.jar \
    --server.port=8080 --spring.data.mongodb.uri=mongodb://db:27017 --spring.cache.type=None