FROM amazoncorretto:8-alpine-jdk

COPY ./dist/rest-scs-sut.jar .
COPY ./dist/jacocoagent.jar .



ENV TOOL="undefined"
ENV RUN="0"

ENTRYPOINT \
    java \
    -javaagent:jacocoagent.jar=destfile=./jacoco/rest-scs__${TOOL}__${RUN}__jacoco.exec,append=false,dumponexit=true \
     -jar rest-scs-sut.jar \
    --server.port=8080