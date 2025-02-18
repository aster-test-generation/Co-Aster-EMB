FROM amazoncorretto:8-alpine-jdk

COPY ./dist/rest-news-sut.jar .
COPY ./dist/jacocoagent.jar .



ENV TOOL="undefined"
ENV RUN="0"

ENTRYPOINT \
    java \
    -javaagent:jacocoagent.jar=destfile=./jacoco/rest-news__${TOOL}__${RUN}__jacoco.exec,append=false,dumponexit=true \
     -jar rest-news-sut.jar \
    --server.port=8080