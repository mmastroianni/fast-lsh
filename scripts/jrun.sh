#!/bin/bash

invocation=$*
this=${0##/}

usage() { 
	echo "usage: $this mainclass [args]" 
}

if [ $# -lt 1 ]; then
	usage
	exit 1
fi

jar_dir="`dirname $0`/../dist"

[ ! -d $jar_dir ] && echo "cannot read jars from $jar_dir" && exit 1

mainclass=$1
shift
mainopts=$*
java_opts="-Xms8G -Xmx16G"

classpath=
for jar in $jar_dir/*.jar; do 
	classpath=$classpath:$jar
done	

java $java_opts -cp $classpath $mainclass $mainopts

