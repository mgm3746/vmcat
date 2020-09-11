/**********************************************************************************************************************
 * vmcat                                                                                                              *
 *                                                                                                                    *
 * Copyright (c) 2020 Mike Millson                                                                                    *
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
package org.github.vmcat.hsql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.github.vmcat.domain.SafepointEvent;
import org.github.vmcat.util.jdk.Analysis;
import org.github.vmcat.util.jdk.JdkUtil.LogEventType;

/**
 * <p>
 * Manage storing and retrieving safepoint data in an HSQL database.
 * </p>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class JvmDao {

    /**
     * SQL statement(s) to create table.
     * 
     * time_stamp, event_name, threads_total, " + "threads_spinning, threads_blocked, spin, block, sync, cleanup, vmop,
     * page_trap_count
     */
    private static final String[] TABLES_CREATE_SQL = {
            "create table safepoint_event (id integer identity, time_stamp bigint, event_name varchar(64), "
                    + "threads_total integer, threads_spinning integer, threads_blocked integer, spin integer, "
                    + "block integer, sync integer, cleanup integer, vmop integer, page_trap_count integer, "
                    + "log_entry varchar(500))" };

    /**
     * SQL statement(s) to delete table(s).
     */
    private static final String[] TABLES_DELETE_SQL = { "delete from safepoint_event " };

    /**
     * The database connection.
     */
    private static Connection connection;

    /**
     * List of all event types associate with JVM run.
     */
    List<LogEventType> eventTypes;

    /**
     * Analysis property keys.
     */
    private List<Analysis> analysis;

    /**
     * Logging lines that do not match any known VM events.
     */
    private List<String> unidentifiedLogLines;

    /**
     * The number of inserts to batch before persisting to database.
     */
    private static int batchSize = 100;

    /**
     * Batch database inserts for improved performance.
     */
    private List<SafepointEvent> safepointBatch;

    /**
     * The JVM options for the JVM run.
     */
    private String options;

    /**
     * JVM version.
     */
    private String version;

    /**
     * Default constructor.
     */
    public JvmDao() {
        try {
            // Load database driver.
            Class.forName("org.hsqldb.jdbcDriver");
        } catch (ClassNotFoundException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("Failed to load HSQLDB JDBC driver.");
        }

        try {
            // Connect to database.

            // Database server for development
            // connection = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost/xdb", "sa",
            // "");

            // In-process standalone mode for deployment.
            connection = DriverManager.getConnection("jdbc:hsqldb:mem:vmdb", "sa", "");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("Error accessing database.");
        }

        // Create tables
        Statement statement = null;
        try {
            statement = connection.createStatement();
            for (int i = 0; i < TABLES_CREATE_SQL.length; i++) {
                statement.executeUpdate(TABLES_CREATE_SQL[i]);
            }
        } catch (SQLException e) {
            if (e.getMessage().startsWith("Table already exists")) {
                cleanup();
            } else {
                System.err.println(e.getMessage());
                throw new RuntimeException("Error creating tables.");
            }
        } finally {
            try {
                statement.close();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                throw new RuntimeException("Error closing Statement.");
            }
        }
        eventTypes = new ArrayList<LogEventType>();
        analysis = new ArrayList<Analysis>();
        unidentifiedLogLines = new ArrayList<String>();
        safepointBatch = new ArrayList<SafepointEvent>();
    }

    public List<String> getUnidentifiedLogLines() {
        return unidentifiedLogLines;
    }

    public List<Analysis> getAnalysis() {
        return analysis;
    }

    public void addSafepointEvent(SafepointEvent event) {
        if (safepointBatch.size() == batchSize) {
            processSafepointBatch();
        }
        safepointBatch.add(event);
    }

    public List<LogEventType> getEventTypes() {
        return eventTypes;
    }

    public void setEventTypes(List<LogEventType> eventTypes) {
        this.eventTypes = eventTypes;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Add safepoint events to database.
     */
    public synchronized void processSafepointBatch() {

        PreparedStatement pst = null;
        try {
            String sqlInsertSafepointEvent = "insert into safepoint_event (time_stamp, event_name, threads_total, "
                    + "threads_spinning, threads_blocked, spin, block, sync, cleanup, vmop, page_trap_count, "
                    + "log_entry) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?)";

            final int TIME_STAMP_INDEX = 1;
            final int EVENT_NAME_INDEX = 2;
            final int THREADS_TOTAL_INDEX = 3;
            final int THREADS_SPIN_INDEX = 4;
            final int THREADS_BLOCK_INDEX = 5;
            final int SPIN_INDEX = 6;
            final int BLOCK_INDEX = 7;
            final int SYNC_INDEX = 8;
            final int CLEANUP_INDEX = 9;
            final int VMOP_INDEX = 10;
            final int TRAP_INDEX = 11;
            final int LOG_ENTRY_INDEX = 12;

            pst = connection.prepareStatement(sqlInsertSafepointEvent);

            for (int i = 0; i < safepointBatch.size(); i++) {
                SafepointEvent event = safepointBatch.get(i);
                pst.setLong(TIME_STAMP_INDEX, event.getTimestamp());
                pst.setString(EVENT_NAME_INDEX, event.getName());
                pst.setInt(THREADS_TOTAL_INDEX, event.getThreadsTotal());
                pst.setInt(THREADS_SPIN_INDEX, event.getThreadsSpinning());
                pst.setInt(THREADS_BLOCK_INDEX, event.getThreadsBlocked());
                pst.setInt(SPIN_INDEX, event.getTimeSpin());
                pst.setInt(BLOCK_INDEX, event.getTimeBlock());
                pst.setInt(SYNC_INDEX, event.getTimeSync());
                pst.setInt(CLEANUP_INDEX, event.getTimeCleanup());
                pst.setInt(VMOP_INDEX, event.getTimeVmop());
                pst.setInt(TRAP_INDEX, event.getPageTrapCount());
                pst.setString(LOG_ENTRY_INDEX, event.getLogEntry());
                pst.addBatch();
            }
            pst.executeBatch();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("Error inserting safepoint event.");
        } finally {
            safepointBatch.clear();
            try {
                pst.close();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                throw new RuntimeException("Error closingPreparedStatement.");
            }
        }
    }

    /**
     * Delete table(s). Useful when running in server mode during development.
     */
    public synchronized void cleanup() {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            for (int i = 0; i < TABLES_DELETE_SQL.length; i++) {
                statement.executeUpdate(TABLES_DELETE_SQL[i]);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("Error deleting rows from tables.");
        } finally {
            try {
                statement.close();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                throw new RuntimeException("Error closing Statement.");
            }
        }
    }

    /**
     * The total number of safepoint events.
     * 
     * @return total number of safepoint events.
     */
    public synchronized int getTotalCount() {
        int count = 0;
        Statement statement = null;
        ResultSet rs = null;
        try {
            statement = connection.createStatement();
            rs = statement.executeQuery("select count(id) from safepoint_event");
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("Error determining safepoint_event event count.");
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                throw new RuntimeException("Error closing ResultSet.");
            }
            try {
                statement.close();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                throw new RuntimeException("Error closing Statement.");
            }
        }
        return count;
    }

    /**
     * The total number of safepoint events.
     * 
     * @param eventType
     *            the type of safepoint event.
     * @return total number of safepoint events.
     */
    public synchronized int getSafepointCount(LogEventType eventType) {
        int count = 0;
        Statement statement = null;
        ResultSet rs = null;
        try {
            statement = connection.createStatement();
            rs = statement.executeQuery(
                    "select count(id) from safepoint_event where event_name='" + eventType.toString() + "'");
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("Error determining RevokeBiasEvent count.");
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                throw new RuntimeException("Error closing ResultSet.");
            }
            try {
                statement.close();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                throw new RuntimeException("Error closing Statement.");
            }
        }
        return count;
    }

    /**
     * The total safepoint time.
     * 
     * @param eventType
     *            the type of safepoint event.
     * @return total pause duration (milliseconds).
     */
    public synchronized long getSafepointTime(LogEventType eventType) {
        long totalPause = 0;
        Statement statement = null;
        ResultSet rs = null;
        try {
            statement = connection.createStatement();
            rs = statement
                    .executeQuery("select sum(sync), sum(cleanup),sum(vmop) from safepoint_event where event_name='"
                            + eventType.toString() + "'");
            if (rs.next()) {
                totalPause = rs.getLong(1);
                totalPause = totalPause + rs.getLong(2);
                totalPause = totalPause + rs.getLong(3);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("Error determining total RevokeBiasEvent time.");
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                throw new RuntimeException("Error closing ResultSet.");
            }
            try {
                statement.close();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                throw new RuntimeException("Error closing Statement.");
            }
        }
        return totalPause;
    }
}
