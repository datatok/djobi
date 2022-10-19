cidre/release/start:
	ansible-playbook ./cidre.yaml --tags "release_start"

cidre/release/end:
	ansible-playbook ./cidre.yaml --tags "release_end"

cidre/milestone/update:
	ansible-playbook ./cidre.yaml --tags "milestone_update"

cidre/build:
	ansible-playbook ./cidre.yaml --tags "build"

dev/test:
	docker-compose -f "docker-compose.yml" -f "djobi-core/docker-compose-ci.yml" -f "djobi-kafka/docker-compose-ci.yml" run --rm gradle

.PHONY: build/release
build/release:
	gradle -Prelease.useLastTag=true clean djobiAssemble -x test

gradle/deps:
    ./gradlew djobi-core:dependencies > dep.html

run/simple:
	SPARK_HOME=~/Documents/spark-3.2.1-bin-hadoop3.2 \
	DJOBI_HOME=~/dev/datatok/djobi/build/release/  \
	DJOBI_VERSION=2 \
	DJOBI_CONF=~/dev/datatok/djobi/dev/default.conf \
	python -m djobi_submit run  \
        --driver-cores 1 \
        --executor-instances 1 \
        ~/dev/datatok/djobi/dev/pipelines/es2fs