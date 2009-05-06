package org.zamecnik.jacomo.stats;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;

/**
 *
 * @author Bohouš
 */
public class Gui {

    public Gui(StatsApp statsApp) {
        this.statsApp = statsApp;
    }

    private void createAndShowGUI() {
        //JFrame.setDefaultLookAndFeelDecorated(true);
        frame = new JFrame("JaCoMo - stats");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel();
        JScrollPane scrollPane = new JScrollPane(mainPanel);

        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.gridwidth = GridBagConstraints.REMAINDER;
        mainPanel.add(new JLabel("count"), c);
        
        c.weighty = 1.0;
        c.fill = GridBagConstraints.BOTH;

        intervalPanel = new IntervalPanel();
        mainPanel.add(intervalPanel, c);
        hourHistogramPanel = new HistogramPanel();
        //mainPanel.add(hourHistogramPanel, c);
        weekdayHistogramPanel = new HistogramPanel();
        //mainPanel.add(weekdayHistogramPanel, c);
//        c.weighty = 0.0;
//        c.fill = GridBagConstraints.NONE;
//        pane.add(new JLabel("intervals"), c);

        Container pane = frame.getContentPane();
        pane.add(scrollPane);
        frame.pack();
        frame.setVisible(true);

        reloadData();
    }

    public static void run(StatsApp statsApp) {
        final Gui gui = new Gui(statsApp);

        javax.swing.SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                gui.createAndShowGUI();
            }
        });
    }

    void reloadData() {
        SwingWorker worker = new SwingWorker<Void, Void>() {
            public Void doInBackground() {
                statsApp.reload();
                return null;
            }
            @Override
            public void done() {
                refreshCharts();
            }
        };
        worker.execute();
    }

    void refreshCharts() {
        hourHistogramPanel.setHistogram(statsApp.getHourHistogram());
        weekdayHistogramPanel.setHistogram(statsApp.getWeekdayHistogram());
        intervalPanel.setIntervals(statsApp.getIntervalsWithContactNames());
    }

    StatsApp statsApp;
    JFrame frame;
    HistogramPanel hourHistogramPanel;
    HistogramPanel weekdayHistogramPanel;
    IntervalPanel intervalPanel;
}
