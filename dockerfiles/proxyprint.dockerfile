FROM amazoncorretto:8-alpine-jdk

COPY ./dist/proxyprint-sut.jar .
COPY ./dist/jacocoagent.jar .



#ENV TOOL="undefined"
#ENV RUN="0"

ENTRYPOINT \
    java \
#    unfortunately dumponexit is completely unreliable in Docker :(
#    -javaagent:jacocoagent.jar=destfile=./jacoco/proxyprint__${TOOL}__${RUN}__jacoco.exec,append=false,dumponexit=true \
    -javaagent:jacocoagent.jar=output=tcpserver,address=*,port=6300,append=false,dumponexit=false \
    -Dspring.datasource.url=jdbc:h2:mem:testdb -Dspring.jpa.database-platform=org.hibernate.dialect.H2Dialect -Dspring.datasource.username=sa -Dspring.datasource.password -Dspring.jpa.show-sql=false -Dspring.jpa.hibernate.ddl-auto=create-drop -Xmx4G -jar proxyprint-sut.jar \
    --server.port=8080