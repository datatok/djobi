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

build/release:
	gradle -Prelease.useLastTag=true clean djobiAssemble -x test