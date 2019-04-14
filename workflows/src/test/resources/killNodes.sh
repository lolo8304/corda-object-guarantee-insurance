#!/usr/bin/env bash
DIR=`dirname "$0"`

echo "still `ps -ef | grep corda | grep java | grep -v IntelliJ | wc -l` running"
ps -ef | grep corda | grep java | grep -v IntelliJ | awk '{print "kill -9 " $2  }'

