FROM amazoncorretto:17-alpine-jdk

COPY ./dist/spring-rest-example-sut.jar .
COPY ./dist/jacocoagent.jar .



#ENV TOOL="undefined"
#ENV RUN="0"

ENTRYPOINT \
    java \
#    unfortunately dumponexit is completely unreliable in Docker :(
#    -javaagent:jacocoagent.jar=destfile=./jacoco/spring-rest-example__${TOOL}__${RUN}__jacoco.exec,append=false,dumponexit=true \
    -javaagent:jacocoagent.jar=output=tcpserver,address=*,port=6300,append=false,dumponexit=false \
     -jar spring-rest-example-sut.jar \
    --server.port=8080 --spring.datasource.username=root --spring.datasource.password=root --spring.datasource.url="jdbc:mysql://db:3306/example?useSSL=false&allowPublicKeyRetrieval=true"