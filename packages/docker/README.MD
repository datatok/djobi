## Dependencies

* base image: alpine:3.16
* java 17 (JRE)

## Entrypoint

``entrypoint.sh`` is a wrapper to the python application ``djobi-submit``.

The python application ``djobi-submit`` is a wrapper to spark-submit which run djobi application.

```
entrypoint.sh -> python djobi-submit -> spark-submit
```

## Docs

* https://www.docker.com/blog/containerized-python-development-part-1/
