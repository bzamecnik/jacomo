package org.zamecnik.jacomo.stats;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;

/**
 * Panel with statistic charts. It uses StatsApp to get the data. The charts
 * are only show when there are the data, otherwise they are hidden.
 * @author Bohumir Zamecnik
 */
public class ChartsPanel extends JPanel {

    /**
     * ChartsPanel constructor. The charts are initially hidden. The parent
     * frame is needed to be resized using pack() funcion after GUI
     * modifications.
     * @param frame parent frame
     */
    public ChartsPanel(JFrame frame) {
        this.frame = frame;
        tabbedPane = new JTabbedPane();
        //tabbedPane.setLayout(new BoxLayout(tabbedPane, BoxLayout.PAGE_AXIS));

        //mainPanel.add(new JLabel("count"));

        intervalPanel = new IntervalPanel();
        tabbedPane.addTab("Intervals", intervalPanel);

        hourHistogramPanel = new HistogramPanel();
        tabbedPane.addTab("Hour histogram", hourHistogramPanel);

        weekdayHistogramPanel = new HistogramPanel();
        tabbedPane.addTab("Weekday histogram", weekdayHistogramPanel);
    }

    /**
     * Get new data and redraw charts. Reloading the data could be a longer
     * operation (it needs use the database), so it is done in a separate
     * thread.
     */
    public void reloadData() {
        if (statsApp != null) {
            new SwingWorker<Void, Void>() {

                public Void doInBackground() {
                    statsApp.reload();
                    return null;
                }

                @Override
                public void done() {
                    refreshCharts();
                }
            }.execute();
        }
    }

    void refreshCharts() {
        if (statsApp != null) {
            hourHistogramPanel.setHistogram(statsApp.getHourHistogram());
            weekdayHistogramPanel.setHistogram(statsApp.getWeekdayHistogram());
            intervalPanel.setIntervals(statsApp.getIntervalsWithContactNames());
        }
    }

    /**
     * Get new data and show charts. Reloading the data could be a longer
     * operation (it needs use the database), so it is done in a separate
     * thread. This is similar to realoadData() but it does something different
     * in done() function, so the code has been separated.
     */
    void showPanels() {
        if (statsApp != null) {
            new SwingWorker<Void, Void>() {

                public Void doInBackground() {
                    statsApp.reload();
                    return null;
                }

                @Override
                public void done() {
                    refreshCharts();
                    removeAll();
                    add(tabbedPane);
                    frame.pack();
                }
            }.execute();
        }
    }

    /**
     * Hide the chart panels.
     */
    void hidePanels() {
        removeAll();
        frame.pack();
    }

    /**
     * Set the stats application. Show panels on a non-null statsApp,
     * hide panels otherwise.
     * @param statsApp the stats application to set
     */
    public void setStatsApp(StatsApplication statsApp) {
        this.statsApp = statsApp;
        if (statsApp != null) {
            showPanels();
        } else {
            hidePanels();
        }
    }
    /** Stats application. Can be null. */
    private StatsApplication statsApp;
    /** Parent frame. */
    private JFrame frame;
    private JTabbedPane tabbedPane;
    /** Hour histogram panel. */
    private HistogramPanel hourHistogramPanel;
    /** Weekday histogram panel. */
    private HistogramPanel weekdayHistogramPanel;
    /** Presence intervals panel. */
    private IntervalPanel intervalPanel;
}
