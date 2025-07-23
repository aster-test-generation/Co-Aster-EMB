FROM amazoncorretto:8-alpine-jdk

COPY ./dist/angular-ecommerce-sut.jar .
COPY ./dist/jacocoagent.jar .



#ENV TOOL="undefined"
#ENV RUN="0"

ENTRYPOINT \
    java \
#    unfortunately dumponexit is completely unreliable in Docker :(
#    -javaagent:jacocoagent.jar=destfile=./jacoco/angular-ecommerce__${TOOL}__${RUN}__jacoco.exec,append=false,dumponexit=true \
    -javaagent:jacocoagent.jar=output=tcpserver,address=*,port=6300,append=false,dumponexit=false \
    -Dfile.encoding=ISO-8859-1 -jar spring-ecommerce-sut.jar \
    --server.port=8080 --spring.datasource.host=mongodb --spring.datasource.port=27017 --spring.datasource.database=test --spring.data.mongodb.uri=mongodb://mongodb:27017/test --spring.redis.host=redis --spring.redis.port=6379 --spring.data.elasticsearch.cluster-name=elasticsearch --spring.data.elsticsearch.cluster-nodes=elastic:9300 --spring.cache.type=NONE