package org.zamecnik.jacomo.stats;

import java.awt.Color;
import java.text.SimpleDateFormat;
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
import org.jfree.chart.axis.PeriodAxis;
import org.jfree.chart.axis.PeriodAxisLabelInfo;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.Range;
import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Minute;
import org.jfree.data.time.Year;
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
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setMouseZoomable(true);
        chartPanel.setDomainZoomable(false);
        chartPanel.setRangeZoomable(true);
        chartPanel.setMouseWheelEnabled(true);
        add(chartPanel);
    }

    private void createChart(IntervalXYDataset dataset) {
        chart = ChartFactory.createXYBarChart("Online Intervals",
                "Date", true, "Contact", dataset, PlotOrientation.HORIZONTAL,
                false, false, false);


        plot = (XYPlot) chart.getPlot();
        //plot.setRangeAxis(new DateAxis("Date"));
        PeriodAxis rangeAxis = new PeriodAxis("Date");
        rangeAxis.setAutoRangeTimePeriodClass(Hour.class);
        PeriodAxisLabelInfo[] periodAxisInfo = new PeriodAxisLabelInfo[3];
        periodAxisInfo[0] = new PeriodAxisLabelInfo(Minute.class,
                new SimpleDateFormat("H:mm"));
        periodAxisInfo[1] = new PeriodAxisLabelInfo(Day.class,
                new SimpleDateFormat("d.M."));
        periodAxisInfo[2] = new PeriodAxisLabelInfo(Year.class,
                new SimpleDateFormat("yyyy"));
        rangeAxis.setLabelInfo(periodAxisInfo);
        plot.setRangeAxis(rangeAxis);

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
        System.out.println("IntervalPanel.setIntervals()");
        // TODO: contact names could be sorted
        Set<Contact> contacts = intervals.keySet();
        List<String> contactNames = new ArrayList<String>();
        for (Contact contact : contacts) {
            contactNames.add(contact.getName());
        }
        SymbolAxis xAxis = new SymbolAxis("Contacts",
                contactNames.toArray(new String[0]));
        xAxis.setGridBandsVisible(false);
        // TODO:
        // we need to set range to make contact not overlap
        Range defaultRange = new Range(- 0.45, Math.min(15, contacts.size()) - 0.45);
        xAxis.setDefaultAutoRange(defaultRange);
        xAxis.setRange(defaultRange);
        plot.setDomainAxis(xAxis);

        dataset.removeAllSeries();
        int index = 0;
        for (Entry<Contact, IntervalList> entry : intervals.entrySet()) {
            Contact contact = entry.getKey();
            XYIntervalSeries series = new XYIntervalSeries(contact.getName());
            series.setDescription(contact.getJid());
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
        if (dataset.getSeriesCount() <= 0) {
            dataset.addSeries(new XYIntervalSeries(""));
        }
    }

    private JFreeChart chart;
    private XYPlot plot;
    private XYIntervalSeriesCollection dataset;
}
