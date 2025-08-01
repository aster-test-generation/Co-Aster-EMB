FROM amazoncorretto:11-alpine-jdk

COPY ./dist/quartz-manager-sut.jar .
COPY ./dist/jacocoagent.jar .



#ENV TOOL="undefined"
#ENV RUN="0"

ENTRYPOINT \
    java \
#    unfortunately dumponexit is completely unreliable in Docker :(
#    -javaagent:jacocoagent.jar=destfile=./jacoco/quartz-manager__${TOOL}__${RUN}__jacoco.exec,append=false,dumponexit=true \
    -javaagent:jacocoagent.jar=output=tcpserver,address=*,port=6300,append=false,dumponexit=false \
     -jar quartz-manager-sut.jar \
    --server.port=8080 --quartz-manager.security.accounts.in-memory.users[0].username=foo --quartz-manager.security.accounts.in-memory.users[0].password=bar --quartz-manager.security.accounts.in-memory.users[0].roles[0]=admin --quartz-manager.security.accounts.in-memory.users[1].username=foo2 --quartz-manager.security.accounts.in-memory.users[1].password=bar --quartz-manager.security.accounts.in-memory.users[1].roles[0]=admin