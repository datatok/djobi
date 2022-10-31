#!/bin/sh

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

# Execute the container CMD under tini for better hygiene
exec tini -s -- "$@"
