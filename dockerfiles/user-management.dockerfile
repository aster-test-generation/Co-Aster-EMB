FROM amazoncorretto:8-alpine-jdk

COPY ./dist/user-management-sut.jar .
COPY ./dist/jacocoagent.jar .



#ENV TOOL="undefined"
#ENV RUN="0"

ENTRYPOINT \
    java \
#    unfortunately dumponexit is completely unreliable in Docker :(
#    -javaagent:jacocoagent.jar=destfile=./jacoco/user-management__${TOOL}__${RUN}__jacoco.exec,append=false,dumponexit=true \
    -javaagent:jacocoagent.jar=output=tcpserver,address=*,port=6300,append=false,dumponexit=false \
     -jar user-management-sut.jar \
    --server.port=8080 --spring.datasource.url="jdbc:mysql://db:3306/users?useSSL=false&allowPublicKeyRetrieval=true"