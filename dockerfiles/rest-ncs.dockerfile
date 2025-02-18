FROM amazoncorretto:8-alpine-jdk

COPY ./dist/rest-ncs-sut.jar .
COPY ./dist/jacocoagent.jar .



ENV TOOL="undefined"
ENV RUN="0"

ENTRYPOINT \
    java \
    -javaagent:jacocoagent.jar=destfile=./jacoco/rest-ncs__${TOOL}__${RUN}__jacoco.exec,append=false,dumponexit=true \
     -jar rest-ncs-sut.jar \
    --server.port=8080