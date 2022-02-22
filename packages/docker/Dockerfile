ARG java_image_tag=11-jre

##
# Stage base (tooling)
##
FROM openjdk:${java_image_tag} AS base

RUN set -ex && \
    sed -i 's/http:\/\/deb.\(.*\)/https:\/\/deb.\1/g' /etc/apt/sources.list && \
    apt-get update && \
    ln -s /lib /lib64 && \
    apt install -y bash tini libc6 libpam-modules krb5-user libnss3 procps curl && \
    echo "auth required pam_wheel.so use_uid" >> /etc/pam.d/su && \
    chgrp root /etc/passwd && chmod ug+rw /etc/passwd && \
    rm -rf /var/cache/apt/*

##
# Stage Apache Spark
##
ARG SPARK_VERSION=3.1.2
ARG HADOOP_VERSION=3.2

FROM base AS spark-base

# Download and extract Spark
RUN curl -L https://downloads.apache.org/spark/spark-${SPARK_VERSION}/spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}.tgz -o spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}.tgz \
    && tar -xvzf spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}.tgz \
    && mv spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION} /opt/spark \
    && rm spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}.tgz

##
# Stage Elastic APM
##
ARG ELASTIC_APM_VERSION=1.29.0

FROM base AS elastic-apm

RUN mkdir -p /opt/elastic && \
    curl -L https://search.maven.org/remotecontent?filepath=co/elastic/apm/elastic-apm-agent/${ELASTIC_APM_VERSION}/elastic-apm-agent-${ELASTIC_APM_VERSION}.jar -o /opt/elastic/elastic-apm-agent-${ELASTIC_APM_VERSION}.jar

##
# Stage Djobi
##
FROM base

ENV SPARK_HOME      /opt/spark
ENV DJOBI_HOME      /opt/djobi
ENV ELASTIC_HOME    /opt/elastic
ENV PATH            $PATH:${SPARK_HOME}/bin:${DJOBI_HOME}

##
# add djobi user to run as no-root
##
RUN groupadd --gid 1000 djobi && \
    useradd --no-create-home --uid 1000 --home-dir ${DJOBI_HOME} --gid 1000 djobi

##
# get artifacts
##
COPY --from=elastic-apm /opt/elastic    ${ELASTIC_HOME}
COPY --from=spark-base  /opt/spark      ${SPARK_HOME}
COPY                    build/release   ${DJOBI_HOME}
COPY                    packages/docker ${DJOBI_HOME}/docker

WORKDIR ${DJOBI_HOME}

RUN chmod +x /opt/djobi/djobi
RUN chmod a+x ${DJOBI_HOME}/docker/entrypoint.sh

ENTRYPOINT [ "/opt/djobi/docker/entrypoint.sh" ]

USER djobi