package org.zamecnik.jacomo.stats;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;

/**
 *
 * @author Bohouš
 */
public class ChartsPanel extends JPanel {

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

    void hidePanels() {
        removeAll();
        frame.pack();
    }

    /**
     * @param statsApp the statsApp to set
     */
    public void setStatsApp(StatsApp statsApp) {
        this.statsApp = statsApp;
        if (statsApp != null) {
            showPanels();
        } else {
            hidePanels();
        }
    }
    private StatsApp statsApp;
    private JFrame frame;
    private JTabbedPane tabbedPane;
    private HistogramPanel hourHistogramPanel;
    private HistogramPanel weekdayHistogramPanel;
    private IntervalPanel intervalPanel;
}
