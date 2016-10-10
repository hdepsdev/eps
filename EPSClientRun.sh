#! /bin/bash
CLASSPATH=.
LIBDIR=`ls lib/*.jar`
for jar in $LIBDIR
  do
	CLASSPATH=$CLASSPATH:$jar
  done
java -cp $CLASSPATH com.bhz.eps.Boot

