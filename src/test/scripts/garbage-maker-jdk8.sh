#!/bin/sh
#
# Generates vm logging for various collectors/settings.
#
# Usage: sh ./garbage-maker-jdk8.sh 
#

VMCAT_HOME=~/workspace/vmcat/target
GARBAGECAT_HOME=~/workspace/garbagecat/target
GARBAGECAT_VERSION=-3.0.5-SNAPSHOT

##### Create VM Logging #####

# serial new / serial old #
java -Xms1m -Xmx64m -XX:+UseSerialGC -XX:+UnlockDiagnosticVMOptions -XX:+LogVMOutput -XX:+PrintSafepointStatistics -XX:PrintSafepointStatisticsCount=1 -XX:LogFile=$VMCAT_HOME/jdk8-serial-new-serial-old.log -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

# parallel scavenge / parallel serial old. #
java -Xms1m -Xmx64m -XX:+UseParallelGC -XX:-UseParallelOldGC -XX:+UnlockDiagnosticVMOptions -XX:+LogVMOutput -XX:+PrintSafepointStatistics -XX:PrintSafepointStatisticsCount=1 -XX:LogFile=$VMCAT_HOME/jdk8-parallel-scavenge-parallel-serial-old.log -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

# parallel scavenge / parallel old compacting #
java -Xms1m -Xmx64m -XX:+UseParallelGC -XX:+UseParallelOldGC -XX:+UnlockDiagnosticVMOptions -XX:+LogVMOutput -XX:+PrintSafepointStatistics -XX:PrintSafepointStatisticsCount=1 -XX:LogFile=$VMCAT_HOME/jdk8-parallel-scavenge-parallel-old-compacting.log -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

# par new / parallel serial old #
java -Xms1m -Xmx64m -XX:+UseParNewGC -XX:-UseParallelOldGC -XX:+UnlockDiagnosticVMOptions -XX:+LogVMOutput -XX:+PrintSafepointStatistics -XX:PrintSafepointStatisticsCount=1 -XX:LogFile=$VMCAT_HOME/jdk8-par-new-parallel-serial-old.log -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

# par new / cms #
java -Xms1m -Xmx64m -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+UnlockDiagnosticVMOptions -XX:+LogVMOutput -XX:+PrintSafepointStatistics -XX:PrintSafepointStatisticsCount=1 -XX:LogFile=$VMCAT_HOME/jdk8-par-new-cms.log -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

# g1 #
java -Xms1m -Xmx64m -XX:+UseG1GC -XX:+UnlockDiagnosticVMOptions -XX:+LogVMOutput -XX:+PrintSafepointStatistics -XX:PrintSafepointStatisticsCount=1 -XX:LogFile=$VMCAT_HOME/jdk8-g1.log  -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log

# shenandoah #
java -Xms1m -Xmx64m -XX:+UseShenandoahGC -XX:+UnlockDiagnosticVMOptions -XX:+LogVMOutput -XX:+PrintSafepointStatistics -XX:PrintSafepointStatisticsCount=1 -XX:LogFile=$VMCAT_HOME/jdk8-shenandoah.log -jar $GARBAGECAT_HOME/garbagecat$GARBAGECAT_VERSION.jar -o /dev/null ./gc.log
