#!/usr/bin/env bash

DJOBI_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

: ${SPARK_MASTER:=yarn}
: ${SPARK_SUBMIT:=spark-submit}
: ${SPARK_WORKERS:=1}
: ${SPARK_CORES:=8}
: ${SPARK_WORKER_MEMORY:=10g}
: ${SPARK_UI_PORT:=4040}

: ${SPARK_DRIVER_CORES:=2}
: ${SPARK_DRIVER_MEMORY:=2g}

: ${SUBMIT_MODE:="client"}
: ${YARN_QUEUE:="collect"}

: ${JOB_NAME:="djobi"}

: ${SPARK_SHUFFLE_PARTITIONS:=10}

: ${ES_SUPPORT:="7"}
: ${KAFKA_SUPPORT:=""}
: ${USERAGENT_PARSER:=""}
: ${USE_AGENT:=true}

: ${LIBS:="${DJOBI_ROOT}/libs"}
: ${JARS:="${LIBS}/djobi-core-%%VERSION%%.jar,${LIBS}/djobi-elasticsearch-es${ES_SUPPORT}-%%VERSION%%.jar"}

if [ -n "${KAFKA_SUPPORT}" ]; then
	echo "Support Kafka ${KAFKA_SUPPORT}"
	JARS="${JARS},${LIBS}/djobi-kafka-kafka1-%%VERSION%%.jar"
fi

if [ -n "${USERAGENT_PARSER}" ]; then
	echo "Support user-agent parser ${USERAGENT_PARSER}"
	JARS="${JARS},${LIBS}/djobi-filter-user_agent-bitwalker-%%VERSION%%.jar"
fi

: ${ASSETS_PATH:=$DJOBI_ROOT}

: ${DJOBI_CONFIG:="${ASSETS_PATH}/env/prod.conf"}
: ${DJOBI_LOG4J:="${ASSETS_PATH}/log4j.properties"}
: ${DJOBI_DEFINES:=""}

: ${DJOBI_JAVA_XX:=""}

[ $SUBMIT_MODE == "cluster" ] && waitAppCompletion="false" || waitAppCompletion="true"

JVM_EXTRA_OPTIONS="-XX:+UseCompressedOops -XX:+UseParallelGC ${DJOBI_JAVA_XX}"
JVM_DRIVER_OPTIONS="-Dlog4j.configuration=file:${DJOBI_LOG4J} -Dconfig.file=${DJOBI_CONFIG} ${DJOBI_DEFINES} "
JVM_WORKER_OPTIONS="-Dlog4j.configuration=file:log4j.properties"

SPARK_FILES="${DJOBI_LOG4J}"

if [ "$USE_AGENT" = true ] ; then
    export ELASTIC_APM_ENVIRONMENT="prod"
    export ELASTIC_APM_SERVICE_VERSION="%%VERSION%%"
    export ELASTIC_APM_AGENT_JAR=elastic-apm-agent-1.18.0.RC1.jar

    JVM_DRIVER_OPTIONS="${JVM_DRIVER_OPTIONS} -javaagent:/home/hdfs/${ELASTIC_APM_AGENT_JAR}"
    JVM_WORKER_OPTIONS="${JVM_WORKER_OPTIONS} -javaagent:${ELASTIC_APM_AGENT_JAR}"

    SPARK_FILES="${SPARK_FILES},/home/hdfs/${ELASTIC_APM_AGENT_JAR}"
    AGENT_CONFIG="-Delastic.apm.service_name=djobi -Delastic.apm.server_urls=${APM_URL} -Delastic.apm.verify_server_cert=false -Delastic.apm.disable_instrumentations=okhttp,jdbc,asynchttpclient,concurrent,servlet-api-async,servlet-api,jax-rs,jax-ws,render,quartz,executor,annotations"
    JVM_EXTRA_OPTIONS="${JVM_EXTRA_OPTIONS} ${AGENT_CONFIG}"
fi

set -ex

exec $SPARK_SUBMIT \
    --jars "${JARS}" \
    --class io.datatok.djobi.Main \
    --name ${JOB_NAME} \
    --master ${SPARK_MASTER} \
    --num-executors ${SPARK_WORKERS} \
    --deploy-mode ${SUBMIT_MODE} \
    --driver-memory ${SPARK_DRIVER_MEMORY} \
    --driver-cores ${SPARK_DRIVER_CORES} \
    --executor-memory ${SPARK_WORKER_MEMORY} \
    --executor-cores ${SPARK_CORES} \
    --files ${SPARK_FILES} \
    --driver-java-options "${JVM_DRIVER_OPTIONS} ${JVM_EXTRA_OPTIONS} " \
    --conf "spark.memory.fraction=0.2" \
    --conf "spark.sql.shuffle.partitions=${SPARK_SHUFFLE_PARTITIONS}" \
    --conf "spark.executor.extraJavaOptions=${JVM_EXTRA_OPTIONS} ${JVM_WORKER_OPTIONS}" \
    --conf "spark.sql.parquet.compression.codec=snappy" \
    --conf "spark.yarn.submit.waitAppCompletion=${waitAppCompletion}" \
    --conf "spark.yarn.am.nodeLabelExpression=master" \
    --queue "${YARN_QUEUE}" \
    --conf "spark.rdd.compress=true" \
    --conf "spark.ui.port=${SPARK_UI_PORT}" \
    ${LIBS}/djobi-app-%%VERSION%%.jar run $@