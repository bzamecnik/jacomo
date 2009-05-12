package org.zamecnik.jacomo.stats;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Sampler turns presence time intervals into values in discrete time blocks.
 * @author Bohumir Zamecnik
 */
public class Sampler {

    /**
     * Sampler constructor. A custom function which computes the date and time
     * when the sample starts can be given.
     * @param sampleSize the size of time sample
     * @param startSampleDateFunc function giving first sample alignment
     */
    public Sampler(
            long sampleSize,
            StartSampleDateFunc startSampleDateFunc) {
        this.sampleSize = sampleSize;
        this.startSampleDateFunc = startSampleDateFunc;
    }


    static {
        hourSampler = new Sampler(
                3600000, // 1 hour in milliseconds
                new Sampler.StartSampleDateFunc() {

            public Calendar computeStartSampleDate(Calendar firstDate) {
                firstDate.set(Calendar.AM_PM, Calendar.AM);
                firstDate.set(Calendar.HOUR, 0);
                firstDate.set(Calendar.MINUTE, 0);
                firstDate.set(Calendar.SECOND, 0);
                firstDate.set(Calendar.MILLISECOND, 0);
                return firstDate;
            }
        });
        weekdaySampler = new Sampler(
                86400000, // 1 day in milliseconds
                new Sampler.StartSampleDateFunc() {

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

//    public boolean[][] sample(Collection<IntervalList> intervalLists) {
//        // - get list of time points from each interval list
//        // - correct end points to close the open intervals at 'now' time
//        // - find out lowest and highest point of all interval lists
//        // - compute number of samples
//        // - for each sample between start and end point:
//        //   - for each interval list:
//        //     - store sample status
//    }

//    /**
//     * Sum sets of sampled values from multiple contacts.
//     * @param sampled first index - sample, second index - contact
//     * @return summed samples
//     */
//    public int[] sum(boolean[][] sampled) {
//        int[] summed = new int[sampled.length];
//        for (int sample = 0; sample < sampled.length; sample++) {
//            boolean[] sampleValues = sampled[sample]; // a possible optimization
//            summed[sample] = 0;
//            for (int user = 0; user < sampled.length; user++) {
//                if (sampleValues[user]) {
//                    summed[sample]++;
//                }
//            }
//        }
//        return summed;
//    }

    /**
     * Sample and sum the presence interval lists. Divide the intervals using
     * discrete time blocks and sample the presence value (currently
     * online/offline). Whenever the interval touches a part of the block the
     * whole block is treated as being online (a kind of maximum metric). We get
     * lists of binary-valued samples of equal length. The next step is to
     * sum up the lists to get numbers of online contacts in each sample.
     * @param intervalLists presence intervals for each contact
     * @return samples with number of online contacts in each one
     */
    public int[] sampleAndSum(Collection<IntervalList> intervalLists) {
        // simple but not very efficient method:
        //return sum(sample(intervalLists));

        // sample and sum at once
        List<List<Date>> points = preparePoints(intervalLists);

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
        int nSamples = (int)(lastPoint - firstPoint);
        if (nSamples <= 0) {
            return null;
        }
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
    
    /**
     * Prepare time points. Convert from collection of interval lists (for each
     * contact) to a list of list of dates.
     * @param intervalLists interval lists for contacts
     * @return outer list - contacts, inner list - time points
     */
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

    /**
     * Round time point to a sample border.
     * @param milliseconds time point in milliseconds since 1970/1/1 0:00:00
     * @param ceiling true - round up (ceiling), false - round down (floor)
     * @return rounded time point in milliseconds
     */
    long roundTimePoint(long milliseconds, boolean ceiling) {
        double tmp = (double) milliseconds / sampleSize;
        return (long) (ceiling ? Math.ceil(tmp) : Math.floor(tmp));
    }

    /**
     * A function which computes the alignment of the first sample.
     * Wrapper over startSampleDateFunc to use Date instead of Calendar.
     * @param firstDate date inside the first sample
     * @return aligned sample start
     */
    Date computeStartSampleDate(Date firstDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(firstDate);
        cal = startSampleDateFunc.computeStartSampleDate(cal);
        return cal.getTime();
    }

    /**
     * A function which computes the alignment of the first sample.
     */
    public interface StartSampleDateFunc {

        /**
         * A function which computes the alignment of the first sample.
         * @param firstDate date inside the first sample
         * @return aligned sample start
         */
        public Calendar computeStartSampleDate(Calendar firstDate);
    }
    /** Size of sample, in milliseconds. */
    long sampleSize;
    StartSampleDateFunc startSampleDateFunc;
    /** Prepared hour sampler aligned to start of an hour. */
    public static final Sampler hourSampler;
    /** Prepared day sampler aligned to the first day of week. */
    public static final Sampler weekdaySampler;
}
