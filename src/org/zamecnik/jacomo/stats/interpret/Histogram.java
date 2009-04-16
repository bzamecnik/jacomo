package org.zamecnik.jacomo.stats.interpret;

import org.zamecnik.jacomo.stats.*;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Bohouš
 */
public class Histogram extends Interpreter {

    public Histogram(int histogramSize, long sampleSize,
            StartSampleDateFunc startSampleDateFunc) {
            this.histogramSize = histogramSize;
            this.sampleSize = sampleSize;
            this.startSampleDateFunc = startSampleDateFunc;
    }

    static {
        hourHistogram = new Histogram(
            24,
            3600000, // 1 hour in milliseconds
            new StartSampleDateFunc() {
                public Calendar computeStartSampleDate(Calendar firstDate) {
                    firstDate.set(Calendar.AM_PM, Calendar.AM);
                    firstDate.set(Calendar.HOUR, 0);
                    firstDate.set(Calendar.MINUTE, 0);
                    firstDate.set(Calendar.SECOND, 0);
                    firstDate.set(Calendar.MILLISECOND, 0);
                    return firstDate;
                }
                }
        );
        weekdayHistogram = new Histogram(
            7,
            86400000, // 1 day in milliseconds
            new StartSampleDateFunc() {
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
        );
    }

    @Override
    public Interpreter.Result interpret() {
        int[] histogram = new int[getHistogramSize()];
        List<Date> timePoints = intervals.getTimePointsList();
        if (timePoints.isEmpty()) {
            return new Result(histogram);
        }

        Calendar cal = Calendar.getInstance();
        Date endPoint = cal.getTime();
        long startPoint = roundTimePoint(computeStartSampleDate(
                timePoints.get(0)).getTime(), false);

        // add end point to close the interval if the last interval is open
        if ((timePoints.size() % 2) != 0) {
            timePoints.add(endPoint);
        }
        
        Iterator<Date> timePointIter = timePoints.iterator();

        long intervalStartPoint;
        long intervalEndPoint;
        while (timePointIter.hasNext()) {
            intervalStartPoint = roundTimePoint(timePointIter.next().getTime(), false)
                    - startPoint;
            intervalEndPoint = roundTimePoint(timePointIter.next().getTime(), true)
                    - startPoint;
            for (long sample = intervalStartPoint;
                sample < intervalEndPoint; sample++)
            {
                histogram[(int)(sample % histogramSize)]++;
            }
        }
        return new Result(histogram);
    }

    long roundTimePoint(long seconds, boolean ceiling) {
        double tmp = (double) seconds / getSampleSize();
        return (long) (ceiling ? Math.ceil(tmp) : Math.floor(tmp));
    }

    Date computeStartSampleDate(Date firstDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(firstDate);
        cal = startSampleDateFunc.computeStartSampleDate(cal);
        return cal.getTime();
    }

    public void setIntervals(IntervalList intervals) {
        this.intervals = intervals;
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
     * @return the sampleSize
     */
    public long getSampleSize() {
        return sampleSize;
    }

    /**
     * @param sampleSize the sampleSize to set
     */
    public void setSampleSize(long sampleSize) {
        this.sampleSize = sampleSize;
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

    public interface StartSampleDateFunc {
        public Calendar computeStartSampleDate(Calendar firstDate);
    }

    IntervalList intervals;
    int histogramSize;
    long sampleSize;
    StartSampleDateFunc startSampleDateFunc;

    public static final Histogram hourHistogram;
    public static final Histogram weekdayHistogram;
}
