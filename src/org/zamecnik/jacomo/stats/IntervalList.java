package org.zamecnik.jacomo.stats;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import org.zamecnik.jacomo.lib.PresenceStatus;

/**
 * List of presence intervals. Currently it supports only online/offline status.
 * It is implemented as a transition list of time points. Each odd item
 * represents the time point of going online, each even item change to offline
 * status.
 * <p>
 * Example (time1, time2, time3, time4 in the list mean intervals):
 * <pre>
 * (-infinity; time1) OFFLINE
 * [time1; time2) ONLINE
 * [time2; time3) OFFLINE
 * [time3; time4) ONLINE
 * (time4; +infinity) OFFLINE
 * </pre>
 * @author Bohumir Zamecnik
 */
public class IntervalList {
    // TODO: support finer-grain presence statuses (away, xa, ...)
    //   - transition list probably wouldn't be enough

    /** Transition list. */
    List<Date> timePoints;

    /**
     * IntervalList constructor.
     */
    public IntervalList() {
        timePoints = new ArrayList<Date>();
    }

    /**
     * Add a time point to the list.
     * @param time time point
     */
    public void add(Date time) {
        timePoints.add(time);
    }

    /**
     * Clear the whole list.
     */
    public void clear() {
        timePoints.clear();
    }

    /**
     * Get presence status in a given time point. The time point given do not
     * necessarily need to be in the list. It can lie anywhere. The value using
     * computed the first preceding point in the list.
     * @param time time point to check
     * @return presence status at the given point
     */
    public PresenceStatus getValue(Date time) {
        int index = Collections.binarySearch(timePoints, time);
        if (index < 0) {
            // Time hit between two points in the list (or outside).
            // Index (represented as (-index - 1) points to a point after the hit.
            // Presence status is taken from the previous point (another -1).
            index = -index-2;
        }
        // else // Time hit a point in the list.
        return getPresenceByIndex(index);
    }

    /**
     * Compute the presence status of the n-th item of the list.
     * If the index is out of bounds of the list, OFFLINE is returned.
     * @param index index of the item
     * @return presence status of the item
     */
    PresenceStatus getPresenceByIndex(int index) {
        if ((index < 0 ) || (index > timePoints.size()) || ((index % 2) != 0)) {
            return PresenceStatus.OFFLINE;
        } else {
            return PresenceStatus.AVAILABLE;
        }
    }

    /**
     * Get a list of time points in the list. The list is unmodifiable.
     * @return list of time points
     */
    public List<Date> getTimePointsList() {
        return Collections.unmodifiableList(timePoints);
    }

    /**
     * Get a list of time points in the list with the end fixed. If the last
     * item of the list opens an interval add a closing item at given time
     * point.
     * The list is unmodifiable.
     * @param endPoint time point to add if needed
     * @return list of time points
     */
    public List<Date> getFixedTimePointsList(Date endPoint) {
        // correct end points to close the open intervals at 'now' time
        // add end point to close the interval if the last interval is open
        if ((timePoints.size() % 2) != 0) {
            List<Date> points = new ArrayList<Date>(timePoints);
            points.add(endPoint);
            return Collections.unmodifiableList(points);
        }
        return Collections.unmodifiableList(timePoints);
    }

    /**
     * Intersect two interval lists. Only parts of intevals present in both
     * list are preserved.
     * <p>
     * The algorithm is based on the ides of X-transition list from RNDr.
     * Josef Pelikan's Computer Graphics letures (16-imagecoding.pdf).
     * <p>
     * Note: both lists must be sorted boforehand
     * @param lhs left hand side interval list
     * @param rhs right hand side interval list
     * @return intersected interval list
     */
    
    public static IntervalList intersect(IntervalList lhs, IntervalList rhs) {
        boolean state, newstate, inStateRhs, inStateLhs;
        state = newstate = inStateRhs = inStateLhs = false;
        Date currentPoint, currentLhsPoint, currentRhsPoint;
        IntervalList newlist = new IntervalList();
        ListIterator<Date> iterLhs = lhs.timePoints.listIterator();
        ListIterator<Date> iterRhs = rhs.timePoints.listIterator();
        while (true) {
            if (iterLhs.hasNext() && iterRhs.hasNext()) {
                currentLhsPoint = iterLhs.next();
                currentRhsPoint = iterRhs.next();
                if (currentLhsPoint.compareTo(currentRhsPoint) < 0) {
                    currentPoint = currentLhsPoint;
                    iterRhs.previous();
                    inStateLhs = !inStateLhs;
                } else {
                    currentPoint = currentRhsPoint;
                    iterLhs.previous();
                    inStateRhs = !inStateRhs;
                }
            } else if (iterLhs.hasNext()) {
                // RHS list have been iterated
                currentPoint = iterLhs.next();
                inStateLhs = !inStateLhs;
            } else if (iterRhs.hasNext()) {
                // LHS list have been iterated
                currentPoint = iterRhs.next();
                inStateRhs = !inStateRhs;
            } else {
                // both lists have been iterated
                break;
            }
            // intersection -> AND operation
            newstate = inStateLhs && inStateRhs;
            if (newstate != state) {
                newlist.add(currentPoint);
            }
            state = newstate;
        }
        return newlist;
    }

    /**
     * Intersect this list with another one using IntervalList.intersect().
     * @param other the other list
     */
    public void intersect(IntervalList other) {
        this.timePoints = IntervalList.intersect(this, other).timePoints;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Date date : timePoints) {
            sb.append(DateFormat.getDateTimeInstance().format(date) + ", ");
        }
        return sb.toString();
    }

//    public static void testIntersect() {
//        IntervalList list1 = new IntervalList();
//        IntervalList list2 = new IntervalList();
//        Calendar cal = Calendar.getInstance();
//        cal.set(2009, 04, 15, 22, 30, 00);
//        list1.add(cal.getTime());
//        cal.add(Calendar.MINUTE, 5);
//        list1.add(cal.getTime());
//        cal.add(Calendar.MINUTE, 3);
//        list1.add(cal.getTime());
//        cal.add(Calendar.MINUTE, 2);
//        list1.add(cal.getTime());
//        cal.add(Calendar.MINUTE, 7);
//        list1.add(cal.getTime());
//
//        cal.set(Calendar.MINUTE, 30);
//        list2.add(cal.getTime());
//        cal.add(Calendar.MINUTE, 9);
//        list2.add(cal.getTime());
//
//        System.out.println("list1: " + list1);
//        System.out.println("list2: " + list2);
//        IntervalList listIntersect = intersect(list2, list1);
//        System.out.println("list intersection: " + listIntersect);
//    }
}
