package org.zamecnik.jacomo.bot;

import java.util.*;

/**
 * Contact filter. A filter to match Jabber Ids using certain criteria.
 * Currently the filter is implemented to match against a blacklist.
 * @author Bohumir Zamecnik
 */
public class ContactFilter {

    /**
     * ContactFilter construtor.
     */
    public ContactFilter() {
        blacklistKeywords = new java.util.HashSet<String>();
    }

    /**
     * Match a contact againts the filter.
     * @param contact contact Jabber Id
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

    /**
     * Add a keyword to blacklist.
     * @param keyword keyword to add
     */
    public void addBlacklistKeyword(String keyword) {
        blacklistKeywords.add(keyword);
    }

    /**
     * Remove a keyword from blacklist.
     * @param keyword keyword to remove
     */
    public void removeBlacklistKeyword(String keyword) {
        blacklistKeywords.remove(keyword);
    }

    /**
     * Clear the filter.
     */
    public void clear() {
        blacklistKeywords.clear();
    }

    /**
     * Set whether to exclude transport contacts.
     * @param exclude exclude transports on true
     */
    public void setExcludeTransports(boolean exclude) {
        excludeTransports = exclude;
    }
    // TODO:
    // - now the filter block blocks anything containing a keyword
    // - keyword matching a JID as whole only can't be specified
    // - solution: use regular expressions
    /** Blacklist. */
    Set<String> blacklistKeywords;
    /** Exclude transport contacts? */
    boolean excludeTransports = true;
}
