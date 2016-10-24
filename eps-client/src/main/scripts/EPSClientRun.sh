#! /bin/bash
chmod 777 ./jre/bin/java
CLASSPATH=.
LIBDIR=`ls lib/*.jar`
for jar in $LIBDIR
  do
	CLASSPATH=$CLASSPATH:$jar
  done
./jre/bin/java -cp $CLASSPATH com.bhz.eps.Boot

