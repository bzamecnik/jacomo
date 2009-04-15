package org.zamecnik.jacomo.stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import org.zamecnik.jacomo.lib.PresenceStatus;

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
                if (currentLhsPoint.compareTo(currentRhsPoint) > 0) {
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
}
