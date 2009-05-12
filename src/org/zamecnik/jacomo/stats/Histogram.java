package org.zamecnik.jacomo.stats;

import java.util.Collection;

/**
 * Histogram statistic. Compute the distribution of online contacts along
 * a period of time.
 * @author Bohumir Zamecnik
 */
public class Histogram {

    /**
     * Histogram constructor.
     * @param histogramSize number of data bins
     */
    public Histogram(int histogramSize) {
        this.histogramSize = histogramSize;
    }

    static {
        hourHistogram = new Histogram(24);
        weekdayHistogram = new Histogram(7);
    }

    /**
     * Compute histogram from lists of intervals using a sampler.
     * First the presence intervals are sampled and summed to a repeating list
     * of data bins. Then values in each group of bins are summed together.
     * @param intervalLists lists of intervals
     * @param sampler sampler
     * @return computed histogram
     */
    public int[] computeHistogram(
            Collection<IntervalList> intervalLists,
            Sampler sampler) {
        int[] samplingSums = sampler.sampleAndSum(intervalLists);
        return computeHistogram(samplingSums);
    }

    /**
     * Computed histogram from precomputed sampling sums.
     * @param samplingSums output of a sampler
     * @return histogram
     */
    public int[] computeHistogram(int[] samplingSums) {
        int[] histogram = new int[histogramSize];
        for (int i = 0; i < samplingSums.length; i++) {
            histogram[i % histogramSize] += samplingSums[i];
        }
        return histogram;
    }

    /**
     * Compute histogram and scale it to make the sum of all the data bins
     * equal 1.
     * @param samplingSums output of a sampler
     * @return scaled histogram
     */
    public double[] computeScaledHistogram(int[] samplingSums) {
        if (samplingSums == null) {
            return null;
        }
        int[] histogram = computeHistogram(samplingSums);
        int total = 0;
        for (int i = 0; i < samplingSums.length; i++) {
            total += samplingSums[i];
        }
        double scaleFactor = 1.0 / (double) total;
        double[] scaledHistogram = new double[histogram.length];
        for (int i = 0; i < scaledHistogram.length; i++) {
            scaledHistogram[i] = histogram[i] * scaleFactor;
        }
        return scaledHistogram;
    }

    /** Number of data bins. */
    final int histogramSize;

    /** Hours in a day. */
    public static final Histogram hourHistogram;
    /** Days in a week. */
    public static final Histogram weekdayHistogram;
}
