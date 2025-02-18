FROM amazoncorretto:8-alpine-jdk

COPY ./dist/restcountries-sut.jar .
COPY ./dist/jacocoagent.jar .



ENV TOOL="undefined"
ENV RUN="0"

ENTRYPOINT \
    java \
    -javaagent:jacocoagent.jar=destfile=./jacoco/restcountries__${TOOL}__${RUN}__jacoco.exec,append=false,dumponexit=true \
     -jar restcountries-sut.jar \
    --server.port=8080