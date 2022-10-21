# djobi-submit

A small python ``spark-submit`` wrapper to run djobi as spark application.

## Features

``djobi-submit`` is a python application

* python 3.10
* cli with ``click`` lib
* build a shell script ``exec spark-submit ...``
* packaged within Docker image
* todo: Read workflow.yml to read spark-submit options (executors, memory ...)

## Usage

```
# Print shell script
DJOBI_VERSION=beta  python -m djobi_submit run --config-file=/opt/djobi/prod.conf

# Execute
python -m djobi_submit run .... >> /tmp/cmd.sh

sh /tmp/cmd.sh
```

