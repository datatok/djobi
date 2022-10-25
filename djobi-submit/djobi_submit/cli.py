from email.policy import default
import click, os, subprocess

@click.group()
def cli():
    pass

@cli.command( context_settings=dict(
    ignore_unknown_options=True,
    allow_extra_args=True,
))
@click.option("--name", default="djobi", help="The job name", envvar="DJOBI_NAME")
@click.option("--apm-server-url", help="APM server URL, to use APM agent.", envvar="DJOBI_APM_SERVER_URL")
@click.option("--support-kafka", is_flag=True, help="Add kafka library.", envvar="DJOBI_SUPPORT_KAFKA")
@click.option("--support-elasticsearch", is_flag=True, help="Add elasticsearch library.", envvar="DJOBI_SUPPORT_ELASTICSEARCH")
@click.option("--support-user-agent-parser", is_flag=True, help="Add user-agent parser filter.", envvar="DJOBI_SUPPORT_UA_PARSER")
@click.option("--config-file", help="", envvar="DJOBI_CONF")
@click.option("--djobi-conf", multiple=True, type=(str, str), help="Override Djobi config")

@click.option("--master", default="localhost[*]", help="The spark master URL", envvar="SPARK_MASTER")
@click.option("--driver-java-options", default="", help="Extra driver java options.", envvar="SPARK_DRIVER_JAVA_OPTS")
@click.option("--driver-memory", default="800M", help="Spark driver memory.", envvar="SPARK_DRIVER_MEMORY")
@click.option("--driver-cores", default=1, help="Spark driver cores.", envvar="SPARK_DRIVER_CORES")
@click.option("--executor-java-options", default="", help="Extra executor java options.", envvar="SPARK_EXECUTOR_JAVA_OPTS")
@click.option("--executor-instances", default=1, help="How many spark executor instances.", envvar="SPARK_EXECUTORS")
@click.option("--executor-memory", default="1G", help="Spark executor memory.", envvar="SPARK_EXECUTOR_MEMORY")
@click.option("--executor-cores", default=1, help="Spark executor cores.", envvar="SPARK_EXECUTOR_CORES")
@click.option("--spark-conf", multiple=True, type=(str, str), help="Extra Spark conf")

@click.pass_context
def run(
    ctx,
    name,
    apm_server_url: str,
    support_kafka: bool,
    support_elasticsearch: bool,
    support_user_agent_parser: bool,
    config_file: str,
    djobi_conf: tuple,
    master: str,
    driver_java_options: str,
    driver_memory: str,
    driver_cores: int,
    executor_java_options: str,
    executor_instances: int,
    executor_memory: str,
    executor_cores: int,
    spark_conf: tuple
) -> None:
    
    extra_env_variables = {}
    spark_files         = []
    spark_jars          = []
    djobi_home          = os.getenv("DJOBI_HOME")
    spark_home          = os.getenv("SPARK_HOME")
    log4j               = f"{djobi_home}/log4j.properties"
    JVMCommonOtions     = f"-XX:+UseCompressedOops -XX:+UseParallelGC -Dconfig.override_with_env_vars=true"
    JVMDriverOptions    = f"{driver_java_options} -Dlog4j.configuration=file:{log4j} -Dconfig.file={config_file} {JVMCommonOtions} "
    JVMExecutorOptions  = f"{executor_java_options} -Dlog4j.configuration=file:log4j.properties {JVMCommonOtions}"

    if not spark_home:
        print("spark_home is empty, SPARK_HOME env variable is missing?")
        exit(1)

    if not djobi_home:
        print("djobi_home is empty, DJOBI_HOME env variable is missing?")
        exit(1)

    with open(f"{djobi_home}/VERSION") as f:
        djobi_version = f.readline()

    if not djobi_version:
        print("djobi_version is empty, ${DJOBI_HOME}/VERSION file is missing?")
        exit(1)

    if not config_file:
        print("config_file is empty, DJOBI_CONF env variable is missing?")
        exit(1)
    
    spark_jars.append(f"{djobi_home}/libs/djobi-core-{djobi_version}.jar")
    
    if support_kafka:
        spark_jars.append(f"{djobi_home}/libs/djobi-kafka-kafka1-{djobi_version}.jar")
        
    if support_elasticsearch:
        spark_jars.append(f"{djobi_home}/libs/djobi-elasticsearch-es7-{djobi_version}.jar")

    if support_user_agent_parser:
        spark_jars.append(f"{djobi_home}/libs/djobi-filter-user_agent-bitwalker-{djobi_version}.jar")
    
    if apm_server_url is not None and len(apm_server_url) > 0:
        extra_env_variables["ELASTIC_APM_ENVIRONMENT"] ="prod"
        extra_env_variables["ELASTIC_APM_SERVICE_VERSION"] = djobi_version

        elastic_home        = os.getenv("ELASTIC_HOME")
        apm_agent_config    = f"-Delastic.apm.service_name=djobi -Delastic.apm.server_urls={apm_server_url} -Delastic.apm.verify_server_cert=false -Delastic.apm.disable_instrumentations=okhttp,jdbc,asynchttpclient,concurrent,servlet-api-async,servlet-api,jax-rs,jax-ws,render,quartz,executor,annotations"
        JVMDriverOptions    = trim(f"{JVMDriverOptions} -javaagent:{elastic_home}/elastic-apm-agent.jar {apm_agent_config}", " ")
        JVMExecutorOptions  = trim(f"{JVMExecutorOptions} -javaagent:elastic-apm-agent.jar {apm_agent_config}", " ")
        
        spark_files.append(f"${elastic_home}/elastic-apm-agent.jar")
    
    spark_conf_list = list(spark_conf)
        
    spark_conf_list.append(("spark.executor.extraJavaOptions", JVMExecutorOptions))
    
    for k,v in djobi_conf:
        kk = k.replace(".", "_")
        extra_env_variables[f"CONFIG_FORCE_{kk}"] = v
        
    buffer_jars = ",".join(spark_jars)
    buffer_djobi_args = " ".join(ctx.args)
    buffer_spark_conf = ""
    buffer_env_vars = ""
    
    if len(spark_files):
        buffer_files = "--files" + (",".join(spark_files))
    else:
        buffer_files = ""
    
    for k,v in spark_conf_list:
        buffer_spark_conf = f"{buffer_spark_conf} --conf '{k}={v}'"
        
    for k,v in extra_env_variables.items():
        buffer_env_vars = f"{buffer_env_vars}export {k}={v} \n"
        
    out_cmd = f"""
{buffer_env_vars}
exec {spark_home}/bin/spark-submit \
--jars {buffer_jars} \
--class io.datatok.djobi.Main \
--name {name} \
--master '{master}' \
--num-executors {executor_instances} \
--executor-memory {executor_memory} \
--executor-cores {executor_cores} \
--deploy-mode client \
--driver-memory {driver_memory} \
--driver-cores {driver_cores} \
--driver-java-options=\"{JVMDriverOptions}\" \
{buffer_spark_conf} \
{buffer_files} \
{djobi_home}/libs/djobi-cli-{djobi_version}.jar \
run \
{buffer_djobi_args}
    """
    
    print(out_cmd)
    
    #p = subprocess.run(args, shell=True, env=extra_env_variables)
