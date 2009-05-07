package org.zamecnik.jacomo.stats;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYIntervalSeries;
import org.jfree.data.xy.XYIntervalSeriesCollection;
import org.zamecnik.jacomo.lib.Contact;

/**
 *
 * @author Bohouš
 */
public class IntervalPanel extends JPanel {
    public IntervalPanel() {
        dataset = new XYIntervalSeriesCollection();
        createChart(dataset);
        add(new ChartPanel(chart));
    }

    private void createChart(IntervalXYDataset dataset) {
        chart = ChartFactory.createXYBarChart("Online Intervals",
                "Date", true, "Contact", dataset, PlotOrientation.HORIZONTAL,
                false, false, false);


        plot = (XYPlot) chart.getPlot();
        plot.setRangeAxis(new DateAxis("Date"));
        // TODO: contact names
//        SymbolAxis xAxis = new SymbolAxis("Contacts", new String[] {"S1", "S2",
//                "S3", "S4", "S5"});
//        xAxis.setGridBandsVisible(false);
//        plot.setDomainAxis(xAxis);
        XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer();
        renderer.setUseYInterval(true);
        plot.setRenderer(renderer);
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        plot.setRangePannable(true);
        plot.setDomainPannable(true);

        ChartUtilities.applyCurrentTheme(chart);
    }

    // map: contact name -> interval list
    public void setIntervals(Map<Contact, IntervalList> intervals) {
        // TODO: contact names could be sorted
        Set<Contact> contacts = intervals.keySet();
        List<String> contactNames = new ArrayList<String>();
        for (Contact contact : contacts) {
            contactNames.add(contact.getName());
        }
        SymbolAxis xAxis = new SymbolAxis("Contacts",
                contactNames.toArray(new String[0]));
        xAxis.setGridBandsVisible(false);
        plot.setDomainAxis(xAxis);

        dataset.removeAllSeries();
        int index = 0;
        for (Entry<Contact, IntervalList> entry : intervals.entrySet()) {
            XYIntervalSeries series = new XYIntervalSeries(entry.getKey().getName());
            series.setDescription(entry.getKey().getJid());
            IntervalList intervalList = entry.getValue();
            Iterator<Date> pointIterator = intervalList.getFixedTimePointsList(
                    Calendar.getInstance().getTime()).iterator();
            while (pointIterator.hasNext()) {
                Date startPoint = pointIterator.next();
                Date endPoint = pointIterator.next();
                series.add(index, index - 0.45, index + 0.45,
                        startPoint.getTime(), startPoint.getTime(), endPoint.getTime());
            }
            dataset.addSeries(series);
            index++;
        }
    }

    private JFreeChart chart;
    private XYPlot plot;
    private XYIntervalSeriesCollection dataset;
}
