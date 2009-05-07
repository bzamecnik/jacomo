package org.zamecnik.jacomo.stats;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;

/**
 *
 * @author Bohouš
 */
public class ChartsPanel extends JPanel {
    
    public ChartsPanel() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

        mainPanel.add(new JLabel("count"));

        intervalPanel = new IntervalPanel();
        mainPanel.add(intervalPanel);

        hourHistogramPanel = new HistogramPanel();
        //mainPanel.add(hourHistogramPanel);

        weekdayHistogramPanel = new HistogramPanel();
        //mainPanel.add(weekdayHistogramPanel);

        add(new JScrollPane(mainPanel));
    }
    
    void reloadData() {
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
        hourHistogramPanel.setHistogram(statsApp.getHourHistogram());
        weekdayHistogramPanel.setHistogram(statsApp.getWeekdayHistogram());
        intervalPanel.setIntervals(statsApp.getIntervalsWithContactNames());
    }

    void showPanels() {
        if (statsApp == null) {
            return;
        }
        removeAll();
        add(new JScrollPane(mainPanel));
        reloadData();
    }

    void hidePanels() {
        removeAll();
    }
    
    private StatsApp statsApp;
    JPanel mainPanel;
    HistogramPanel hourHistogramPanel;
    HistogramPanel weekdayHistogramPanel;
    IntervalPanel intervalPanel;

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
}
