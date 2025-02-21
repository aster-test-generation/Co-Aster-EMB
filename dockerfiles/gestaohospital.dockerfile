FROM amazoncorretto:8-alpine-jdk

COPY ./dist/gestaohospital-sut.jar .
COPY ./dist/jacocoagent.jar .



#ENV TOOL="undefined"
#ENV RUN="0"

ENTRYPOINT \
    java \
#    unfortunately dumponexit is completely unreliable in Docker :(
#    -javaagent:jacocoagent.jar=destfile=./jacoco/gestaohospital__${TOOL}__${RUN}__jacoco.exec,append=false,dumponexit=true \
    -javaagent:jacocoagent.jar=output=tcpserver,address=*,port=6300,append=false,dumponexit=false \
     -jar gestaohospital-sut.jar \
    --server.port=8080 --liquibase.enabled=false --spring.data.mongodb.uri=mongodb://db:27017/mongo_db --spring.datasource.username=sa --spring.datasource.password --dg-toolkit.derby.port=0 --spring.cache.type=NONE