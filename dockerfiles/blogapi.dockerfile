FROM amazoncorretto:8-alpine-jdk

COPY ./dist/blogapi-sut.jar .
COPY ./dist/jacocoagent.jar .



ENV TOOL="undefined"
ENV RUN="0"

ENTRYPOINT \
    java \
    -javaagent:jacocoagent.jar=destfile=./jacoco/blogapi__${TOOL}__${RUN}__jacoco.exec,append=false,dumponexit=true \
     -jar blogapi-sut.jar \
    --server.port=8080 --spring.datasource.url="jdbc:mysql://db:3306/blogapi?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"