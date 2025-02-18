FROM amazoncorretto:8-alpine-jdk

COPY ./dist/features-service-sut.jar .
COPY ./dist/jacocoagent.jar .



ENV TOOL="undefined"
ENV RUN="0"

ENTRYPOINT \
    java \
    -javaagent:jacocoagent.jar=destfile=./jacoco/features-service__${TOOL}__${RUN}__jacoco.exec,append=false,dumponexit=true \
    -Dspring.datasource.url=jdbc:h2:mem:testdb -Dspring.jpa.database-platform=org.hibernate.dialect.H2Dialect -Dspring.datasource.username=sa -Dspring.datasource.password -jar features-service-sut.jar \
    --server.port=8080