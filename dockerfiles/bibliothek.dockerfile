FROM amazoncorretto:17-alpine-jdk

COPY ./dist/bibliothek-sut.jar .
COPY ./dist/jacocoagent.jar .



#ENV TOOL="undefined"
#ENV RUN="0"

ENTRYPOINT \
    java \
#    unfortunately dumponexit is completely unreliable in Docker :(
#    -javaagent:jacocoagent.jar=destfile=./jacoco/bibliothek__${TOOL}__${RUN}__jacoco.exec,append=false,dumponexit=true \
    -javaagent:jacocoagent.jar=output=tcpserver,address=*,port=6300,append=false,dumponexit=false \
     -jar bibliothek-sut.jar \
    --server.port=8080 --databaseUrl=mongodb://db:27017/mongo_db --spring.data.mongodb.uri=mongodb://db:27017/mongo_db --app.storagePath=./tmp/bibliothek/