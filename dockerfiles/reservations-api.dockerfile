FROM amazoncorretto:11-alpine-jdk

COPY ./dist/reservations-api-sut.jar .
COPY ./dist/jacocoagent.jar .



#ENV TOOL="undefined"
#ENV RUN="0"

ENTRYPOINT \
    java \
#    unfortunately dumponexit is completely unreliable in Docker :(
#    -javaagent:jacocoagent.jar=destfile=./jacoco/reservations-api__${TOOL}__${RUN}__jacoco.exec,append=false,dumponexit=true \
    -javaagent:jacocoagent.jar=output=tcpserver,address=*,port=6300,append=false,dumponexit=false \
    -Dfile.encoding=ISO-8859-1 -jar reservations-api-sut.jar \
    --server.port=8080 --databaseUrl=mongodb://db:27017/mongo_db --spring.data.mongodb.uri=mongodb://db:27017/mongo_db --app.jwt.secret=abcdef012345678901234567890123456789abcdef012345678901234567890123456789