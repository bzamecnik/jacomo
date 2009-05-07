package org.zamecnik.jacomo.stats;

import java.util.Collection;
import java.util.Map;
import org.zamecnik.jacomo.lib.Contact;
import org.zamecnik.jacomo.lib.DBBackend;

/**
 *
 * @author Bohouš
 */
public class StatsApp {
    public StatsApp(DBBackend dbBackend) {
        presenceManager = new PresenceManager(dbBackend);
        presenceManager.refresh();
        hourHistogram = Histogram.hourHistogram;
        hourQuantizer = Quantizer.hourQuantizer;
        weekdayHistogram = Histogram.weekdayHistogram;
        weekdayQuantizer = Quantizer.weekdayQuantizer;
        reload();
    }

    public void reload() {
        presenceManager.refresh();

        intervalsWithContactNames = presenceManager.getPresenceIntervalsWithContacts();
        Collection<IntervalList> intervalLists = intervalsWithContactNames.values();

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

    public Map<Contact, IntervalList> getIntervalsWithContactNames() {
        return intervalsWithContactNames;
    }

    private PresenceManager presenceManager;

    private Histogram hourHistogram;
    private Quantizer hourQuantizer;
    private int[] hourQuantizationSums;
    private double[] scaledHourHistogramResult;

    private Histogram weekdayHistogram;
    private Quantizer weekdayQuantizer;
    private int[] weekdayQuantizationSums;
    private double[] scaledWeekdayHistogramResult;

    private Map<Contact, IntervalList> intervalsWithContactNames;
}
