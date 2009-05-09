package org.zamecnik.jacomo.lib;

/**
 * Jabber contact representation. A data class representing a contact from a
 * Jabber roster.
 * @author Bohumir Zamecnik
 */
public class Contact {
    private int id;
    private String jid;
    private String name;

    /**
     * Crate a new Contact instance. There are three constructors with default
     * values for optional parameters.
     * @param jid JID (Jabber identification) - mandatory
     * @param name Contact name - optional
     * @param id Database ID - optional
     */
    public Contact(String jid, String name, int id) {
        setJid(jid);
        setName(name);
        setId(id);
    }

    public Contact(String jid, String name) {
        this(jid, name, 0);
    }

    public Contact(String jid) {
        this(jid, "");
    }

    /**
     * @return the jid
     */
    public String getJid() {
        return jid;
    }

    /**
     * @param jid the jid to set
     */
    private void setJid(String jid) {
        this.jid = (jid != null) ? jid : "";
    }
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = (name != null) ? name : "";
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    private void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return getId() + ": " + getJid() + " (" + getName() + ")";
    }
}
