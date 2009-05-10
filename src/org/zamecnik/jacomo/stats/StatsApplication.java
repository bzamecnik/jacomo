package org.zamecnik.jacomo.stats;

import java.util.Collection;
import java.util.Map;
import org.zamecnik.jacomo.lib.Contact;
import org.zamecnik.jacomo.lib.DBBackend;

/**
 * Statistics application. The responsibility of this class is to hold
 * objects which interpret data in database an compute some statistics. Then
 * it offers the results for further use.
 * @author Bohumir Zamecnik
 */
public class StatsApplication {
    /**
     * StatsApplication constructor. The database backend resource is not owned
     * here. The data is loaded and results computed now.
     * @param dbBackend database backend reference.
     */
    public StatsApplication(DBBackend dbBackend) {
        presenceManager = new PresenceManager(dbBackend);
        presenceManager.reload();
        hourHistogram = Histogram.hourHistogram;
        hourSampler = Sampler.hourSampler;
        weekdayHistogram = Histogram.weekdayHistogram;
        weekdaySampler = Sampler.weekdaySampler;
        reload();
    }

    /**
     * Reload the data from database and compute the statistic results.
     */
    public void reload() {
        presenceManager.reload();

        intervalsWithContactNames = presenceManager.getPresenceIntervalsWithContacts();
        Collection<IntervalList> intervalLists = intervalsWithContactNames.values();

        hourQuantizationSums = hourSampler.sampleAndSum(intervalLists);
        scaledHourHistogramResult = hourHistogram.computeScaledHistogram(hourQuantizationSums);

        weekdayQuantizationSums = weekdaySampler.sampleAndSum(intervalLists);
        scaledWeekdayHistogramResult = weekdayHistogram.computeScaledHistogram(weekdayQuantizationSums);
    }

    /**
     * Get scaled hour histogram results.
     * @return scaled hour histogram
     */
    public double[] getHourHistogram() {
        return scaledHourHistogramResult;
    }

    /**
     * Get scaled weekday histogram results.
     * @return scaled weekday histogram
     */
    public double[] getWeekdayHistogram() {
        return scaledWeekdayHistogramResult;
    }

    /**
     * Get presence intervals with associated contact information.
     * @return intervals and contact information
     */
    public Map<Contact, IntervalList> getIntervalsWithContactNames() {
        return intervalsWithContactNames;
    }

    /** Presence manager used to interprest database data. */
    private PresenceManager presenceManager;

    /** Hour histogram statistic. */
    private Histogram hourHistogram;
    /** Hour sampler. */
    private Sampler hourSampler;
    /** Hour quantization sums intermediate result. */
    private int[] hourQuantizationSums;
    /** Scaled hour histogram result. */
    private double[] scaledHourHistogramResult;

    /** Weekday histogram statistic. */
    private Histogram weekdayHistogram;
    /** Weekday sampler. */
    private Sampler weekdaySampler;
    /** Weekday quantization sums intermediate result. */
    private int[] weekdayQuantizationSums;
    /** Scaled weekday histogram result. */
    private double[] scaledWeekdayHistogramResult;

    /**  Presence intervals with associated contact information. */
    private Map<Contact, IntervalList> intervalsWithContactNames;
}
