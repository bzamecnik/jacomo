package org.zamecnik.jacomo.stats;

import java.util.Collection;

/**
 *
 * @author Bohouš
 */
public class StatsApp {
    public StatsApp(PresenceManager presenceManager) {
        this.presenceManager = presenceManager;
        hourHistogram = Histogram.hourHistogram;
        hourQuantizer = Quantizer.hourQuantizer;
        weekdayHistogram = Histogram.weekdayHistogram;
        weekdayQuantizer = Quantizer.weekdayQuantizer;
    }

    public void reload() {
        presenceManager.refresh();

        Collection<IntervalList> intervalLists = presenceManager.getAllPresenceIntervals();

        hourQuantizationSums = hourQuantizer.quantizeAndSum(intervalLists);
        scaledHourHistogramResult = hourHistogram.computeScaledHistogram(hourQuantizationSums);

        weekdayQuantizationSums = weekdayQuantizer.quantizeAndSum(intervalLists);
        scaledWeekdayHistogramResult = weekdayHistogram.computeScaledHistogram(weekdayQuantizationSums);
    }

    public double[] getHourHistogram() {
        return scaledHourHistogramResult;
    }

    public double[] getWeekdayHistogram() {
        return scaledWeekdayHistogramResult;
    }

    PresenceManager presenceManager;

    Histogram hourHistogram;
    Quantizer hourQuantizer;
    int[] hourQuantizationSums;
    double[] scaledHourHistogramResult;

    Histogram weekdayHistogram;
    Quantizer weekdayQuantizer;
    int[] weekdayQuantizationSums;
    double[] scaledWeekdayHistogramResult;
}
