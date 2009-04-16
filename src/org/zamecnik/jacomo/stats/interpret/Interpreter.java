package org.zamecnik.jacomo.stats.interpret;

import org.zamecnik.jacomo.stats.*;

/**
 *
 * @author Bohouš
 */
public abstract class Interpreter {
    public abstract Result interpret();

    public abstract class Result {
    }

    PresenceManager presenceManager;
}
