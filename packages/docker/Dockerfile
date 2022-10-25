ARG java_image_tag=17-jre

##
# Stage base (tooling)
##
FROM alpine:3.16.2 AS base

ENV SPARK_HOME      /opt/spark
ENV DJOBI_HOME      /opt/djobi
ENV ELASTIC_HOME    /opt/elastic

RUN apk add --update --no-cache wget curl

##
# Apache Spark distribution
# - remove unused stuff for Djobi
##
FROM base AS spark-base

ARG SPARK_VERSION=3.3.0
ARG HADOOP_VERSION=3
ARG SPARK_CDN=https://dlcdn.apache.org/spark

RUN wget --quiet ${SPARK_CDN}/spark-${SPARK_VERSION}/spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}.tgz \
      && tar -xzf spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}.tgz \
      && mv spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION} spark \
      && cd spark && rm -rf examples data python R yarn

##
# Stage Elastic APM
##
FROM base AS elastic-apm

ARG ELASTIC_APM_VERSION=1.34.1

RUN mkdir -p /opt/elastic && \
    curl -sL https://search.maven.org/remotecontent?filepath=co/elastic/apm/elastic-apm-agent/${ELASTIC_APM_VERSION}/elastic-apm-agent-${ELASTIC_APM_VERSION}.jar -o /opt/elastic/elastic-apm-agent-${ELASTIC_APM_VERSION}.jar && \
    ln -s /opt/elastic/elastic-apm-agent-${ELASTIC_APM_VERSION}.jar /opt/elastic/elastic-apm-agent.jar

##
# Stage "djobi-submit" python app
##
FROM base AS djobi-submit

RUN apk add --update --no-cache python3 py3-pip

ENV PATH "${DJOBI_HOME}/submit/venv/bin:$PATH"

WORKDIR ${DJOBI_HOME}/submit

RUN python3 -m venv venv

COPY djobi-submit/requirements.txt ./

RUN pip install --no-cache-dir -r requirements.txt

##
# Stage Djobi
##
FROM base

RUN apk add --update --no-cache openjdk11 --repository=http://dl-cdn.alpinelinux.org/alpine/edge/community && \
    apk add --update --no-cache \
            bash \
            tini \
            python3 # https://pkgs.alpinelinux.org/package/v3.16/main/x86_64/python3 \
            java-snappy-native \
    && \
    rm -rf /var/cache/* /tmp/*

ENV LANG=en_US.UTF-8 \
    LC_ALL=en_US.UTF-8 \
    PYTHONPATH="/opt/spark/python/:$PYTHONPATH" \
    SPARK_WORK_DIR="${SPARK_HOME}/work" \
    JAVA_HOME="/usr/lib/jvm/java-17-openjdk"

ENV PATH="${JAVA_HOME}/bin:${SPARK_HOME}/bin:${SPARK_HOME}/sbin:${DJOBI_HOME}/submit/venv/bin:$PATH"

##
# get artifacts
##
COPY --from=elastic-apm  --chown=1000:1000 /opt/elastic         ${ELASTIC_HOME}
COPY --from=spark-base   --chown=1000:1000 /spark               ${SPARK_HOME}
COPY                     --chown=1000:1000 build/release        ${DJOBI_HOME}
COPY                     --chown=1000:1000 packages/docker      ${DJOBI_HOME}/docker
COPY                     --chown=1000:1000 djobi-submit         ${DJOBI_HOME}/submit
COPY --from=djobi-submit --chown=1000:1000 ${DJOBI_HOME}/submit/venv ${DJOBI_HOME}/submit/venv

##
# add djobi user to run as no-root
##
RUN adduser --no-create-home --disabled-password --uid 1000 --home ${DJOBI_HOME} djobi && \
    mkdir ${SPARK_WORK_DIR} && \
    chgrp root /etc/passwd && chmod ug+rw /etc/passwd

WORKDIR ${DJOBI_HOME}

RUN chmod a+x ${DJOBI_HOME}/docker/entrypoint.sh

ENTRYPOINT [ "/opt/djobi/docker/entrypoint.sh" ]

USER djobi
