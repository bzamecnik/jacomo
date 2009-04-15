package org.zamecnik.jacomo.bot;

import java.util.*;

/**
 *
 * @author Bohouš
 */
public class ContactFilter {

    public ContactFilter() {
        blacklistKeywords = new java.util.HashSet<String>();
    }

    /**
     * Filter contacts using a blacklist.
     * @param contact
     * @return true if the contact passed the filter
     */
    public boolean filter(String contact) {
        if (excludeTransports && !contact.contains("@")) {
            return false;
        }
        for (String keyword : blacklistKeywords) {
            if (contact.contains(keyword)) {
                return false;
            }
        }
        return true;
    }

    public void addBlacklistKeyword(String keyword) {
        blacklistKeywords.add(keyword);
    }

    public void removeBlacklistKeyword(String keyword) {
        blacklistKeywords.remove(keyword);
    }

    public void setExcludeTransports(boolean exclude) {
        excludeTransports = exclude;
    }
    // TODO:
    // - now the filter block blocks anything containing a keyword
    // - keyword matching a JID as whole only can't be specified
    // - solution: use regular expressions
    Set<String> blacklistKeywords;
    boolean excludeTransports = true;
}
