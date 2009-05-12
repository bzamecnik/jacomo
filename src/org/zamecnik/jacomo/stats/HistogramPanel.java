package org.zamecnik.jacomo.stats;

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
 * Histogram panel GUI component.
 * @author Bohumir Zamecnik
 */
public class HistogramPanel extends JPanel {

    /**
     * HistogramPanel constructor.
     */
    public HistogramPanel() {
        //setHistogram(histogram);
        series = new XYSeries("online users histogram");
        XYSeriesCollection collection = new XYSeriesCollection();
        collection.addSeries(series);
        dataset = new XYBarDataset(collection, 0.9);
        chart = createChart(dataset);
        add(new ChartPanel(chart));
    }

    /**
     * Create and configure a XYBarChart for the histogram.
     * @param dataset data container
     * @return the chart created
     */
    private static JFreeChart createChart(IntervalXYDataset dataset) {
        // TODO: make the labels configurable
        JFreeChart chart = ChartFactory.createXYBarChart(
                "Histogram", // chart title
                "Hour", // domain axis label
                false,
                "Users online", // range axis label
                dataset, // data
                PlotOrientation.VERTICAL, // orientation
                true, // include legend
                false, // tooltips
                false); // urls

        XYPlot plot = (XYPlot) chart.getPlot();
        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
        domainAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer();
        renderer.setDrawBarOutline(false);
        return chart;
    }

    /**
     * Set a new histogram and update the chart.
     * @param histogram histogram to set
     */
    public void setHistogram(double[] histogram) {
        XYSeries updatedSeries = new XYSeries("online users histogram");
        for (int i = 0; i < histogram.length; i++) {
            series.add(i, histogram[i]);
        }
        series = updatedSeries;
    }
    private JFreeChart chart;
    private XYBarDataset dataset;
    private XYSeries series;
}
