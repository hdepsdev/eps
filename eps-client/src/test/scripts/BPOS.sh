#! /bin/bash
CLASSPATH=.
LIBDIR=`ls lib/*.jar`
for jar in $LIBDIR
  do
	CLASSPATH=$CLASSPATH:$jar
  done
./jre/bin/java -cp $CLASSPATH com.bhz.eps.BPosEmulator

