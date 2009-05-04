package org.zamecnik.jacomo.stats.interpret;

import org.zamecnik.jacomo.stats.*;
import java.util.Calendar;
import java.util.Collection;

/**
 *
 * @author Bohouš
 */
public class Histogram extends Interpreter {

    public Histogram(int histogramSize, Quantizer quantizer) {
            this.histogramSize = histogramSize;
            this.quantizer = quantizer;
    }

    static {
        hourHistogram = new Histogram(
            24,
            new Quantizer(
                3600000, // 1 hour in milliseconds
                new Quantizer.StartSampleDateFunc() {
                    public Calendar computeStartSampleDate(Calendar firstDate) {
                        firstDate.set(Calendar.AM_PM, Calendar.AM);
                        firstDate.set(Calendar.HOUR, 0);
                        firstDate.set(Calendar.MINUTE, 0);
                        firstDate.set(Calendar.SECOND, 0);
                        firstDate.set(Calendar.MILLISECOND, 0);
                        return firstDate;
                    }
                }
            )
        );
        weekdayHistogram = new Histogram(
            7,
            new Quantizer(
                86400000, // 1 day in milliseconds
                new Quantizer.StartSampleDateFunc() {
                    public Calendar computeStartSampleDate(Calendar firstDate) {
                        firstDate.set(Calendar.DAY_OF_WEEK,
                                firstDate.getFirstDayOfWeek());
                        firstDate.set(Calendar.AM_PM, Calendar.AM);
                        firstDate.set(Calendar.HOUR, 0);
                        firstDate.set(Calendar.MINUTE, 0);
                        firstDate.set(Calendar.SECOND, 0);
                        firstDate.set(Calendar.MILLISECOND, 0);
                        return firstDate;
                    }
                }
            )
        );
    }

    @Override
    public Interpreter.Result interpret() {
        int[] histogram = new int[histogramSize];

        quantizer.setIntervalLists(intervalLists);
        Quantizer.Result quantizerResult = (Quantizer.Result) quantizer.interpret();
        int[] sums = quantizerResult.getSums();
        for (int i = 0; i < sums.length; i++) {
            histogram[i % histogramSize]++;
        }
        return new Result(histogram);
    }

    /**
     * @return the histogramSize
     */
    public int getHistogramSize() {
        return histogramSize;
    }

    /**
     * @param histogramSize the histogramSize to set
     */
    public void setHistogramSize(int histogramSize) {
        this.histogramSize = histogramSize;
    }

    /**
     * @param intervalLists the intervalLists to set
     */
    public void setIntervalLists(Collection<IntervalList> intervalLists) {
        this.intervalLists = intervalLists;
    }

    public class Result extends Interpreter.Result {
        public Result(int[] histogram) {
            this.histogram = histogram;
        }
        public int[] getHistogram() {
            return histogram;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < histogram.length; i++) {
                sb.append(i + ": " + histogram[i] + "\n");
            }
            return sb.toString();
        }

        int[] histogram;
    }

    int histogramSize;
    Quantizer quantizer;
    private Collection<IntervalList> intervalLists;

    public static final Histogram hourHistogram;
    public static final Histogram weekdayHistogram;
}
