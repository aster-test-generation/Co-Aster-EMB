FROM {{BASE_IMAGE}}

COPY {{EMB_DIR}}/{{SUT_NAME}}-sut.jar .
COPY {{EMB_DIR}}/jacocoagent.jar .

{% if ADDITIONAL_FILES %}

{% for file in files %}
COPY {{file['source']}} .
{% endfor %}
{% endif %}

ENV TOOL="undefined"
ENV RUN="0"

ENTRYPOINT \
    java \
    -javaagent:jacocoagent.jar=destfile=./jacoco/{{SUT_NAME}}__{{TOOL}}__{{RUN}}__jacoco.exec,append=false,dumponexit=true \
    {{ JVM_PARAMETERS if JVM_PARAMETERS }} -jar {{SUT_NAME}}-sut.jar \
    {{ INPUT_PARAMETERS if INPUT_PARAMETERS }}
