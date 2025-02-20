FROM amazoncorretto:8-alpine-jdk

COPY ./dist/scout-api-sut.jar .
COPY ./dist/jacocoagent.jar .




COPY ./scripts/dockerize/data/additional_files/scout-api/init_db.sql .

COPY ./scripts/dockerize/data/additional_files/scout-api/scout_api_evomaster.yml .



#ENV TOOL="undefined"
#ENV RUN="0"

ENTRYPOINT \
    java \
#    unfortunately dumponexit is completely unreliable in Docker :(
#    -javaagent:jacocoagent.jar=destfile=./jacoco/scout-api__${TOOL}__${RUN}__jacoco.exec,append=false,dumponexit=true \
    -javaagent:jacocoagent.jar=output=tcpserver,address=*,port=6300,append=false,dumponexit=false \
     -jar scout-api-sut.jar \
    server scout_api_evomaster.yml