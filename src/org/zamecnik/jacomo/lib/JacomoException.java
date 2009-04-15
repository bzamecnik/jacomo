package org.zamecnik.jacomo.lib;

/**
 *
 * @author Bohouš
 */
public class JacomoException extends Exception {
    public JacomoException() {
        super();
    }
    public JacomoException(String s) {
        super(s);
    }
    public JacomoException(String s, Throwable t) {
        super(s, t);
    }
    public JacomoException(Throwable t) {
        super(t);
    }
}
