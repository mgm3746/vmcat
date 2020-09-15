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

import org.github.vmcat.domain.jdk.SafepointEvent;
import org.github.vmcat.domain.jdk.SafepointEventSummary;
import org.github.vmcat.util.jdk.Analysis;
import org.github.vmcat.util.jdk.JdkUtil;
import org.github.vmcat.util.jdk.JdkUtil.LogEventType;
import org.github.vmcat.util.jdk.JdkUtil.TriggerType;

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
     */
    private static final String[] TABLES_CREATE_SQL = {
            "create table safepoint_event (id integer identity, time_stamp bigint, trigger_type varchar(64), "
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
     * Event types.
     */
    private List<LogEventType> eventTypes;

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
            String sqlInsertSafepointEvent = "insert into safepoint_event (time_stamp, trigger_type, threads_total, "
                    + "threads_spinning, threads_blocked, spin, block, sync, cleanup, vmop, page_trap_count, "
                    + "log_entry) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?)";

            final int TIME_STAMP_INDEX = 1;
            final int TRIGGER_TYPE_INDEX = 2;
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
                // Use trigger for event name
                pst.setString(TRIGGER_TYPE_INDEX, event.getTriggerType().toString());
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
    public synchronized int getSafepointEventCount() {
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
            throw new RuntimeException("Error determining total safepoint count.");
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
    public synchronized long getSafepointEventTotalPause(LogEventType eventType) {
        long totalPause = 0;
        Statement statement = null;
        ResultSet rs = null;
        try {
            statement = connection.createStatement();
            rs = statement.executeQuery("select sum(sync), sum(cleanup),sum(vmop) from safepoint_event");
            if (rs.next()) {
                totalPause = rs.getLong(1);
                totalPause = totalPause + rs.getLong(2);
                totalPause = totalPause + rs.getLong(3);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("Error determining total safepoint time.");
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

    /**
     * Retrieve all <code>SafepointEvent</code>s.
     * 
     * @return <code>List</code> of events.
     */
    public synchronized List<SafepointEvent> getSafepointEvents() {
        List<SafepointEvent> events = new ArrayList<SafepointEvent>();
        Statement statement = null;
        ResultSet rs = null;
        try {
            statement = connection.createStatement();
            StringBuffer sql = new StringBuffer();
            sql.append("select time_stamp, trigger_type, sync, cleanup, vmop, log_entry from safepoint_event order by "
                    + "time_stamp asc, id asc");
            rs = statement.executeQuery(sql.toString());
            while (rs.next()) {
                SafepointEvent event = JdkUtil.hydrateSafepointEvent(LogEventType.SAFEPOINT, rs.getString(6),
                        rs.getLong(1), rs.getInt(3), rs.getInt(4), rs.getInt(5));
                events.add(event);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("Error retrieving safepoint events.");
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
        return events;
    }

    /**
     * The first <code>SafepointEvent</code>.
     * 
     * @return The first <code>SafepointEvent</code>.
     */
    public synchronized SafepointEvent getFirstSafepointEvent() {
        SafepointEvent event = null;
        Statement statement = null;
        ResultSet rs = null;
        try {
            statement = connection.createStatement();
            rs = statement.executeQuery(
                    "select log_entry from safepoint_event where id = " + "(select min(id) from safepoint_event)");
            if (rs.next()) {
                event = (SafepointEvent) JdkUtil.parseLogLine(rs.getString(1));
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("Error determining first safepoint event.");
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
        return event;
    }

    /**
     * Retrieve the last <code>SafepointEvent</code>.
     * 
     * @return The last <code>SafepointEvent</code>.
     */
    public synchronized SafepointEvent getLastSafepointEvent() {
        SafepointEvent event = null;
        // Retrieve last event from batch or database.
        if (safepointBatch.size() > 0) {
            event = safepointBatch.get(safepointBatch.size() - 1);
        } else {
            event = querySafepointLastEvent();
        }
        return event;
    }

    /**
     * Retrieve the last <code>SafepointEvent</code>.
     * 
     * @return The last <code>SafepointEvent</code> in database.
     */

    private synchronized SafepointEvent querySafepointLastEvent() {
        SafepointEvent event = null;
        Statement statement = null;
        ResultSet rs = null;
        try {
            statement = connection.createStatement();
            rs = statement.executeQuery(
                    "select log_entry from safepoint_event where id = " + "(select max(id) from safepoint_event)");
            if (rs.next()) {
                event = (SafepointEvent) JdkUtil.parseLogLine(rs.getString(1));
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("Error determining last safepoint event.");
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
        return event;
    }

    /**
     * The total <code>SafepointEvent</code> pause time.
     * 
     * @return total pause duration (milliseconds).
     */
    public synchronized long getSafepointTotalPause() {
        long totalPause = 0;
        Statement statement = null;
        ResultSet rs = null;
        try {
            statement = connection.createStatement();
            rs = statement.executeQuery("select sum(sync + cleanup + vmop) from safepoint_event");
            if (rs.next()) {
                totalPause = rs.getLong(1);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("Error determining safepoint total pause time.");
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

    /**
     * The maximum safepoint pause time.
     * 
     * @return maximum pause duration (milliseconds).
     */
    public synchronized int getMaxPause() {
        int maxPause = 0;
        Statement statement = null;
        ResultSet rs = null;
        try {
            statement = connection.createStatement();
            rs = statement.executeQuery("select max(sync + cleanup + vmop) from safepoint_event");
            if (rs.next()) {
                maxPause = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("Error determine maximum pause time.");
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
        return maxPause;
    }

    /**
     * Generate <code>SafepointEventSummary</code>s.
     * 
     * @return <code>List</code> of <code>SafepointEventSummary</code>s.
     */
    public synchronized List<SafepointEventSummary> getSafepointEventSummaries() {
        List<SafepointEventSummary> safepointEventSummaries = new ArrayList<SafepointEventSummary>();
        Statement statement = null;
        ResultSet rs = null;
        try {
            statement = connection.createStatement();
            StringBuffer sql = new StringBuffer();
            sql.append("select trigger_type, count(id), sum(sync + cleanup + vmop) from safepoint_event group by "
                    + "trigger_type order by sum(sync + cleanup + vmop) desc");
            rs = statement.executeQuery(sql.toString());
            while (rs.next()) {
                TriggerType triggerType = JdkUtil.identifyTriggerType(rs.getString(1));
                SafepointEventSummary summary = new SafepointEventSummary(triggerType, rs.getLong(2), rs.getLong(3));
                safepointEventSummaries.add(summary);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("Error retrieving safepoint event summaries.");
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
        return safepointEventSummaries;
    }
}
