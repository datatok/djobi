#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
ARG java_image_tag=11-jre-slim

FROM openjdk:${java_image_tag}

# Before building the docker image, first build and make a Spark distribution following
# the instructions in http://spark.apache.org/docs/latest/building-spark.html.
# If this docker file is being used in the context of building your images from a Spark
# distribution, the docker build command should be invoked from the top level directory
# of the Spark distribution. E.g.:
# docker build -t spark:latest -f kubernetes/dockerfiles/spark/Dockerfile .

RUN set -ex && \
    sed -i 's/http:\/\/deb.\(.*\)/https:\/\/deb.\1/g' /etc/apt/sources.list && \
    apt-get update && \
    ln -s /lib /lib64 && \
    apt install -y bash tini libc6 libpam-modules krb5-user libnss3 procps curl && \
    mkdir -p /opt/spark && \
    mkdir -p /opt/spark/work-dir && \
    touch /opt/spark/RELEASE && \
    rm /bin/sh && \
    ln -sv /bin/bash /bin/sh && \
    echo "auth required pam_wheel.so use_uid" >> /etc/pam.d/su && \
    chgrp root /etc/passwd && chmod ug+rw /etc/passwd && \
    rm -rf /var/cache/apt/*

ARG spark_version=3.1.2
ARG hadoop_version=3.2

RUN curl -sL https://mirrors.estointernet.in/apache/spark/spark-${spark_version}/spark-${spark_version}-bin-hadoop${hadoop_version}.tgz --output spark-bin-hadoop.tgz && \
    tar -xzf spark-bin-hadoop.tgz

RUN cd spark-${spark_version}-bin-hadoop${hadoop_version} && \
    cp -r jars /opt/spark/jars && \
    cp -r bin /opt/spark/bin && \
    cp -r sbin /opt/spark/sbin && \
    cp -r conf /opt/spark/conf && \
    cp -r kubernetes/dockerfiles/spark/entrypoint.sh /opt/  && \
    cp -r kubernetes/dockerfiles/spark/decom.sh /opt/  && \
    cp -r kubernetes/tests /opt/spark/tests  && \
    cp -r data /opt/spark/data

ENV SPARK_HOME /opt/spark
ENV DJOBI_HOME /opt/djobi

ENV PATH $PATH:${SPARK_HOME}/bin:${DJOBI_HOME}

ADD build/release ${DJOBI_HOME}

WORKDIR ${DJOBI_HOME}

##
# add djobi user to run as no-root
##
RUN groupadd --gid 1000 djobi && \
    useradd --no-create-home --uid 1000 --home-dir ${DJOBI_HOME} --gid 1000 djobi

RUN chmod g+w /opt/spark/work-dir
RUN chmod a+x /opt/decom.sh
RUN chmod +x /opt/djobi/djobi

USER djobi