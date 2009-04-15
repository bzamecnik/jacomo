package lib;

/**
 *
 * @author Bohouš
 */
public class Contact {
    private int id;
    private String jid;
    private String name;

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
        this.jid = jid;
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
        this.name = name;
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
}