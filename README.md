# vmcat
A command line tool to parse Java -XX:+LogVMOutput and do analysis to support JVM tuning and troubleshooting for OpenJDK and Oracle JDK.

## Supports

  * OpenJDK
  * Oracle JDK
  
 ### Recommended GC Logging Options
 
 >-XX:+UnlockDiagnosticVMOptions -XX:+LogVMOutput -XX:+PrintSafepointStatistics -XX:PrintSafepointStatisticsCount=1
 
## Building

Get source:
```
git clone https://github.com/mgm3746/garbagecat.git
```

Build it:
```
cd garbagecat
mvn clean (rebuilding)
mvn assembly:assembly
mvn javadoc:javadoc
```

## Usage

```
java -jar vmcat-1.0.0-SNAPSHOT.jar --help
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