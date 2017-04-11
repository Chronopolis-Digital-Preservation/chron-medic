#!/bin/sh

### BEGIN INIT INFO
# Provides:      chron-repiar
# Default-Start: 3 5
# Default-Stop:  0 1 2 6
# Description:   Start the Chronopolis Repair service
### END INIT INFO

# User to execute as
CHRON_USER="chronopolis"

REP_JAR="/usr/lib/chronopolis/chron-repair.jar"
REP_PID_FILE="/var/run/chron-repair"

# Set the location which holds our repair config
export SPRING_CONFIG_NAME="repair"
export SPRING_CONFIG_LOCATION="/etc/chronopolis/"

JAVA_BIN=/usr/bin/java
JAVA_CMD="$JAVA_BIN -jar $REP_JAR"
PARAMS=" &"

. /etc/init.d/functions

RETVAL=0
COUNTDOWN=1

case "$1" in
    start)
    echo "Starting the chron repair service"
    daemon --user "$CHRON_USER" --pidfile "$REP_PID_FILE" $JAVA_CMD $PARAMS > /dev/null 2>&1
    RETVAL=$?

    echo "Waiting for startup to complete..."
    while [ $COUNTDOWN -gt 0 ]; do
        sleep 8 # This seems to be the minimum amount of time to get consistent results
        let COUNTDOWN=0
    done

    RUNNING=1
    # This from the jenkins init script... slightly modified for our use
    if [ $RETVAL -eq 0 ]; then
        # Create a pipe to read from so we can still alter $RUNNING
        if [ ! -p check_pipe ]; then
            mkfifo check_pipe
        fi

        /bin/ps hww -u "$CHRON_USER" -o sess,ppid,pid,cmd > check_pipe &
        while read sess ppid pid cmd; do
            [ $ppid -eq 1 ] || continue
            echo "$cmd" | grep $REP_JAR > /dev/null
            [ $? -eq 0 ] || continue
            echo $pid > $REP_PID_FILE
            let RUNNING=0
            # echo $RUNNING
        done < check_pipe
    fi

    if [ $RUNNING -eq 0 ]; then
        success
    else
        failure
    fi

    RETVAL=$RUNNING
    ;;
    stop)
    echo "Stopping the chron repair service"
    killproc -p $REP_PID_FILE
    ;;
    restart)
    $0 stop
    $0 start
    ;;
    status)
        status chron-repair
        RETVAL=$?
    ;;
esac

exit $RETVAL
