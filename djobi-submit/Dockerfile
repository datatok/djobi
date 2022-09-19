FROM alpine:3.16.2 AS base

ENV SPARK_HOME      /opt/spark
ENV DJOBI_HOME      /opt/djobi
ENV ELASTIC_HOME    /opt/elastic

RUN apk add --update --no-cache wget curl python3 py3-pip bash

ENV DJOBI_HOME      /opt/djobi
ENV PATH            "${DJOBI_HOME}/submit/venv/bin:$PATH"

WORKDIR ${DJOBI_HOME}/submit

RUN python3 -m venv venv

COPY requirements.txt ./

RUN pip install --no-cache-dir -r requirements.txt

FROM alpine:3.16.2

RUN apk add --update --no-cache python3

ENV DJOBI_HOME      /opt/djobi
ENV PATH            "${DJOBI_HOME}/submit/venv/bin:$PATH"

COPY --from=base ${DJOBI_HOME}/submit/venv ${DJOBI_HOME}/submit/venv

COPY . ${DJOBI_HOME}/submit

WORKDIR ${DJOBI_HOME}/submit