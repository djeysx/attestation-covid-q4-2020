#!/bin/bash

cd `dirname $0`

export JAVA_HOME=/ccv/opt/dsi_editique/jdk8
export PATH=$JAVA_HOME/bin:$PATH
JAVA=java
OPTS="-Xmx192M"

PORT=8083
APPNAME=${project.artifactId}
JARFILE=${project.build.finalName}.jar
APPOPTS="--spring.config.additional-location=file:../etc/application.yaml --port=$PORT"

CMD="$JAVA $OPTS -jar $JARFILE $APPOPTS"

statusme() {
	pgrep -a -f "$CMD"
	case "$?" in
		0)	echo "active" ; exit $? ;;
		1)	echo "inactive" ; exit $? ;;
		*)	echo "error" ; exit $? ;;
	esac
}

isStopped() {
	pgrep -a -f "$CMD"
	case "$?" in
		0)	echo "active" ; exit 1 ;;
		1)	echo "inactive" ; exit 0 ;;
		*)	echo "error" ; exit $? ;;
	esac
}

startme() {
	echo "Starting $APPNAME"
	nohup $CMD >/dev/null &
    sleep 1
	statusme
}

debugme() {
	echo "Starting DEBUG $APPNAME"
	$CMD
}

stopme() {
	echo "Stopping $APPNAME..."
    pkill -f "$CMD"
    sleep 2
	isStopped
}

case "$1" in 
    start)   startme ;;
    stop)    stopme ;;
    restart) stopme; startme ;;
    status)  statusme ;;
    debug)  debugme ;;
    *) echo "usage: $0 status|start|stop|restart|debug" >&2
       exit 1
       ;;
esac
