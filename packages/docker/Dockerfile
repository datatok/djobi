ARG java_image_tag=11-jre

##
# Stage base (tooling)
##
FROM alpine:3.16 AS base

ENV SPARK_HOME      /opt/spark
ENV DJOBI_HOME      /opt/djobi
ENV ELASTIC_HOME    /opt/elastic

RUN apk add --update --no-cache wget curl

##
# Stage Apache Spark
##
FROM base AS spark-base

ARG SPARK_VERSION=3.2.2
ARG HADOOP_VERSION=3.2
ARG SPARK_CDN=https://dlcdn.apache.org/spark

RUN wget --quiet ${SPARK_CDN}/spark-${SPARK_VERSION}/spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}.tgz \
      && tar -xzf spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}.tgz \
      && mv spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION} spark \
      && rm -rf spark/examples spark/data \
      && rm spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}.tgz

##
# Stage Elastic APM
##
FROM base AS elastic-apm

ARG ELASTIC_APM_VERSION=1.32.0

RUN mkdir -p /opt/elastic && \
    curl -sL https://search.maven.org/remotecontent?filepath=co/elastic/apm/elastic-apm-agent/${ELASTIC_APM_VERSION}/elastic-apm-agent-${ELASTIC_APM_VERSION}.jar -o /opt/elastic/elastic-apm-agent-${ELASTIC_APM_VERSION}.jar && \
    ln -s /opt/elastic/elastic-apm-agent-${ELASTIC_APM_VERSION}.jar /opt/elastic/elastic-apm-agent.jar

##
# Stage Djobi
##
FROM base

RUN apk --no-cache add openjdk11 --repository=http://dl-cdn.alpinelinux.org/alpine/edge/community

RUN apk add --update --no-cache \
            bash \
            tini \
            python3 py3-pip \
            java-snappy-native

ENV LANG=en_US.UTF-8 \
    LC_ALL=en_US.UTF-8 \
    PYTHONPATH="/opt/spark/python/:$PYTHONPATH" \
    SPARK_WORK_DIR="${SPARK_HOME}/work" \
    JAVA_HOME="/usr/lib/jvm/java-11-openjdk"

ENV PATH="/opt/python/bin:${JAVA_HOME}/bin:${SPARK_HOME}/bin:${SPARK_HOME}/sbin:${DJOBI_HOME}:$PATH"

##
# get artifacts
##
COPY --from=elastic-apm /opt/elastic        ${ELASTIC_HOME}
COPY --from=spark-base  /spark              ${SPARK_HOME}
COPY                    build/release       ${DJOBI_HOME}
COPY                    packages/docker     ${DJOBI_HOME}/docker
COPY                    djobi-submit        ${DJOBI_HOME}/submit

##
# add djobi user to run as no-root
##
RUN adduser --no-create-home --disabled-password --uid 1000 --home ${DJOBI_HOME} djobi && \
    mkdir ${SPARK_WORK_DIR} && \
    chgrp root /etc/passwd && chmod ug+rw /etc/passwd && \
    chmod g+w ${SPARK_WORK_DIR} && \
    chown djobi ${DJOBI_HOME} ${SPARK_WORK_DIR}

WORKDIR ${SPARK_WORK_DIR}

RUN chmod a+x ${DJOBI_HOME}/docker/entrypoint.sh

ENTRYPOINT [ "/opt/djobi/docker/entrypoint.sh" ]

USER djobi

RUN pip install ${DJOBI_HOME}/submit/dist/djobi_submit-0.1.0-py3-none-any.whl