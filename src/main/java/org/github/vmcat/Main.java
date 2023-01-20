/**********************************************************************************************************************
 * vmcat                                                                                                              *
 *                                                                                                                    *
 * Copyright (c) 2020-2023 Mike Millson                                                                                    *
 *                                                                                                                    * 
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License       * 
 * v. 2.0 which is available at https://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0 which is    *
 * available at https://www.apache.org/licenses/LICENSE-2.0.                                                          *
 *                                                                                                                    *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0                                                                     *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.github.vmcat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.github.vmcat.domain.Jvm;
import org.github.vmcat.domain.JvmRun;
import org.github.vmcat.domain.jdk.SafepointEventSummary;
import org.github.vmcat.service.Manager;
import org.github.vmcat.util.Constants;
import org.github.vmcat.util.jdk.Analysis;
import org.github.vmcat.util.jdk.JdkMath;
import org.github.vmcat.util.jdk.JdkUtil;
import org.github.vmcat.util.jdk.Safepoint;
import org.json.JSONObject;

/**
 * <p>
 * vmcat main class. A controller that prepares the model (by parsinglog entries) and provides analysis (the report
 * view).
 * </p>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class Main {

    /**
     * The maximum number of rejected log lines to track. A throttle to limit memory consumption.
     */
    public static final int REJECT_LIMIT = 1000;

    private static Options options;

    static {
        // Declare command line options
        options = new Options();
        options.addOption(Constants.OPTION_HELP_SHORT, Constants.OPTION_HELP_LONG, false, "help");
        options.addOption(Constants.OPTION_VERSION_SHORT, Constants.OPTION_VERSION_LONG, false, "version");
        options.addOption(Constants.OPTION_LATEST_VERSION_SHORT, Constants.OPTION_LATEST_VERSION_LONG, false,
                "latest version");
        options.addOption(Constants.OPTION_OUTPUT_SHORT, Constants.OPTION_OUTPUT_LONG, true,
                "output file name (default " + Constants.OUTPUT_FILE_NAME + ")");
        options.addOption(Constants.OPTION_THRESHOLD_SHORT, Constants.OPTION_THRESHOLD_LONG, true,
                "threshold (0-100) for throughput bottleneck reporting");
    }

    /**
     * @param args
     *            The argument list includes one or more scope options followed by the name of the vm log file to
     *            inspect.
     */
    public static void main(String[] args) {

        CommandLine cmd = null;

        try {
            cmd = parseOptions(args);
        } catch (ParseException pe) {
            System.out.println(pe.getMessage());
            usage(options);
        }

        if (cmd != null) {
            if (cmd.hasOption(Constants.OPTION_HELP_LONG)) {
                usage(options);
            } else {

                String logFileName = (String) cmd.getArgList().get(cmd.getArgList().size() - 1);
                File logFile = new File(logFileName);

                Manager manager = new Manager();

                // Store safepoint logging in data store.
                manager.store(logFile);

                // Create report
                Jvm jvm = new Jvm();
                // Determine report options
                int throughputThreshold = Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD;
                if (cmd.hasOption(Constants.OPTION_THRESHOLD_LONG)) {
                    throughputThreshold = Integer.parseInt(cmd.getOptionValue(Constants.OPTION_THRESHOLD_SHORT));
                }
                JvmRun jvmRun = manager.getJvmRun(jvm, throughputThreshold);
                String outputFileName;
                if (cmd.hasOption(Constants.OPTION_OUTPUT_LONG)) {
                    outputFileName = cmd.getOptionValue(Constants.OPTION_OUTPUT_SHORT);
                } else {
                    outputFileName = Constants.OUTPUT_FILE_NAME;
                }

                boolean version = cmd.hasOption(Constants.OPTION_VERSION_LONG);
                boolean latestVersion = cmd.hasOption(Constants.OPTION_LATEST_VERSION_LONG);
                createReport(jvmRun, outputFileName, version, latestVersion);
            }
        }
    }

    /**
     * Parse command line options.
     * 
     * @return
     */
    private static final CommandLine parseOptions(String[] args) throws ParseException {
        CommandLineParser parser = new BasicParser();
        CommandLine cmd = null;
        // Allow user to just specify help or version.
        if (args.length == 1 && (args[0].equals("-" + Constants.OPTION_HELP_SHORT)
                || args[0].equals("--" + Constants.OPTION_HELP_LONG))) {
            usage(options);
        } else if (args.length == 1 && (args[0].equals("-" + Constants.OPTION_VERSION_SHORT)
                || args[0].equals("--" + Constants.OPTION_VERSION_LONG))) {
            System.out.println("Running vmcat version: " + getVersion());
        } else if (args.length == 1 && (args[0].equals("-" + Constants.OPTION_LATEST_VERSION_SHORT)
                || args[0].equals("--" + Constants.OPTION_LATEST_VERSION_LONG))) {
            System.out.println("Latest vmcat version/tag: " + getLatestVersion());
        } else if (args.length == 2 && (((args[0].equals("-" + Constants.OPTION_VERSION_SHORT)
                || args[0].equals("--" + Constants.OPTION_VERSION_LONG))
                && (args[1].equals("-" + Constants.OPTION_LATEST_VERSION_SHORT)
                        || args[1].equals("--" + Constants.OPTION_LATEST_VERSION_LONG)))
                || ((args[1].equals("-" + Constants.OPTION_VERSION_SHORT)
                        || args[1].equals("--" + Constants.OPTION_VERSION_LONG))
                        && (args[0].equals("-" + Constants.OPTION_LATEST_VERSION_SHORT)
                                || args[0].equals("--" + Constants.OPTION_LATEST_VERSION_LONG))))) {
            System.out.println("Running vmcat version: " + getVersion());
            System.out.println("Latest vmcat version/tag: " + getLatestVersion());
        } else {
            cmd = parser.parse(options, args);
            validateOptions(cmd);
        }
        return cmd;
    }

    /**
     * Output usage help.
     * 
     * @param options
     */
    private static void usage(Options options) {
        // Use the built in formatter class
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("vmcat [OPTION]... [FILE]", options);
    }

    /**
     * Validate command line options.
     * 
     * @param cmd
     *            The command line options.
     * 
     * @throws ParseException
     *             Command line options not valid.
     */
    public static void validateOptions(CommandLine cmd) throws ParseException {
        // Ensure command line input.
        if (cmd.getArgList().size() == 0) {
            throw new ParseException("Missing input");
        } else {
            // Ensure file input.
            String logFileName = (String) cmd.getArgList().get(cmd.getArgList().size() - 1);
            if (logFileName == null) {
                throw new ParseException("Missing file");
            } else {
                // Ensure file exists.
                File logFile = new File(logFileName);
                if (!logFile.exists()) {
                    throw new ParseException("Invalid file: '" + logFileName + "'");
                }
            }
        }
        // threshold
        if (cmd.hasOption(Constants.OPTION_THRESHOLD_LONG)) {
            String thresholdRegEx = "^\\d{1,3}$";
            String thresholdOptionValue = cmd.getOptionValue(Constants.OPTION_THRESHOLD_SHORT);
            Pattern pattern = Pattern.compile(thresholdRegEx);
            Matcher matcher = pattern.matcher(thresholdOptionValue);
            if (!matcher.find()) {
                throw new ParseException("Invalid threshold: '" + thresholdOptionValue + "'");
            }
        }
    }

    /**
     * @return version string.
     */
    private static String getVersion() {
        ResourceBundle rb = ResourceBundle.getBundle("META-INF/maven/vmcat/vmcat/pom");
        return rb.getString("version");
    }

    /**
     * @return version string.
     */
    private static String getLatestVersion() {
        String url = "https://github.com/mgm3746/vmcat/releases/latest";
        String name = null;
        try {
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            httpClient = HttpClients.custom()
                    .setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build())
                    .build();
            HttpGet request = new HttpGet(url);
            request.addHeader("Accept", "application/json");
            request.addHeader("content-type", "application/json");
            HttpResponse result = httpClient.execute(request);
            String json = EntityUtils.toString(result.getEntity(), "UTF-8");
            JSONObject jsonObj = new JSONObject(json);
            name = jsonObj.getString("tag_name");
        }

        catch (Exception ex) {
            name = "Unable to retrieve";
            ex.printStackTrace();
        }
        return name;
    }

    /**
     * Create VM Log Analysis report.
     * 
     * @param jvmRun
     *            JVM run data.
     * @param reportFileName
     *            Report file name.
     * @param version
     *            Whether or not to report vmcat version.
     * @param latestVersion
     *            Whether or not to report latest vmcat version.
     * 
     */
    private static void createReport(JvmRun jvmRun, String reportFileName, boolean version, boolean latestVersion) {
        File reportFile = new File(reportFileName);
        FileWriter fileWriter = null;
        PrintWriter printWriter = null;
        try {
            fileWriter = new FileWriter(reportFile);
            printWriter = new PrintWriter(fileWriter);

            if (version || latestVersion) {
                printWriter.write("========================================" + Constants.LINE_SEPARATOR);
                if (version) {
                    printWriter.write("Running vmcat version: " + getVersion() + System.getProperty("line.separator"));
                }
                if (latestVersion) {
                    printWriter.write(
                            "Latest vmcat version/tag: " + getLatestVersion() + System.getProperty("line.separator"));
                }
            }

            // Bottlenecks
            List<String> bottlenecks = jvmRun.getBottlenecks();
            if (bottlenecks.size() > 0) {
                printWriter.write("========================================" + Constants.LINE_SEPARATOR);
                printWriter.write(
                        "Throughput less than " + jvmRun.getThroughputThreshold() + "%" + Constants.LINE_SEPARATOR);
                printWriter.write("----------------------------------------" + Constants.LINE_SEPARATOR);
                Iterator<String> iterator = bottlenecks.iterator();
                while (iterator.hasNext()) {
                    printWriter.write(iterator.next() + Constants.LINE_SEPARATOR);
                }
            }

            // JVM information
            if (jvmRun.getJvm().getVersion() != null || jvmRun.getJvm().getOptions() != null) {
                printWriter.write("========================================" + Constants.LINE_SEPARATOR);
                printWriter.write("JVM:" + Constants.LINE_SEPARATOR);
                printWriter.write("----------------------------------------" + Constants.LINE_SEPARATOR);
                if (jvmRun.getJvm().getVersion() != null) {
                    printWriter.write("Version: " + jvmRun.getJvm().getVersion() + Constants.LINE_SEPARATOR);
                }
                if (jvmRun.getJvm().getOptions() != null) {
                    printWriter.write("Options: " + jvmRun.getJvm().getOptions() + Constants.LINE_SEPARATOR);
                }
            }

            // Summary
            printWriter.write("========================================" + Constants.LINE_SEPARATOR);
            printWriter.write("SUMMARY:" + Constants.LINE_SEPARATOR);
            printWriter.write("----------------------------------------" + Constants.LINE_SEPARATOR);

            if (jvmRun.getSafepointEventCount() > 0) {
                // Throughput
                printWriter.write("Throughput: ");
                if (jvmRun.getThroughput() == 100 && jvmRun.getSafepointEventCount() > 0) {
                    // Provide clue it's rounded to 100
                    printWriter.write("~");
                }
                printWriter.write(jvmRun.getThroughput() + "%" + Constants.LINE_SEPARATOR);
                // Max pause
                BigDecimal maxPause = JdkMath.convertMillisToSecs(jvmRun.getMaxPause());
                printWriter.write("Max Pause: " + maxPause.toString() + " secs" + Constants.LINE_SEPARATOR);
                // Total pause time
                BigDecimal totalPause = JdkMath.convertMillisToSecs(jvmRun.getSafepointTotalPause());
                printWriter.write("Total Pause: " + totalPause.toString() + " secs" + Constants.LINE_SEPARATOR);
            }
            // First/last timestamps
            if (jvmRun.getSafepointEventCount() > 0) {
                // First event
                String firstEventDatestamp = JdkUtil.getDateStamp(jvmRun.getFirstSafepointEvent().getLogEntry());
                if (firstEventDatestamp != null) {
                    printWriter.write("First Datestamp: ");
                    printWriter.write(firstEventDatestamp);
                    printWriter.write(Constants.LINE_SEPARATOR);
                } else {
                    printWriter.write("First Timestamp: ");
                    BigDecimal firstEventTimestamp = JdkMath
                            .convertMillisToSecs(jvmRun.getFirstSafepointEvent().getTimestamp());
                    printWriter.write(firstEventTimestamp.toString());
                    printWriter.write(" secs" + Constants.LINE_SEPARATOR);
                }
                // Last event
                String lastEventDatestamp = JdkUtil.getDateStamp(jvmRun.getLastSafepointEvent().getLogEntry());
                if (lastEventDatestamp != null) {
                    printWriter.write("Last Datestamp: ");
                    printWriter.write(lastEventDatestamp);
                    printWriter.write(Constants.LINE_SEPARATOR);
                } else {
                    printWriter.write("Last Timestamp: ");
                    BigDecimal lastEventTimestamp = JdkMath
                            .convertMillisToSecs(jvmRun.getLastSafepointEvent().getTimestamp());
                    printWriter.write(lastEventTimestamp.toString());
                    printWriter.write(" secs" + Constants.LINE_SEPARATOR);
                }
            }

            // Triggers
            printWriter.write("========================================" + Constants.LINE_SEPARATOR);
            printWriter.write("TRIGGERS:" + Constants.LINE_SEPARATOR);
            printWriter.write("----------------------------------------" + Constants.LINE_SEPARATOR);

            if (jvmRun.getSafepointEventCount() > 0) {
                printWriter.printf("%30s%10s%12s%7s%12s%n", "", "#", "Time (s)", "", "Max (s)");
                List<SafepointEventSummary> summaries = jvmRun.getSafepointEventSummaries();
                Iterator<SafepointEventSummary> iterator = summaries.iterator();
                while (iterator.hasNext()) {
                    SafepointEventSummary summary = iterator.next();
                    BigDecimal pauseTotal = JdkMath.convertMillisToSecs(summary.getPauseTotal());
                    String pauseTotalString = null;
                    if (pauseTotal.toString().equals("0.000")) {
                        // give rounding hint
                        pauseTotalString = "~" + pauseTotal.toString();
                    } else {
                        pauseTotalString = pauseTotal.toString();
                    }
                    BigDecimal percent = new BigDecimal(summary.getPauseTotal());
                    percent = percent.divide(new BigDecimal(jvmRun.getSafepointTotalPause()), 2,
                            RoundingMode.HALF_EVEN);
                    percent = percent.movePointRight(2);
                    String percentString = null;
                    if (percent.intValue() == 0) {
                        // give rounding hint
                        percentString = "~" + percent.toString();
                    } else {
                        percentString = percent.toString();
                    }
                    BigDecimal pauseMax = JdkMath.convertMillisToSecs(summary.getPauseMax());
                    String pauseMaxString = null;
                    if (pauseMax.toString().equals("0.000")) {
                        // give rounding hint
                        pauseMaxString = "~" + pauseMax.toString();
                    } else {
                        pauseMaxString = pauseMax.toString();
                    }
                    printWriter.printf("%-30s%10s%12s%6s%%%12s%n", Safepoint.getTriggerLiteral(summary.getTrigger()),
                            summary.getCount(), pauseTotalString, percentString, pauseMaxString);
                }
            }

            printWriter.write("========================================" + Constants.LINE_SEPARATOR);

            // Analysis
            List<Analysis> analysis = jvmRun.getAnalysis();
            if (!analysis.isEmpty()) {

                // Determine analysis levels
                List<Analysis> error = new ArrayList<Analysis>();
                List<Analysis> warn = new ArrayList<Analysis>();
                List<Analysis> info = new ArrayList<Analysis>();

                Iterator<Analysis> iterator = analysis.iterator();
                while (iterator.hasNext()) {
                    Analysis a = iterator.next();
                    String level = a.getKey().split("\\.")[0];
                    if (level.equals("error")) {
                        error.add(a);
                    } else if (level.equals("warn")) {
                        warn.add(a);
                    } else if (level.equals("info")) {
                        info.add(a);
                    }
                }

                printWriter.write("ANALYSIS:" + Constants.LINE_SEPARATOR);

                iterator = error.iterator();
                boolean printHeader = true;
                // ERROR
                while (iterator.hasNext()) {
                    if (printHeader) {
                        printWriter.write("----------------------------------------" + Constants.LINE_SEPARATOR);
                        printWriter.write("error" + Constants.LINE_SEPARATOR);
                        printWriter.write("----------------------------------------" + Constants.LINE_SEPARATOR);
                    }
                    printHeader = false;
                    Analysis a = iterator.next();
                    printWriter.write("*");
                    printWriter.write(a.getValue());
                    printWriter.write(Constants.LINE_SEPARATOR);
                }
                // WARN
                iterator = warn.iterator();
                printHeader = true;
                while (iterator.hasNext()) {
                    if (printHeader) {
                        printWriter.write("----------------------------------------" + Constants.LINE_SEPARATOR);
                        printWriter.write("warn" + Constants.LINE_SEPARATOR);
                        printWriter.write("----------------------------------------" + Constants.LINE_SEPARATOR);
                    }
                    printHeader = false;
                    Analysis a = iterator.next();
                    printWriter.write("*");
                    printWriter.write(a.getValue());
                    printWriter.write(Constants.LINE_SEPARATOR);
                }
                // INFO
                iterator = info.iterator();
                printHeader = true;
                while (iterator.hasNext()) {
                    if (printHeader) {
                        printWriter.write("----------------------------------------" + Constants.LINE_SEPARATOR);
                        printWriter.write("info" + Constants.LINE_SEPARATOR);
                        printWriter.write("----------------------------------------" + Constants.LINE_SEPARATOR);
                    }
                    printHeader = false;
                    Analysis a = iterator.next();
                    printWriter.write("*");
                    printWriter.write(a.getValue());
                    printWriter.write(Constants.LINE_SEPARATOR);
                }
                printWriter.write("========================================" + Constants.LINE_SEPARATOR);
            }

            // Unidentified log lines
            List<String> unidentifiedLogLines = jvmRun.getUnidentifiedLogLines();
            if (!unidentifiedLogLines.isEmpty()) {
                printWriter
                        .write(unidentifiedLogLines.size() + " UNIDENTIFIED LOG LINE(S):" + Constants.LINE_SEPARATOR);
                printWriter.write("----------------------------------------" + Constants.LINE_SEPARATOR);

                Iterator<String> iterator = unidentifiedLogLines.iterator();
                while (iterator.hasNext()) {
                    String unidentifiedLogLine = iterator.next();
                    printWriter.write(unidentifiedLogLine);
                    printWriter.write(Constants.LINE_SEPARATOR);
                }
                printWriter.write("========================================" + Constants.LINE_SEPARATOR);
            }
        } catch (

        FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Close streams
            if (printWriter != null) {
                try {
                    printWriter.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
