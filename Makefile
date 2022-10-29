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

packaging/docker/build:
	docker-compose build djobi

.PHONY: build/release
build/release:
	gradle -Prelease.useLastTag=true clean djobiAssemble -x test

.PHONY: build
build:
	gradle -x :djobi-core:signArchives -x :djobi-tests:signArchives --no-parallel --rerun-tasks djobiAssemble testReport

.PHONY: test/ci
test/ci:
	gradle -x :djobi-core:signArchives -x :djobi-tests:signArchives --no-parallel --rerun-tasks test -DincludeIntegrationTests

.PHONY: test/integration
test/integration:
	gradle -x :djobi-core:signArchives -x :djobi-tests:signArchives --no-parallel --rerun-tasks test -DincludeIntegrationTests

.PHONY: code/dependencies
code/dependencies:
	gradle :djobi-core:dependencies > dep.html
	open dep.html

.PHONY: run/simple
run/simple:
	SPARK_HOME=~/Documents/spark-3.3.0-bin-hadoop3 \
	DJOBI_HOME=~/dev/datatok/djobi/build/release/  \
	DJOBI_CONF=~/dev/datatok/djobi/dev/default.conf \
	\
	python3 djobi-submit/djobi_submit run  \
        --driver-cores 1 \
        --executor-instances 1 \
        --support-elasticsearch 7 \
        --log-level info \
        ~/dev/datatok/djobi/dev/workflows/es2fs --arg date=yesterday > run.sh

	cat run.sh

	projectRoot=$(shell pwd) \
		sh run.sh
