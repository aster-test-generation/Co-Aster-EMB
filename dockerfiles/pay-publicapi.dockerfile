FROM amazoncorretto:11-alpine-jdk

COPY ./dist/pay-publicapi-sut.jar .
COPY ./dist/jacocoagent.jar .




COPY ./scripts/dockerize/data/additional_files/pay-publicapi/em_config.yaml .



#ENV TOOL="undefined"
#ENV RUN="0"

ENTRYPOINT \
    java \
#    unfortunately dumponexit is completely unreliable in Docker :(
#    -javaagent:jacocoagent.jar=destfile=./jacoco/pay-publicapi__${TOOL}__${RUN}__jacoco.exec,append=false,dumponexit=true \
    -javaagent:jacocoagent.jar=output=tcpserver,address=*,port=6300,append=false,dumponexit=false \
    -Ddw.server.applicationConnectors[0].port=8080 -Ddw.server.adminConnectors[0].port=0 -Ddw.redis.endpoint=db:6379 -jar pay-publicapi-sut.jar \
    server em_config.yaml