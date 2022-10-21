from email.policy import default
import click, os, subprocess

from djobi_submit import __app_name__, __version__

@click.group()
def cli():
    pass

@cli.command( context_settings=dict(
    ignore_unknown_options=True,
    allow_extra_args=True,
))
@click.option("--name", default="djobi", help="The job name")
@click.option("--apm-server-url", help="APM server URL, to use APM agent.")
@click.option("--support-kafka", is_flag=True, help="Add kafka library.")
@click.option("--support-elasticsearch", is_flag=True, help="Add elasticsearch library.")
@click.option("--support-user-agent-parser", is_flag=True, help="Add user-agent parser filter.")
@click.option("--config-file", help="")
@click.option("--djobi-conf", multiple=True, type=(str, str), help="Override Djobi config")

@click.option("--master", default="localhost[*]", help="The spark master URL")
@click.option("--driver-java-options", default="", help="Extra driver java options.")
@click.option("--driver-memory", default="800Mi", help="Spark driver memory.")
@click.option("--driver-cores", default=1, help="Spark driver cores.")
@click.option("--executor-java-options", default="", help="Extra driver java options.")
@click.option("--executor-instances", default=1, help="How many spark executor instances.")
@click.option("--executor-memory", default="1Gi", help="Spark executor memory.")
@click.option("--executor-cores", default=1, help="Spark executor cores.")
@click.option("--spark-conf", multiple=True, type=(str, str), help="Extra Spark conf")

@click.argument("workflow-path")
@click.pass_context
def run(
    ctx,
    name,
    pipeline_path: str,
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
    djobi_version       = os.getenv("DJOBI_VERSION")
    djobi_home          = os.getenv("DJOBI_HOME")
    log4j               = f"{djobi_home}/log4j.properties"
    JVMCommonOtions     = f"-XX:+UseCompressedOops -XX:+UseParallelGC -Dconfig.override_with_env_vars=true"
    JVMDriverOptions    = f"{driver_java_options} -Dlog4j.configuration=file:{log4j} -Dconfig.file={config_file} {JVMCommonOtions} "
    JVMExecutorOptions  = f"{executor_java_options} -Dlog4j.configuration=file:log4j.properties {JVMCommonOtions}"
    
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
        JVMDriverOptions    = f"{JVMDriverOptions} -javaagent:{elastic_home}/elastic-apm-agent.jar {apm_agent_config}"
        JVMExecutorOptions  = f"{JVMExecutorOptions} -javaagent:elastic-apm-agent.jar {apm_agent_config}"
        
        spark_files.append(f"${elastic_home}/elastic-apm-agent.jar")
    
    spark_conf_list = list(spark_conf)
        
    spark_conf_list.append(("spark.executor.extraJavaOptions", JVMExecutorOptions))
    
    for k,v in djobi_conf:
        extra_env_variables[f"CONFIG_FORCE_{k}"] = v 
    
    args1 = [
        "spark-submit",
        "--jars", ",".join(spark_jars),
        "--class", "io.datatok.djobi.Main",
        "--name", name,
        "--master", master,
        "--num-executors", str(executor_instances),
        "--executor-memory", executor_memory,
        "--executor-cores", str(executor_cores),
        "--deploy-mode", "client",
        "--driver-memory", driver_memory,
        "--driver-cores", str(driver_cores),
        "--files", ",".join(spark_files),
        "--driver-java-options", JVMDriverOptions
    ]
    
    args2 = []
    
    for k,v in spark_conf_list:
        args2.append("--conf")
        args2.append(f"{k}={v}")
    
    args3 = [
        f"{djobi_home}/libs/djobi-cli-{djobi_version}.jar",
        "run",
        " ".join(ctx.args)
    ]
    
    args = [ *args1, *args2, *args3 ]
    
    out_cmd = ""
    
    for k,v in extra_env_variables.items():
        out_cmd = f"{out_cmd} {k}={v} "
    
    out_cmd_args = " ".join(args)
    
    out_cmd = f"{out_cmd} {out_cmd_args}"
    
    print(out_cmd)
    
    #p = subprocess.run(args, shell=True, env=extra_env_variables)