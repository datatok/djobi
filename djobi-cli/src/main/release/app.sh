#!/usr/bin/env bash

# This run script dir (resolve to absolute path)
SCRIPT_DIR=$(cd $(dirname $0) && pwd)    # This dir is where this script live.
APP_DIR=$(cd $SCRIPT_DIR/.. && pwd)      # Assume the application dir is one level up from script dir.

# Default parameters
JAVA_HOME=${JAVA_HOME:=/apps/jdk}        # This is the home directory of Java development kit.
RUN_JAVA_CP=${RUN_JAVA_CP:=$CLASSPATH}     # A classpath prefix before -classpath option, default to $CLASSPATH
RUN_JAVA_OPTS=${RUN_JAVA_OPTS:=}           # Java options (-Xmx512m -XX:MaxPermSize=128m etc)
RUN_JAVA_DEBUG=${RUN_JAVA_DEBUG:=}         # If not empty, print the full java command line before executing it.
RUN_JAVA_NO_PARSE=${RUN_JAVA_NO_PARSE:=}   # If not empty, skip the auto parsing of -D and -cp options from script arguments.
RUN_JAVA_NO_AUTOCP=${RUN_JAVA_NO_AUTOCP:=} # If not empty, do not auto setup Java classpath
RUN_JAVA_DRY=${RUN_JAVA_DRY:=}             # If not empty, do not exec Java command, but just print

# Djobi options
: ${ES_SUPPORT:=""}
: ${KAFKA_SUPPORT:=""}
: ${USERAGENT_PARSER:=""}
: ${USE_AGENT:=true}
: ${DJOBI_JAR_SPARK:=false}

# Djobi specific stuff
DJOBI_MAIN=io.datatok.djobi.Main

DJOBI_LIBS_FOLDER=${DJOBI_LIBS_FOLDER:=$SCRIPT_DIR/libs}
JARS="${DJOBI_LIBS_FOLDER}/djobi-core-%%VERSION%%.jar:${DJOBI_LIBS_FOLDER}/djobi-cli-%%VERSION%%.jar"

if [ -n "${ES_SUPPORT}" ]; then
	JARS="${JARS}:${DJOBI_LIBS_FOLDER}/djobi-elasticsearch-es${ES_SUPPORT}-%%VERSION%%.jar"
fi

if [ -n "${KAFKA_SUPPORT}" ]; then
	echo "Support Kafka ${KAFKA_SUPPORT}"
	JARS="${JARS}:${DJOBI_LIBS_FOLDER}/djobi-kafka-kafka1-%%VERSION%%.jar"
fi

if [ -n "${USERAGENT_PARSER}" ]; then
	echo "Support user-agent parser ${USERAGENT_PARSER}"
	JARS="${JARS}:${DJOBI_LIBS_FOLDER}/djobi-filter-user_agent-bitwalker-%%VERSION%%.jar"
fi

if [ "${DJOBI_JAR_SPARK}" = true ]; then
	JARS="${JARS}:${DJOBI_LIBS_FOLDER}/djobi-core-%%VERSION%%-spark-assembly-provided.jar"
fi

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

RUN_JAVA_CP="$RUN_JAVA_CP:$JARS"

JVM_EXTRA_OPTIONS="-XX:+UseCompressedOops -XX:+UseParallelGC"
JVM_DRIVER_OPTIONS="-Dconfig.file=${DJOBI_CONFIG} "

RUN_JAVA_OPTS="${JVM_EXTRA_OPTIONS} ${JVM_DRIVER_OPTIONS}"

# Define where is the java executable is
JAVA_CMD=java
if [ -d "$JAVA_HOME" ]; then
	JAVA_CMD="$JAVA_HOME/bin/java"
fi

# Display full Java command.
if [ -n "$RUN_JAVA_DEBUG" ] || [ -n "$RUN_JAVA_DRY" ]; then
  echo "$JAVA_CMD" $RUN_JAVA_OPTS -cp "$RUN_JAVA_CP" $DJOBI_MAIN "$@"
fi

# Run Java Main class
if [ -z "$RUN_JAVA_DRY" ]; then
  "$JAVA_CMD" $RUN_JAVA_OPTS -cp "$RUN_JAVA_CP" $DJOBI_MAIN "$@"
fi