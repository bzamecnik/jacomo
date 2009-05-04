package org.zamecnik.jacomo.stats;

import org.zamecnik.jacomo.stats.interpret.*;
import org.zamecnik.jacomo.stats.*;
import java.util.Collection;

/**
 *
 * @author Bohouš
 */
public class Histogram {

    public Histogram(int histogramSize) {
        this.histogramSize = histogramSize;
    }

    static {
        hourHistogram = new Histogram(24);
        weekdayHistogram = new Histogram(7);
    }

    public int[] computeHistogram(
            Collection<IntervalList> intervalLists,
            Quantizer quantizer) {
        int[] quantizationSums = quantizer.quantizeAndSum(intervalLists);
        return computeHistogram(quantizationSums);
    }

    public int[] computeHistogram(int[] quantizationSums) {
        int[] histogram = new int[histogramSize];
        for (int i = 0; i < quantizationSums.length; i++) {
            histogram[i % histogramSize]++;
        }
        return histogram;
    }

    int histogramSize;

    public static final Histogram hourHistogram;
    public static final Histogram weekdayHistogram;
}
