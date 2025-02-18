FROM amazoncorretto:11-alpine-jdk

COPY ./dist/market-sut.jar .
COPY ./dist/jacocoagent.jar .



ENV TOOL="undefined"
ENV RUN="0"

ENTRYPOINT \
    java \
    -javaagent:jacocoagent.jar=destfile=./jacoco/market__${TOOL}__${RUN}__jacoco.exec,append=false,dumponexit=true \
    -Dspring.datasource.url="jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1" -Dspring.datasource.username=sa -Dspring.datasource.password -jar market-sut.jar \
    --server.port=8080