package org.zamecnik.jacomo.stats;

import java.util.Random;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYBarDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * @author Bohouš
 */
public class HistogramPanel extends JPanel {

    public HistogramPanel() {
        JFreeChart chart = createChart(createDataset());
        add(new ChartPanel(chart));
    }

    private static JFreeChart createChart(IntervalXYDataset dataset) {
        JFreeChart chart = ChartFactory.createXYBarChart(
            "Histogram",              // chart title
            "Hour",                   // domain axis label
            false,
            "Users online",           // range axis label
            dataset,                  // data
            PlotOrientation.VERTICAL, // orientation
            true,                     // include legend
            false,                    // tooltips
            false );                  // urls

        XYPlot plot = (XYPlot) chart.getPlot();
        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
        domainAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer();
        renderer.setDrawBarOutline(false);
        return chart;
    }

    private static IntervalXYDataset createDataset() {
        XYSeries series = new XYSeries("online user count");
        Random rnd = new Random();
        for (int i = 1; i <= 24; i++) {
            series.add(i - 0.5, rnd.nextInt(100));
        }
        XYSeriesCollection collection = new XYSeriesCollection();
        collection.addSeries(series);
        return new XYBarDataset(collection, 0.9);
    }
}
