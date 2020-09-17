# vmcat
A command line tool to parse Java -XX:+LogVMOutput and do analysis to support JVM tuning and troubleshooting for OpenJDK and Oracle JDK.

## Supports

  * OpenJDK
  * Oracle JDK
  
 ### Recommended JVM Options
 
 >-XX:+UnlockDiagnosticVMOptions -XX:+LogVMOutput -XX:+PrintSafepointStatistics -XX:PrintSafepointStatisticsCount=1
 
## Building

Get source:
```
git clone https://github.com/mgm3746/vmcat.git
```

Build it:
```
cd vmcat
mvn clean (rebuilding)
mvn assembly:assembly
mvn javadoc:javadoc
```

## Usage

```
java -jar vmcat-1.0.2-SNAPSHOT.jar --help
usage: vmcat [OPTION]... [FILE]
 -h,--help              help
 -l,--latest            latest version
 -o,--output <arg>      output file name (default report.txt)
 -t,--threshold <arg>   threshold (0-100) for throughput bottleneck
                        reporting
 -v,--version           version
```

Notes:
  1. By default a report called report.txt is created in the directory where the **vmcat** tool is run. Specifying a custom name for the output file is useful when analyzing multiple vm logs.
  1. Version information is included in the report by using the version and.or latest version options.
  1. Preprocessing is sometimes required (e.g. when non-standard JVM options are used). It removes extraneous logging and makes any format adjustments needed for parsing (e.g. combining logging that the JVM sometimes splits across multiple lines). 
  1. If threshold is not defined, it defaults to 90.
  1. Throughput = (Time outside safepoint) / (Total Time). Throughput of 100 means no time spent in safepoint (good). Throughput of 0 means all time spent in safepoint (bad).

## Report

```
========================================
Throughput less than 5%
----------------------------------------
822.685: RevokeBias                       [    3077          0              0    ]      [     0     0     0    15     0    ]  0
822.703: BulkRevokeBias                   [    3077          0              1    ]      [     0     0     0    15    58    ]  0
...
4190.458: RevokeBias                       [    3085          0              4    ]      [     0     0     1    14     0    ]  0
4190.475: BulkRevokeBias                   [    3085          0              1    ]      [     0     0     0    13    39    ]  0
========================================
JVM:
----------------------------------------
Version: OpenJDK 64-Bit Server VM (25.201-b09) for linux-amd64 JRE (1.8.0_201-b09), built on Mar  
5 2019 10:14:09 by &quot;mockbuild&quot; with gcc 4.4.7 20120313 (Red Hat 4.4.7-23)
Options: -Xms4G -Xmx4G -XX:+UnlockDiagnosticVMOptions -XX:MetaspaceSize=512M 
-XX:MaxMetaspaceSize=1024M -XX:+PrintSafepointStatistics -XX:PrintSafepointStatisticsCount=1 
-XX:+LogVMOutput -XX:LogFile=vm.log -verbose:gc -Xloggc:gc.log -XX:+PrintGCDetails 
-XX:+PrintGCTimeStamps -XX:+HeapDumpOnOutOfMemoryError -XX:+PrintGCApplicationStoppedTime 
-XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=5 -XX:GCLogFileSize=10M
========================================
SUMMARY:
----------------------------------------
Throughput: 88%
Max Pause: 3.121 secs
Total Pause: 900.246 secs
First Timestamp: 0.159 secs
Last Timestamp: 7677.815 secs
========================================
TRIGGERS:
----------------------------------------
                                       #    Time (s)            Max (s)
ParallelGCFailedAllocation          8491     613.913    68%       3.121
RevokeBias                         13007     225.595    25%       1.118
BulkRevokeBias                       725      45.937     5%       0.592
no vm operation                      535       9.594     1%       0.074
ParallelGCSystemGC                    38       2.902    ~0%       0.352
ForceSafepoint                        73       1.388    ~0%       0.046
Deoptimize                            97       0.917    ~0%       0.084
EnableBiasedLocking                    1      ~0.000    ~0%      ~0.000
========================================

```