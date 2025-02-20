FROM amazoncorretto:8-alpine-jdk

COPY ./dist/genome-nexus-sut.jar .
COPY ./dist/jacocoagent.jar .



#ENV TOOL="undefined"
#ENV RUN="0"

ENTRYPOINT \
    java \
#    unfortunately dumponexit is completely unreliable in Docker :(
#    -javaagent:jacocoagent.jar=destfile=./jacoco/genome-nexus__${TOOL}__${RUN}__jacoco.exec,append=false,dumponexit=true \
    -javaagent:jacocoagent.jar=output=tcpserver,address=*,port=6300,append=false,dumponexit=false \
     -jar genome-nexus-sut.jar \
    --server.port=8080 --spring.data.mongodb.uri=mongodb://db:27017/mongo_db --spring.cache.type=NONE