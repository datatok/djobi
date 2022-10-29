#!/bin/sh

# shellcheck disable=SC1091

#set -o errexit
#set -o nounset
#set -o pipefail
#set -o xtrace

if [ $EUID -eq 0 ]
then
    info "<!> dont run as root <!>"
fi

set -e

# Check whether there is a passwd entry for the container UID
myuid=$(id -u)
mygid=$(id -g)
# turn off -e for getent because it will return error code in anonymous uid case
set +e
uidentry=$(getent passwd $myuid)
set -e

# If there is no passwd entry for the container UID, attempt to create one
if [ -z "$uidentry" ]
then
    if [ -w /etc/passwd ]
    then
	    echo "$myuid:x:$myuid:$mygid:${SPARK_USER_NAME:-anonymous uid}:$SPARK_HOME:/bin/false" >> /etc/passwd
    else
	    echo "Container ENTRYPOINT failed to add passwd entry for anonymous UID"
    fi
fi

DJOBI_VERSION=$(cat "${DJOBI_HOME}/VERSION")

export DJOBI_VERSION

case "$1" in
  "")
    set -- python3 submit/djobi_submit/ run --help
    ;;
  djobi)
    shift 1

    echo "#!/bin/sh" > /tmp/cmd.sh

    python3 submit/djobi_submit/ run "$@" >> /tmp/cmd.sh

    chmod +x /tmp/cmd.sh

    cat /tmp/cmd.sh

    set -- "/tmp/cmd.sh"
    ;;
esac

set -ex

# Execute the container CMD under tini for better hygiene
exec tini -s -- "$@"
