package org.zamecnik.jacomo.lib;

/**
 * General JaCoMo exception. Used to translate lower level exceptions.
 * @author Bohumir Zamecnik
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
