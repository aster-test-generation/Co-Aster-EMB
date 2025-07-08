FROM amazoncorretto:8-alpine-jdk

COPY ./dist/swagger-petstore-sut.jar .
COPY ./dist/jacocoagent.jar .




COPY ./scripts/dockerize/data/additional_files/swagger-petstore/inflector.yaml .



#ENV TOOL="undefined"
#ENV RUN="0"

ENTRYPOINT \
    java \
#    unfortunately dumponexit is completely unreliable in Docker :(
#    -javaagent:jacocoagent.jar=destfile=./jacoco/swagger-petstore__${TOOL}__${RUN}__jacoco.exec,append=false,dumponexit=true \
    -javaagent:jacocoagent.jar=output=tcpserver,address=*,port=6300,append=false,dumponexit=false \
     -jar swagger-petstore-sut.jar \
    8080