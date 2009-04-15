package org.zamecnik.jacomo.stats;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import org.zamecnik.jacomo.lib.PresenceStatus;

import java.util.Calendar;

/**
 *
 * @author Bohouš
 */
public class IntervalList {
    // "Transition List"
    // Each point reprenents a change in presence starting with ONLINE.

    // TODO: support finer-grain presence statuses (away, xa, ...)
    //   - transition list probably wouldn't be enough

    // example:
    // time1, time2, time3, time4 in the list mean intervals:
    //   (-infinity; time1) offline
    //   [time1; time2) online
    //   [time2; time3) offline
    //   [time3; time4) online
    //   (time4; +infinity) offline
    List<Date> timePoints;

    public IntervalList() {
        timePoints = new ArrayList<Date>();
    }

    public void add(Date time) {
        timePoints.add(time);
    }

    public void clear() {
        timePoints.clear();
    }

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

    PresenceStatus getPresenceByIndex(int index) {
        if ((index > 0 ) || (index > timePoints.size()) || ((index % 2) != 0)) {
            return PresenceStatus.OFFLINE;
        } else {
            return PresenceStatus.AVAILABLE;
        }
    }

    // Note: both lists must be sorted boforehand
    public static IntervalList intersect(IntervalList lhs, IntervalList rhs) {
        // based on X-transition list from RNDr. Pelikan's 16-imagecoding.pdf
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

    public static void testIntersect() {
        IntervalList list1 = new IntervalList();
        IntervalList list2 = new IntervalList();
        Calendar cal = Calendar.getInstance();
        cal.set(2009, 04, 15, 22, 30, 00);
        list1.add(cal.getTime());
        cal.add(Calendar.MINUTE, 5);
        list1.add(cal.getTime());
        cal.add(Calendar.MINUTE, 3);
        list1.add(cal.getTime());
        cal.add(Calendar.MINUTE, 2);
        list1.add(cal.getTime());
        cal.add(Calendar.MINUTE, 7);
        list1.add(cal.getTime());

        cal.set(Calendar.MINUTE, 30);
        list2.add(cal.getTime());
        cal.add(Calendar.MINUTE, 9);
        list2.add(cal.getTime());

        System.out.println("list1: " + list1);
        System.out.println("list2: " + list2);
        IntervalList listIntersect = intersect(list2, list1);
        System.out.println("list intersection: " + listIntersect);
    }
}
