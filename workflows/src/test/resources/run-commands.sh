#!/usr/bin/env bash
OLDDIR=`pwd`
cd "../../../../build/nodes/"
DIR=`pwd`
cd $OLDDIR

function start_corda_node {
    local name=$1
    local filename=_$name
    local debugPort=$2
    local agentPort=$3
    echo "start $name on port $debugPort / $agentPort"
    echo 'cd "'$DIR'/'$name'" ; "/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/jre/bin/java" "-Dname='$name'" "-Dcapsule.jvm.args=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address='$debugPort' -javaagent:drivers/jolokia-jvm-1.6.0-agent.jar=port='$agentPort',logHandlerClass=net.corda.node.JolokiaSlf4jAdapter" "-jar" "corda.jar"; exit' > $filename.command;chmod u+x $filename.command;open $filename.command

}

function start_web_node {
    local name=$1
    local filename=_$name-Web
    local debugPort=$2
    local agentPort=$3
    echo 'cd "'$DIR'/'$name'" ; "/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/jre/bin/java" "-Dname='$name'-web" "-Dcapsule.jvm.args=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address='$debugPort'" "-jar" "corda-webserver.jar"; exit' > $filename.command;chmod u+x $filename.command;open $filename.command
}

notar=`grep "name.*O=" ../../../../build.gradle | sed "s/.*O=\(.*\),L=.*/\1/" | grep Notar`
nodes=`grep "name.*O=" ../../../../build.gradle | sed "s/.*O=\(.*\),L=.*/\1/" | grep -v Notar`

port1=5005
port2=7005
start_corda_node "$notar" "$port1" "$port2"

for node in $nodes; do
    port1=$((port1+1))
    port2=$((port2+1))
    start_corda_node "$node" "$port1" "$port2"
done

for node in $nodes; do
    port1=$((port1+1))
    port2=$((port2+1))
    start_web_node "$node" "$port1" "$port2"
done
