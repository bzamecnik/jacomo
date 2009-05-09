package org.zamecnik.jacomo.stats;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Bohumir Zamecnik
 */
public class Quantizer {

    public Quantizer(
            long sampleSize,
            StartSampleDateFunc startSampleDateFunc) {
        this.sampleSize = sampleSize;
        this.startSampleDateFunc = startSampleDateFunc;
    }


    static {
        hourQuantizer = new Quantizer(
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
        });
        weekdayQuantizer = new Quantizer(
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
        });
    }

//    public boolean[][] quantize(Collection<IntervalList> intervalLists) {
//        // - get list of time points from each interval list
//        // - correct end points to close the open intervals at 'now' time
//        // - find out lowest and highest point of all interval lists
//        // - compute number of samples
//        // - for each sample between start and end point:
//        //   - for each interval list:
//        //     - store sample status
//    }
    public int[] sum(boolean[][] quantized) {
        int[] summed = new int[quantized.length];
        for (int sample = 0; sample < quantized.length; sample++) {
            boolean[] sampleValues = quantized[sample]; // a possible optimization
            summed[sample] = 0;
            for (int user = 0; user < quantized.length; user++) {
                if (sampleValues[user]) {
                    summed[sample]++;
                }
            }
        }
        return summed;
    }

    public int[] quantizeAndSum(Collection<IntervalList> intervalLists) {
        // simple but not very efficient method:
        //return sum(quantize(intervalLists));

        // quantize and sum at once
        return quantizeAndSum(preparePoints(intervalLists));
    }

    int[] quantizeAndSum(List<List<Date>> points) {
        // find out minimum and maximum points of all interval lists
        Date firstDate = new Date();
        Date lastDate = new Date(0);
        for (List<Date> pointsList : points) {
            for (Date point : pointsList) {
                if (point.before(firstDate)) {
                    firstDate = point;
                }
                if (point.after(lastDate)) {
                    lastDate = point;
                }
            }
        }
        long firstPoint = roundTimePoint(
                computeStartSampleDate(firstDate).getTime(), false);
        long lastPoint = roundTimePoint(lastDate.getTime(), true);

        // compute number of samples
        int nSamples = (int) ((lastPoint - firstPoint));
        int[] summedSamples = new int[nSamples];

        // TODO: initialize summedSamples to 0 (is it automatic?)

        // - for each list of points:
        //   - for each interval (two consecutive points):
        //     - for each sample between interval start and end point
        //       - compute correct sample and increment it

        long startPoint;
        long endPoint;
        for (List<Date> pointsList : points) {
            Iterator<Date> iter = pointsList.iterator();
            while (iter.hasNext()) {
                startPoint = roundTimePoint(iter.next().getTime(), false) - firstPoint;
                endPoint = roundTimePoint(iter.next().getTime(), true) - firstPoint;
                for (long sample = startPoint; sample < endPoint; sample++) {
                    summedSamples[(int) sample]++;
                }
            }
        }

        return summedSamples;
    }

    List<List<Date>> preparePoints(Collection<IntervalList> intervalLists) {
        Calendar cal = Calendar.getInstance();
        Date endPoint = cal.getTime();

        // get a list of time points from each interval list
        List<List<Date>> points = new ArrayList<List<Date>>();
        for (IntervalList intervalList : intervalLists) {
            points.add(intervalList.getFixedTimePointsList(endPoint));
        }
        return points;
    }

    long roundTimePoint(long seconds, boolean ceiling) {
        double tmp = (double) seconds / sampleSize;
        return (long) (ceiling ? Math.ceil(tmp) : Math.floor(tmp));
    }

    Date computeStartSampleDate(Date firstDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(firstDate);
        cal = startSampleDateFunc.computeStartSampleDate(cal);
        return cal.getTime();
    }

    public interface StartSampleDateFunc {

        public Calendar computeStartSampleDate(Calendar firstDate);
    }
    long sampleSize;
    StartSampleDateFunc startSampleDateFunc;
    public static final Quantizer hourQuantizer;
    public static final Quantizer weekdayQuantizer;
}
