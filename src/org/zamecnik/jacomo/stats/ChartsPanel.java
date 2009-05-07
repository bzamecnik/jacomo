package org.zamecnik.jacomo.stats;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;

/**
 *
 * @author Bohouš
 */
public class ChartsPanel extends JPanel {
    
    public ChartsPanel(JFrame frame) {
        this.frame = frame;
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

        mainPanel.add(new JLabel("count"));

        intervalPanel = new IntervalPanel();
        //mainPanel.add(intervalPanel);

        hourHistogramPanel = new HistogramPanel();
        mainPanel.add(hourHistogramPanel);

        weekdayHistogramPanel = new HistogramPanel();
        mainPanel.add(weekdayHistogramPanel);
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
            //intervalPanel.setIntervals(statsApp.getIntervalsWithContactNames());
        }
    }

    void showPanels() {
        if (statsApp != null) {
            removeAll();
            add(new JScrollPane(mainPanel));
            frame.pack();
            reloadData();
        }
    }

    void hidePanels() {
        removeAll();
        frame.pack();
    }
    
    private StatsApp statsApp;
    private JFrame frame;
    private JPanel mainPanel;
    private HistogramPanel hourHistogramPanel;
    private HistogramPanel weekdayHistogramPanel;
    private IntervalPanel intervalPanel;

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
