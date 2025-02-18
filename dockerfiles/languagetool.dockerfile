FROM amazoncorretto:8-alpine-jdk

COPY ./dist/languagetool-sut.jar .
COPY ./dist/jacocoagent.jar .



ENV TOOL="undefined"
ENV RUN="0"

ENTRYPOINT \
    java \
    -javaagent:jacocoagent.jar=destfile=./jacoco/languagetool__${TOOL}__${RUN}__jacoco.exec,append=false,dumponexit=true \
     -jar languagetool-sut.jar \
    --port 8080 --public