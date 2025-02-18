FROM amazoncorretto:8-alpine-jdk

COPY ./dist/youtube-mock-sut.jar .
COPY ./dist/jacocoagent.jar .



ENV TOOL="undefined"
ENV RUN="0"

ENTRYPOINT \
    java \
    -javaagent:jacocoagent.jar=destfile=./jacoco/youtube-mock__${TOOL}__${RUN}__jacoco.exec,append=false,dumponexit=true \
     -jar youtube-mock-sut.jar \
    --server.port=8080